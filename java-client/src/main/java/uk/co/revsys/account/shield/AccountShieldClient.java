package uk.co.revsys.account.shield;

import java.io.IOException;

public interface AccountShieldClient {

    public void registerUser(User user) throws AccountShieldException, IOException;
    
    public User getUser(String userId) throws UserNotFoundException, AccountShieldException, IOException;
    
    public void updateUser(User user) throws UserNotFoundException, AccountShieldException, IOException;
    
    public DeviceCheck checkDevice(String sessionId, String userId) throws UserNotFoundException, AccountShieldException, IOException;
    
    public void requestDeviceVerification(String sessionId, String userId) throws UserNotFoundException, AccountShieldException, IOException;
    
    public void verifyDevice(String sessionId, String userId, String verificationCode) throws UserNotFoundException, InvalidVerificationCodeException, AccountShieldException, IOException;
    
}
