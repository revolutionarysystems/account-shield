package uk.co.revsys.account.shield;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.mail.Message.RecipientType;
import org.apache.commons.lang.RandomStringUtils;
import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.revsys.objectology.dao.DaoException;
import uk.co.revsys.objectology.exception.UnexpectedAttributeException;
import uk.co.revsys.objectology.exception.ValidationException;
import uk.co.revsys.objectology.model.instance.OlogyInstance;
import uk.co.revsys.objectology.model.instance.Property;
import uk.co.revsys.objectology.model.instance.Time;
import uk.co.revsys.objectology.model.template.OlogyTemplate;
import uk.co.revsys.objectology.query.JSONQuery;
import uk.co.revsys.objectology.query.Query;
import uk.co.revsys.objectology.service.ServiceFactory;
import uk.co.revsys.oddball.client.ApplyRuleSetRequest;
import uk.co.revsys.oddball.client.Assessment;
import uk.co.revsys.oddball.client.Case;
import uk.co.revsys.oddball.client.CaseQuery;
import uk.co.revsys.oddball.client.OddballClient;
import uk.co.revsys.oddball.client.OddballClientException;

public class AccountShield {

    private OddballClient oddballClient;
    private Mailer mailer;

    public AccountShield(OddballClient oddballClient, Mailer mailer) {
        this.oddballClient = oddballClient;
        this.mailer = mailer;
    }

    public void registerUser(String accountId, User user) throws AccountShieldException {
        try {
            OlogyTemplate userTemplate = ServiceFactory.getOlogyTemplateService().findByName("Account Shield User Template");
            try {
                getUserInstance(accountId, user.getId());
                throw new UserAlreadyExistsException();
            } catch (UserNotFoundException ex) {
                // Ignore
            }
            OlogyInstance userInstance = new OlogyInstance(userTemplate);
            userInstance.setAttribute("accountId", new Property(accountId));
            userInstance.setAttribute("userId", new Property(user.getId()));
            userInstance.setAttribute("email", new Property(user.getEmail()));
            ServiceFactory.getOlogyInstanceService().create(userInstance);
        } catch (DaoException ex) {
            throw new AccountShieldException("Unable to register user");
        } catch (ValidationException ex) {
            throw new AccountShieldException("Unable to register user: " + ex.getMessage());
        } catch (UnexpectedAttributeException ex) {
            throw new AccountShieldException("Unable to register user: " + ex.getMessage());
        }
    }

    public User getUser(String accountId, String userId) throws UserNotFoundException, AccountShieldException {
        OlogyInstance userInstance = getUserInstance(accountId, userId);
        User user = new User(userInstance.getId());
        user.setEmail(userInstance.getAttribute("email", Property.class).getValue());
        return user;
    }

    public void updateUser(String accountId, String userId, User user) throws AccountShieldException {
        try {
//            OlogyInstance userInstance = getUserInstance(accountId, user.getId());
            OlogyInstance userInstance = getUserInstance(accountId, userId);
            userInstance.setAttribute("email", new Property(user.getEmail()));
            ServiceFactory.getOlogyInstanceService().update(userInstance);
        } catch (DaoException ex) {
            throw new AccountShieldException("Unable to update user");
        } catch (ValidationException ex) {
            throw new AccountShieldException("Unable to update user: " + ex.getMessage());
        } catch (UnexpectedAttributeException ex) {
            throw new AccountShieldException("Unable to update user: " + ex.getMessage());
        }
    }

    public void deleteUser(String accountId, String userId) throws AccountShieldException {
        try {
            OlogyInstance userInstance = getUserInstance(accountId, userId);
            ServiceFactory.getOlogyInstanceService().delete(userInstance);
        } catch (DaoException ex) {
            throw new AccountShieldException("Unable to delete user");
        }
    }

    public LoginCheck checkLogin(String accountId, String sessionId, String userId) throws AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            Assessment assessment = applyLogin(accountId, sessionId, userId, "login");
            JSONObject json = new JSONObject(assessment.getBody());
            String challengeString = json.getJSONObject("derived").getString("challenge");
            if(challengeString.equals("none")){
                return new LoginCheck(false);
            }else{
                VerificationReason verificationReason = VerificationReason.UNVERIFIED;
                if(challengeString.equals("disowned")){
                    verificationReason = VerificationReason.DISOWNED;
                }
                StringBuilder detail = new StringBuilder();
                detail.append(json.getString("locationCity")+",");
                detail.append(json.getString("locationRegion")+",");
                detail.append(json.getString("locationCountry")+";");
                detail.append(json.getString("devicePlatform")+",");
                detail.append(json.getString("deviceType")+",");
                detail.append(json.getString("deviceBrowser"));
                return new LoginCheck(true, verificationReason, detail.toString());
            }
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to check login");
        }
    }

    public LoginCheck autoVerify(String accountId, String sessionId, String userId) throws AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            Assessment assessment = applyLogin(accountId, sessionId, userId, "register");
            JSONObject json = new JSONObject(assessment.getBody());
            String challengeString = json.getJSONObject("derived").getString("challenge");
            if(challengeString.equals("none")){
                return new LoginCheck(false);
            }else{
                VerificationReason verificationReason = VerificationReason.UNVERIFIED;
                if(challengeString.equals("disowned")){
                    verificationReason = VerificationReason.DISOWNED;
                }
                return new LoginCheck(true, verificationReason);
            }
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to auto register");
        }
    }

          

    public String requestDeviceVerification(String accountId, String sessionId, String userId, String message) throws AccountShieldException {
        try {
            // TODO send request to oddball for device details
            User user = getUser(accountId, userId);
            OlogyTemplate verificationCodeTemplate = ServiceFactory.getOlogyTemplateService().findByName("Account Shield Verification Code Template");
            OlogyInstance verificationCodeInstance = new OlogyInstance(verificationCodeTemplate);
            verificationCodeInstance.setAttribute("sessionId", new Property(sessionId));
            verificationCodeInstance.setAttribute("accountId", new Property(accountId));
            verificationCodeInstance.setAttribute("userId", new Property(userId));
            String verificationCode = RandomStringUtils.randomAlphanumeric(8);
            verificationCodeInstance.setAttribute("code", new Property(verificationCode));
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 30);
            verificationCodeInstance.setAttribute("expiryTime", new Time(calendar.getTime()));
            ServiceFactory.getOlogyInstanceService().create(verificationCodeInstance);
            Email email = new Email();
            email.setFromAddress("Account Shield", "do.not.reply@echo-central.com");
            email.setSubject("Please verify your device");
            email.addRecipient(user.getEmail(), user.getEmail(), RecipientType.TO);
            email.setText(message + "Your verification code is: " + verificationCode);
            mailer.sendMail(email);
            return verificationCode;
        } catch (DaoException ex) {
            throw new AccountShieldException("Unable to send verification request");
        } catch (ValidationException ex) {
            throw new AccountShieldException("Unable to send verification request");
        } catch (UnexpectedAttributeException ex) {
            throw new AccountShieldException("Unable to send verification request");
        }
    }

    public void verifyDevice(String accountId, String sessionId, String userId, String verificationCode) throws InvalidVerificationCodeException, AccountShieldException {
        try {
            Map params = new HashMap();
            params.put("sessionId", sessionId);
            params.put("accountId", accountId);
            params.put("userId", userId);
            params.put("code", verificationCode);
            Query query = new JSONQuery(params);
            query.setLimit(1);
            List<OlogyInstance> results = ServiceFactory.getOlogyInstanceService().find("verificationCode", query);
            if (results.isEmpty()) {
                throw new InvalidVerificationCodeException();
            }
            OlogyInstance verificationCodeInstance = results.get(0);
            if (verificationCodeInstance.getAttribute("expiryTime", Time.class).getValue().before(new Date())) {
                throw new InvalidVerificationCodeException();
            }
            applyLogin(accountId, sessionId, userId, "email");
            verifyDeviceSessions(accountId, userId, sessionId);
        } catch (DaoException ex) {
            throw new AccountShieldException("Unable to verify device");
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to verify device");
        }
    }

    public List<Session> verifyDeviceSessions(String accountId, String userId, String deviceId) throws UserNotFoundException, AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            JSONObject json = new JSONObject();
            json.put("case.parameters.VID", userId);
            json.put("case.watchValues.fp-device", deviceId);
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", null, accountId, null, null, json.toString(), null, "ASVerify.script");
            List<Case> cases = findCases(query);
            List<Session> sessions = new LinkedList<Session>();
            for (Case sessionCase : cases) {
                System.out.println(sessionCase.getBody());
                JSONObject caseJSON = new JSONObject(sessionCase.getBody());
                sessions.add(getSessionFromJSON(caseJSON));
            }
            return sessions;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve device");
        }
    }


    public List<Session> disownDeviceSessions(String accountId, String userId, String deviceId) throws UserNotFoundException, AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            JSONObject json = new JSONObject();
            json.put("case.parameters.VID", userId);
            json.put("case.watchValues.fp-device", deviceId);
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", null, accountId, null, null, json.toString(), null, "ASDisown.script");
            List<Case> cases = findCases(query);
            List<Session> sessions = new LinkedList<Session>();
            for (Case sessionCase : cases) {
                System.out.println(sessionCase.getBody());
                JSONObject caseJSON = new JSONObject(sessionCase.getBody());
                sessions.add(getSessionFromJSON(caseJSON));
            }
            return sessions;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve device");
        }
    }

    public List<Session> undoDisownDeviceSessions(String accountId, String userId, String deviceId) throws UserNotFoundException, AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            JSONObject json = new JSONObject();
            json.put("case.parameters.VID", userId);
            json.put("case.watchValues.fp-device", deviceId);
            json.put("derived.device-verification", "disownedDevice");
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", null, accountId, null, null, json.toString(), null, "ASUndoDisown.script");
            List<Case> cases = findCases(query);
            List<Session> sessions = new LinkedList<Session>();
            for (Case sessionCase : cases) {
                System.out.println(sessionCase.getBody());
                JSONObject caseJSON = new JSONObject(sessionCase.getBody());
                sessions.add(getSessionFromJSON(caseJSON));
            }
            return sessions;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve device");
        }
    }

    public List<Session> getSessions(String accountId, String userId, int limit) throws UserNotFoundException, AccountShieldException {
        return getSessions(accountId, userId, 0, limit);
    }

    public List<Session> getSessions(String accountId, String userId, int offset, int limit) throws UserNotFoundException, AccountShieldException {
        try {
            if(offset < 0){
                offset = 0;
            }
            if(limit < 1){
                limit = 1;
            }
            getUserInstance(accountId, userId);
            JSONObject json = new JSONObject();
            json.put("case.parameters.VID", userId);
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", "ASEpisodes.visit.xform", accountId, null, "latest " + (offset+1) + "-" + limit, json.toString());
            List<Case> cases = findCases(query);
            List<Session> sessions = new LinkedList<Session>();
            for (Case sessionCase : cases) {
                System.out.println(sessionCase.getBody());
                JSONObject caseJSON = new JSONObject(sessionCase.getBody());
                sessions.add(getSessionFromJSON(caseJSON));
            }
            return sessions;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve sessions");
        }
    }

    public Session getSession(String accountId, String userId, String sessionId) throws UserNotFoundException, AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", "ASEpisodes.visit.xform", accountId, sessionId, "latest");
            Case sessionCase = findCase(query);
            if (sessionCase == null) {
                return null;
            }
            JSONObject caseJSON = new JSONObject(sessionCase.getBody());
            Session session = getSessionFromJSON(caseJSON);
            return session;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve session");
        }
    }

    public List<Page> getSessionHistory(String accountId, String userId, String sessionId) throws UserNotFoundException, AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", null, accountId, sessionId, "latest");
            Case sessionCase = findCase(query);
            if (sessionCase == null) {
                return null;
            }
            JSONObject caseJSON = new JSONObject(sessionCase.getBody());
            System.out.println(caseJSON.toString());
            List<Page> history = new LinkedList<Page>();
            JSONArray signalsJSON = caseJSON.getJSONObject("case").getJSONArray("signals");
            for (int i = 0; i < signalsJSON.length(); i++) {
                JSONObject signalJSON = signalsJSON.getJSONObject(i);
                Page page = new Page();
                String description = signalJSON.getString("description");
                page.setUrl(description.substring(0, description.indexOf("||")));
                page.setTitle(description.substring(description.indexOf("||") + 2));
                page.setTime(new Date(signalJSON.getLong("time")));
                history.add(page);
            }
            return history;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve session history");
        }
    }

    public List<Page> getEpisodeHistory(String accountId, String userId, String episodeId) throws UserNotFoundException, AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            JSONObject json = new JSONObject();
            json.put("_id", episodeId);
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", null, accountId, null, null, json.toString());
            Case sessionCase = findCase(query);
            if (sessionCase == null) {
                return null;
            }
            JSONObject caseJSON = new JSONObject(sessionCase.getBody());
            System.out.println(caseJSON.toString());
            List<Page> history = new LinkedList<Page>();
            JSONArray signalsJSON = caseJSON.getJSONObject("case").getJSONArray("signals");
            for (int i = 0; i < signalsJSON.length(); i++) {
                JSONObject signalJSON = signalsJSON.getJSONObject(i);
                Page page = new Page();
                String description = signalJSON.getString("description");
                page.setUrl(description.substring(0, description.indexOf("||")));
                page.setTitle(description.substring(description.indexOf("||") + 2));
                page.setTime(new Date(signalJSON.getLong("time")));
                history.add(page);
            }
            return history;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve session history");
        }
    }

    public List<Device> getDevices(String accountId, String userId) throws UserNotFoundException, AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            JSONObject json = new JSONObject();
            json.put("case.parameters.VID", userId);
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", "ASEpisodes.visit.xform", accountId, null, "latest", json.toString(), "case.watchValues.fp-device");
            List<Case> cases = findCases(query);
            List<Device> devices = new LinkedList<Device>();
            for (Case deviceCase : cases) {
                JSONObject caseJSON = new JSONObject(deviceCase.getBody());
                Device device = getDeviceFromJSON(caseJSON);
                devices.add(device);
            }
            return devices;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve devices");
        }
    }

    public Device getDevice(String accountId, String userId, String deviceId) throws UserNotFoundException, AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            JSONObject json = new JSONObject();
            json.put("case.parameters.VID", userId);
            json.put("case.watchValues.fp-device", deviceId);
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", "ASEpisodes.visit.xform", accountId, null, "latest", json.toString());
            Case deviceCase = findCase(query);
            if (deviceCase == null) {
                return null;
            }
            JSONObject caseJSON = new JSONObject(deviceCase.getBody());
            Device device = getDeviceFromJSON(caseJSON);
            return device;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve device");
        }
    }

    public List<Session> getSessionsForDevice(String accountId, String userId, String deviceId, int limit) throws UserNotFoundException, AccountShieldException {
        return getSessionsForDevice(accountId, userId, deviceId, 0, limit);
    }

    public List<Session> getSessionsForDevice(String accountId, String userId, String deviceId, int offset, int limit) throws UserNotFoundException, AccountShieldException {
        try {
            if(offset < 0){
                offset = 0;
            }
            if(limit < 1){
                limit = 1;
            }
            getUserInstance(accountId, userId);
            JSONObject json = new JSONObject();
            json.put("case.parameters.VID", userId);
            json.put("case.watchValues.fp-device", deviceId);
            CaseQuery query = new CaseQuery("ASEpisodes.rules", "owner", "ASEpisodes.visit.xform", accountId, null, "latest " + (offset+1) + "-" + limit, json.toString());
            List<Case> cases = findCases(query);
            System.out.println("cases = " + cases.size());
            List<Session> sessions = new LinkedList<Session>();
            for (Case sessionCase : cases) {
                JSONObject caseJSON = new JSONObject(sessionCase.getBody());
                sessions.add(getSessionFromJSON(caseJSON));
            }
            return sessions;
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to retrieve sessions for device");
        }
    }

    private Case findCase(CaseQuery query) throws OddballClientException {
        try {
            return oddballClient.findCase(query);
        } catch (IOException ex) {
            throw new OddballClientException(ex);
        }
    }

    private List<Case> findCases(CaseQuery query) throws OddballClientException {
        try {
            return oddballClient.findCases(query);
        } catch (IOException ex) {
            throw new OddballClientException(ex);
        }
    }

        private OlogyInstance getUserInstance(String accountId, String userId) throws UserNotFoundException, AccountShieldException {
        try {
            Map queryParams = new HashMap();
            queryParams.put("accountId", accountId);
            queryParams.put("userId", userId);
            List<OlogyInstance> results = ServiceFactory.getOlogyInstanceService().find("user", new JSONQuery(queryParams));
            if (results.isEmpty()) {
                throw new UserNotFoundException();
            }
            return results.get(0);
        } catch (DaoException ex) {
            throw new AccountShieldException("Failed to retrieve user");
        }
    }

    private Assessment applyLogin(String accountId, String sessionId, String userId, String verification) throws OddballClientException {
        try {
            JSONObject json = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("owner", accountId);
            data.put("session", sessionId);
            data.put("VID", userId);
            data.put("partition", "login");
            data.put("verification", verification);
            json.put("parameters", data);
            ApplyRuleSetRequest request = new ApplyRuleSetRequest("ASLogin.rules", json.toString(), "ASLogin.incoming.json", "ASLoginProcessor.script");
            Assessment assessment = oddballClient.applyRuleSet(request);
            assessment = new Assessment(new JSONArray(assessment.getBody()).getJSONObject(0).toString());
            return assessment;
        } catch (IOException ex) {
            throw new OddballClientException(ex);
        }
    }

    private Session getSessionFromJSON(JSONObject json) {
        System.out.println(json.toString());
        Session session = new Session();
        session.setId(json.getString("series"));
        session.setEpisodeId(json.getString("_id"));
        session.setTime(new Date(json.getLong("time")));
        Device device = getDeviceFromJSON(json);
        session.setDevice(device);
        Connection connection = new Connection();
        connection.setIpAddress(json.getString("connectionExternalIP"));
        Location location = new Location();
        location.setCity(json.getString("locationCity"));
        location.setCountry(json.getString("locationCountry"));
        String coordinates = json.getString("locationCoords");
        String[] tokens = coordinates.split(",");
        if (tokens.length == 2) {
            try {
                String latitudeString = tokens[0];
                String longitudeString = tokens[1];
                float latitude = Float.parseFloat(latitudeString);
                float longitude = Float.parseFloat(longitudeString);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
            } catch (NumberFormatException ex) {
                System.out.println("Unable to parse coordinates " + coordinates);
            }
        }
        connection.setLocation(location);
        session.setConnection(connection);
        return session;
    }

    private Device getDeviceFromJSON(JSONObject json) {
        System.out.println(json.toString());
        Device device = new Device();
        device.setId(json.optString("fp-device"));
        device.setPlatform(json.getString("devicePlatform"));
        device.setType(json.getString("deviceType"));
        device.setBrowser(json.getString("deviceBrowser"));
        device.setVerification(json.getString("deviceVerification"));
        return device;
    }

}
