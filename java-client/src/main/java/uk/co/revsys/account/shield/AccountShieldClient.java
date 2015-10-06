package uk.co.revsys.account.shield;

import java.io.IOException;

public interface AccountShieldClient {

    public CheckLoginResult checkLogin(String sessionId, String userId) throws IOException;
    
}
