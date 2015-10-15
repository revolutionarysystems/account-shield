package uk.co.revsys.account.shield;

public class AccountShieldException extends Exception {

    public AccountShieldException(String message) {
        super(message);
    }

    public AccountShieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountShieldException(Throwable cause) {
        super(cause);
    }

}
