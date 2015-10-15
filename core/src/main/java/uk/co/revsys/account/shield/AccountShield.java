package uk.co.revsys.account.shield;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.Message.RecipientType;
import org.apache.commons.lang.RandomStringUtils;
import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;
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

    public void updateUser(String accountId, User user) throws AccountShieldException {
        try {
            OlogyInstance userInstance = getUserInstance(accountId, user.getId());
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

    public DeviceCheck checkDevice(String accountId, String sessionId, String userId) throws AccountShieldException {
        try {
            getUserInstance(accountId, userId);
            JSONObject json = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("owner", accountId);
            data.put("session", sessionId);
            data.put("VID", userId);
            data.put("partition", "login");
            json.put("parameters", data);
            ApplyRuleSetRequest request = new ApplyRuleSetRequest("ASLogin.rules", json.toString(), "ASLogin.incoming.json", "ASLoginProcessor.script");
            Assessment assessment = oddballClient.applyRuleSet(request);
            return new DeviceCheck(true);
        } catch (OddballClientException ex) {
            throw new AccountShieldException("Unable to check device");
        }
    }

    public void requestDeviceVerification(String accountId, String sessionId, String userId) throws AccountShieldException {
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
            email.setText("Your verification code is: " + verificationCode);
            mailer.sendMail(email);
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
        } catch (DaoException ex) {
            throw new AccountShieldException("Unable to verify device");
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

}
