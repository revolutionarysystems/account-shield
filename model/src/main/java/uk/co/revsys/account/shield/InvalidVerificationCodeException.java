package uk.co.revsys.account.shield;

public class InvalidVerificationCodeException extends AccountShieldException{

    public InvalidVerificationCodeException() {
        super("Invalid verification code");
    }

}
