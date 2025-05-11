package edu.wustl.honeyrj.protocol;

import edu.wustl.honeyrj.lowinteraction.LIHelper;
import edu.wustl.honeyrj.lowinteraction.LIProtocol;
import edu.wustl.honeyrj.lowinteraction.TALK_FIRST;

import java.util.Vector;
import java.util.regex.Pattern;

public class HttpProtocol implements LIProtocol {

    private boolean finished = false;
    private int state = 0;

    @Override
    public TALK_FIRST whoTalksFirst() {
        return TALK_FIRST.CLIENT_FIRST;
    }

    @Override
    public Vector<String> processInput(String messageFromClient) {
        Vector<String> response = new Vector<>();

        if (messageFromClient == null) {
            finished = true;
            return response;
        }

        if (Pattern.matches("^GET .* HTTP/1.1$", messageFromClient)) {
            response.add("HTTP/1.1 200 OK");
            response.add("Content-Type: text/plain");
            response.add("");
            response.add("Fake HTTP GET Response");
        } else if (Pattern.matches("^POST .* HTTP/1.1$", messageFromClient)) {
            response.add("HTTP/1.1 200 OK");
            response.add("Content-Type: text/plain");
            response.add("");
            response.add("Fake HTTP POST Response");
        } else {
            response.add("HTTP/1.1 400 Bad Request");
        }

        finished = true;
        return response;
    }

    @Override
    public int getPort() {
        return 8080; // منفذ HTTP شائع بديل عن 80
    }

    @Override
    public String toString() {
        return "HTTP";
    }

    @Override
    public boolean isConnectionOver() {
        return finished;
    }
}
