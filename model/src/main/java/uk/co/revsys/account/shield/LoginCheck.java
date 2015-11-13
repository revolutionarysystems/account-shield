package uk.co.revsys.account.shield;

public class LoginCheck {

    private boolean requiresVerification = false;
    private VerificationReason verificationReason;

    public LoginCheck() {
    }

    public LoginCheck(boolean requiresVerification) {
        this.requiresVerification = requiresVerification;
    }

    public LoginCheck(boolean requiresVerification, VerificationReason verificationReason) {
        this.requiresVerification = requiresVerification;
        this.verificationReason = verificationReason;
    }
    
    public boolean getRequiresVerification() {
        return requiresVerification;
    }

    public void setRequiresVerification(boolean requiresVerification) {
        this.requiresVerification = requiresVerification;
    }

    public VerificationReason getVerificationReason() {
        return verificationReason;
    }

    public void setVerificationReason(VerificationReason verificationReason) {
        this.verificationReason = verificationReason;
    }

    
    
}
