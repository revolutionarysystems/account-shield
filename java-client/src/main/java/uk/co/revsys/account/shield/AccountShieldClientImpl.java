package uk.co.revsys.account.shield;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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
    private String url;
    private String account;
    private String username;
    private String password;

    public AccountShieldClientImpl(String url, String account, String username, String password) {
        this(new HttpClientImpl(), url, account, username, password);
    }

    public AccountShieldClientImpl(HttpClient httpClient, String url, String account, String username, String password) {
        this.httpClient = httpClient;
        this.url = url;
        this.account = account;
        this.username = username;
        this.password = password;
    }

    @Override
    public CheckLoginResult checkLogin(String sessionId, String userId) throws IOException {
        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("owner", account);
        data.put("session", sessionId);
        data.put("VID", userId);
        data.put("partition", "login");
        json.put("parameters", data);
        HttpRequest request = new HttpRequest(url);
        if (username != null && !username.isEmpty()) {
            request.setCredentials(new BasicAuthCredentials(username, password));
        }
        request.setMethod(HttpMethod.GET);
        Map parameters = new HashMap();
        String caseString = json.toString();
        logger.info(caseString);
        parameters.put("case", caseString);
        parameters.put("inboundTransformer", "ASLogin.incoming.json");
        parameters.put("processor", "ASLoginProcessor.script");
        request.setParameters(parameters);
        HttpResponse response = httpClient.invoke(request);
        if (response.getStatusCode() == 200) {
            InputStream responseStream = response.getInputStream();
            if (responseStream != null) {
                logger.info(IOUtils.toString(responseStream));
                responseStream.close();
            }
        } else {
            logger.error("AS Server returned " + response.getStatusCode());
            InputStream responseStream = response.getInputStream();
            if (responseStream != null) {
                logger.error(IOUtils.toString(responseStream));
                responseStream.close();
            }
        }
        return new CheckLoginResult();
    }

}
