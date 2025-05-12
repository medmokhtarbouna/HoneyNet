// ✅ الخطوة 1: تحديث كلاس SessionStats لإضافة حقل IP
package edu.wustl.honeyrj.analysis;

import java.util.Date;
import java.util.Set;

public class SessionStats {
    public String name;
    public Date timestamp;
    public int numFiles;
    public int totalLines;
    public int suspiciousLines;
    public Set<String> protocolsUsed;

    // 🆕 حقل جديد لعناوين IP
    public String ipAddress;

    public SessionStats(String name, Date timestamp, int numFiles, int totalLines,
                        int suspiciousLines, Set<String> protocolsUsed, String ipAddress) {
        this.name = name;
        this.timestamp = timestamp;
        this.numFiles = numFiles;
        this.totalLines = totalLines;
        this.suspiciousLines = suspiciousLines;
        this.protocolsUsed = protocolsUsed;
        this.ipAddress = ipAddress;
    }
}
