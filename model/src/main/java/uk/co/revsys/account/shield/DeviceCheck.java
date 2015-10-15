package uk.co.revsys.account.shield;

public class DeviceCheck {

    private boolean verified;

    public DeviceCheck(boolean verified) {
        this.verified = verified;
    }

    public boolean isVerified() {
        return verified;
    }
    
}
