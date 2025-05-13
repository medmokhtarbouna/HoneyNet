package edu.wustl.honeyrj.analysis;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFileAnalyzer {

    public static List<SessionStats> analyzeAllSessions(File baseDir,
                                                        Map<String, Integer> protoMap,
                                                        Map<String, Integer> ipMap,
                                                        Map<String, Integer> keywordMap) throws IOException {

        List<SessionStats> sessions = new ArrayList<>();
        String lastSeenIp = "UNKNOWN";

        File[] sessionDirs;
        if (baseDir.isDirectory() && baseDir.getName().startsWith("rj_")) {
            sessionDirs = new File[]{baseDir}; // مجلد جلسة مفرد
        } else {
            sessionDirs = baseDir.listFiles(File::isDirectory); // مجلد يحتوي على جلسات
            if (sessionDirs == null) return sessions;
        }

        for (File session : sessionDirs) {
            int totalLines = 0;
            int suspiciousLines = 0;
            Set<String> protocols = new HashSet<>();
            Set<String> ips = new HashSet<>();
            int numFiles = 0;

            File[] files = session.listFiles((d, name) -> name.endsWith(".log"));
            if (files == null) continue;

            for (File logFile : files) {
                numFiles++;
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        totalLines++;

                        // استخراج محاولات الدخول
                        if (line.toLowerCase().contains("login:") || line.toLowerCase().contains("username")) {
                            String username = extractValue(line);
                            CredentialStore.add(lastSeenIp, username, "", detectProtocol(line), new Date());
                        }

                        if (line.toLowerCase().contains("password:") || line.toLowerCase().contains("mot de passe")) {
                            String password = extractValue(line);
                            List<CredentialAttempt> all = CredentialStore.getCredentials();
                            if (!all.isEmpty()) {
                                CredentialAttempt last = all.get(all.size() - 1);
                                if (last.password == null || last.password.isEmpty()) {
                                    last.password = password;
                                }
                            }
                        }

                        // استخراج البروتوكولات
                        if (line.contains("FTP")) protocols.add("FTP");
                        if (line.contains("SSH")) protocols.add("SSH");
                        if (line.contains("HTTP")) protocols.add("HTTP");
                        if (line.contains("IRC")) protocols.add("IRC");

                        // استخراج عنوان IP
                        if (line.contains("finished talking to")) {
                            Matcher matcher = Pattern.compile("talking to /([\\d\\.]+)").matcher(line);
                            if (matcher.find()) {
                                String extractedIp = matcher.group(1);
                                if (extractedIp != null && !extractedIp.isEmpty() && !extractedIp.equals("0") && !extractedIp.equals("0.0.0.0")) {
                                    lastSeenIp = extractedIp;
                                }
                                ips.add(lastSeenIp);
                                ipMap.put(lastSeenIp, ipMap.getOrDefault(lastSeenIp, 0) + 1);
                            }
                        }

                        // الكلمات الدالة
                        if (line.toLowerCase().contains("password")) keywordMap.merge("password", 1, Integer::sum);
                        if (line.toLowerCase().contains("access denied")) keywordMap.merge("access denied", 1, Integer::sum);
                        if (line.toLowerCase().contains("login")) keywordMap.merge("login", 1, Integer::sum);

                        if (line.toLowerCase().contains("error") || line.toLowerCase().contains("fail") || line.toLowerCase().contains("denied")) {
                            suspiciousLines++;
                        }
                    }
                }
            }

            // IP ممثل للجلسة
            String ipAddress = ips.stream().findFirst().orElse("N/A");

            SessionStats stat = new SessionStats(
                    session.getName(),
                    new Date(session.lastModified()),
                    numFiles,
                    totalLines,
                    suspiciousLines,
                    protocols,
                    ipAddress
            );
            sessions.add(stat);

            // تحديث البروتوكولات
            for (String proto : protocols) {
                protoMap.merge(proto, 1, Integer::sum);
            }
        }

        return sessions;
    }

    private static String extractValue(String line) {
        String[] parts = line.split("[:,]");
        return parts.length > 1 ? parts[1].trim() : "";
    }

    private static String detectProtocol(String line) {
        if (line.toLowerCase().contains("ftp")) return "FTP";
        if (line.toLowerCase().contains("ssh")) return "SSH";
        if (line.toLowerCase().contains("http")) return "HTTP";
        return "UNKNOWN";
    }
}
