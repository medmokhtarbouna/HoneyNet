package edu.wustl.honeyrj.analysis;

import java.util.Date;
import java.util.List;

public class SessionStats {
    public String name;
    public int numFiles;
    public int totalLines;
    public int suspiciousLines;
    public List<String> protocolsUsed;
    public Date timestamp;

    public SessionStats(String name, int numFiles, int totalLines, int suspiciousLines, List<String> protocolsUsed) {
        this.name = name;
        this.numFiles = numFiles;
        this.totalLines = totalLines;
        this.suspiciousLines = suspiciousLines;
        this.protocolsUsed = protocolsUsed;
        this.timestamp = new Date();
    }
}
