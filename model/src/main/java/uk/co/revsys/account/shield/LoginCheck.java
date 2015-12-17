package uk.co.revsys.account.shield;

public class LoginCheck {

    private boolean requiresVerification = false;
    private VerificationReason verificationReason;
    private String detail;

    public LoginCheck() {
    }

    public LoginCheck(boolean requiresVerification) {
        this.requiresVerification = requiresVerification;
    }

    public LoginCheck(boolean requiresVerification, VerificationReason verificationReason) {
        this.requiresVerification = requiresVerification;
        this.verificationReason = verificationReason;
    }
    
    public LoginCheck(boolean requiresVerification, VerificationReason verificationReason, String detail) {
        this.requiresVerification = requiresVerification;
        this.verificationReason = verificationReason;
        this.detail = detail;
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

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    
    
}
