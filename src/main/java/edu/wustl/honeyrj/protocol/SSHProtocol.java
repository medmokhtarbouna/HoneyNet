package edu.wustl.honeyrj.protocol;

import edu.wustl.honeyrj.lowinteraction.LIHelper;
import edu.wustl.honeyrj.lowinteraction.LIProtocol;
import edu.wustl.honeyrj.lowinteraction.TALK_FIRST;

import java.util.Vector;

public class SSHProtocol implements LIProtocol {

    private boolean finished = false;
    private int step = 0;

    @Override
    public TALK_FIRST whoTalksFirst() {
        return TALK_FIRST.SVR_FIRST;
    }

    @Override
    public Vector<String> processInput(String messageFromClient) {
        Vector<String> responses = new Vector<>();

        switch (step) {
            case 0:
                responses.add("SSH-2.0-OpenSSH_8.0");
                responses.add("login: ");
                step = 1;
                break;

            case 1:
                responses.add("password: ");
                step = 2;
                break;

            case 2:
                responses.add("Access denied.");
                finished = true;
                break;

            default:
                responses.add("Session closed.");
                finished = true;
                break;
        }


        return responses;
    }

    @Override
    public int getPort() {
        return 22;
    }

    @Override
    public String toString() {
        return "SSH";
    }

    @Override
    public boolean isConnectionOver() {
        return finished;
    }
}
