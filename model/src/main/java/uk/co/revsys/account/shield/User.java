package uk.co.revsys.account.shield;

public class User {

    private String id;
    private String email;

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
}
