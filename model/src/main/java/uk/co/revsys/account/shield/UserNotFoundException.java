package uk.co.revsys.account.shield;

public class UserNotFoundException extends AccountShieldException{

    public UserNotFoundException() {
        super("User not found");
    }

}
