package edu.wustl.honeyrj.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CredentialStore {
    private static final List<CredentialAttempt> store = new ArrayList<>();

    public static void add(String ip, String user, String pass, String protocol, Date timestamp) {
        store.add(new CredentialAttempt(ip, user, pass, protocol,timestamp));
    }

    public static List<CredentialAttempt> getCredentials() {
        return Collections.unmodifiableList(store);
    }

    public static void clear() {
        store.clear();
    }
}
