package uk.co.revsys.account.shield;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.utils.http.BasicAuthCredentials;
import uk.co.revsys.utils.http.HttpClient;
import uk.co.revsys.utils.http.HttpClientImpl;
import uk.co.revsys.utils.http.HttpMethod;
import uk.co.revsys.utils.http.HttpRequest;
import uk.co.revsys.utils.http.HttpResponse;

public class AccountShieldClientImpl implements AccountShieldClient {

    final Logger logger = LoggerFactory.getLogger(AccountShieldClientImpl.class);

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String url;
    private String username;
    private String password;

    public AccountShieldClientImpl(String url, String username, String password) {
        this.httpClient = new HttpClientImpl();
        this.objectMapper = new ObjectMapper();
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void registerUser(User user) throws AccountShieldException, IOException {
        HttpRequest request = new HttpRequest(url + "/registerUser");
        request.setMethod(HttpMethod.POST);
        request.setCredentials(new BasicAuthCredentials(username, password));
        request.getParameters().put("userId", user.getId());
        if (user.getEmail() != null) {
            request.getParameters().put("email", URLEncoder.encode(user.getEmail(), "UTF-8"));
        }
        HttpResponse response = httpClient.invoke(request);
        readResponse(response);
    }

    @Override
    public User getUser(String userId) throws UserNotFoundException, AccountShieldException, IOException {
        HttpRequest request = new HttpRequest(url + "/user?userId=" + userId);
        request.setCredentials(new BasicAuthCredentials(username, password));
        HttpResponse response = httpClient.invoke(request);
        String responseText = readResponse(response);
        JSONObject json = new JSONObject(responseText);
        User user = new User(json.getString("id"));
        user.setEmail(json.getString("email"));
        return user;
    }

    @Override
    public void updateUser(User user) throws UserNotFoundException, AccountShieldException, IOException {
        HttpRequest request = new HttpRequest(url + "/user?userId=" + user.getId());
        request.setMethod(HttpMethod.POST);
        request.setCredentials(new BasicAuthCredentials(username, password));
        request.getHeaders().put("Content-Type", "application/json");
        request.setBody(new ByteArrayInputStream(new JSONObject(user).toString().getBytes()));
        HttpResponse response = httpClient.invoke(request);
        readResponse(response);
    }

    @Override
    public LoginCheck checkLogin(String sessionId, String userId) throws UserNotFoundException, AccountShieldException, IOException {
        HttpRequest request = new HttpRequest(url + "/checkLogin?sessionId=" + sessionId + "&userId=" + userId);
        request.setCredentials(new BasicAuthCredentials(username, password));
        HttpResponse response = httpClient.invoke(request);
        String responseText = readResponse(response);
        LoginCheck loginCheck = objectMapper.readValue(responseText, LoginCheck.class);
        return loginCheck;
    }

    @Override
    public void requestDeviceVerification(String sessionId, String userId) throws UserNotFoundException, AccountShieldException, IOException {
        HttpRequest request = new HttpRequest(url + "/requestDeviceVerification?sessionId=" + sessionId + "&userId=" + userId);
        request.setCredentials(new BasicAuthCredentials(username, password));
        HttpResponse response = httpClient.invoke(request);
        readResponse(response);
    }

    @Override
    public void verifyDevice(String sessionId, String userId, String verificationCode) throws UserNotFoundException, AccountShieldException, IOException {
        HttpRequest request = new HttpRequest(url + "/verifyDevice?sessionId=" + sessionId + "&userId=" + userId + "&verificationCode=" + verificationCode);
        request.setCredentials(new BasicAuthCredentials(username, password));
        HttpResponse response = httpClient.invoke(request);
        readResponse(response);
    }

    private String readResponse(HttpResponse response) throws IOException, AccountShieldException {
        InputStream responseStream = response.getInputStream();
        String responseText = null;
        if (responseStream != null) {
            try {
                responseText = IOUtils.toString(responseStream);
            } finally {
                responseStream.close();
            }
        }
        if (response.getStatusCode() == 200) {
            return responseText;
        } else if (response.getStatusCode() == 500) {
            JSONObject json = new JSONObject(responseText);
            String type = json.getString("type");
            String message = json.getString("message");
            if (type.equals(UserNotFoundException.class.getSimpleName())) {
                throw new UserNotFoundException();
            } else if (type.equals(InvalidVerificationCodeException.class.getSimpleName())) {
                throw new InvalidVerificationCodeException();
            } else if (type.equals(UserAlreadyExistsException.class.getSimpleName())) {
                throw new UserAlreadyExistsException();
            }
            throw new AccountShieldException(message);
        } else {
            if (responseText == null) {
                throw new IOException("Server returned status " + response.getStatusCode());
            }
            throw new IOException("Server returned status " + response.getStatusCode() + ": " + responseText);
        }
    }

}
