package edu.wustl.honeyrj.analysis;

import java.util.Date;

public class CredentialAttempt {
    public String ip;
    public String username;
    public String password;
    public String protocol;
    public Date timestamp;

    public CredentialAttempt(String ip, String username, String password, String protocol, Date timestamp) {
        this.ip = ip;
        this.username = username;
        this.password = password;
        this.protocol = protocol;
        this.timestamp = timestamp;
    }
}
