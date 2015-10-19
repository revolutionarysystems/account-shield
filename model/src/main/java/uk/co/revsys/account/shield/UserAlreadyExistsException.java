package uk.co.revsys.account.shield;

public class UserAlreadyExistsException extends AccountShieldException{

    public UserAlreadyExistsException() {
        super("User already exists");
    }

}
