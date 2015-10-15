package uk.co.revsys.account.shield;

import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;

public class Emailer extends Mailer{

    private static TransportStrategy getTransportStrategy(String type){
        TransportStrategy strategy = TransportStrategy.valueOf(type);
        return strategy;
    }
    
    public Emailer(String host, Integer port, String username, String password) {
        super(host, port, username, password);
    }

    public Emailer(String host, Integer port, String username, String password, TransportStrategy transportStrategy) {
        super(host, port, username, password, transportStrategy);
    }

    public Emailer(String host, Integer port, String username, String password, String transportStrategy) {
        super(host, port, username, password, getTransportStrategy(transportStrategy));
    }

}
