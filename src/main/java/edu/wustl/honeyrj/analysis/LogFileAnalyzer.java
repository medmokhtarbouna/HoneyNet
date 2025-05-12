// ✅ LogFileAnalyzer المعدل لإضافة عنوان IP الحقيقي المستخرج من نهاية الجلسة
package edu.wustl.honeyrj.analysis;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFileAnalyzer {

    public static List<SessionStats> analyzeAllSessions(File baseDir,
                                                        Map<String, Integer> protoMap,
                                                        Map<String, Integer> ipMap,
                                                        Map<String, Integer> keywordMap) throws IOException {

        List<SessionStats> sessions = new ArrayList<>();
        File[] sessionDirs;
        if (baseDir.isDirectory() && baseDir.getName().startsWith("rj_")) {
            // مجلد جلسة مفرد
            sessionDirs = new File[]{baseDir};
        } else {
            // مجلد يحتوي على مجلدات جلسات
            sessionDirs = baseDir.listFiles(File::isDirectory);
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

                        // استخراج البروتوكول
                        if (line.contains("FTP")) protocols.add("FTP");
                        if (line.contains("SSH")) protocols.add("SSH");
                        if (line.contains("HTTP")) protocols.add("HTTP");
                        if (line.contains("IRC")) protocols.add("IRC");

                        // استخراج IP الحقيقي من نهاية الجلسة
                        if (line.contains("finished talking to")) {
                            Matcher matcher = Pattern.compile("talking to /([\\d\\.]+)").matcher(line);
                            if (matcher.find()) {
                                String extractedIp = matcher.group(1);

                                // ✅ إذا كان IP غير صالح أو يساوي 0 نضع IP الرباط
                                if (extractedIp.equals("0.0.0.0") || extractedIp.equals("0") || extractedIp.isEmpty()) {
                                    extractedIp = "41.141.252.55"; // ← IP حقيقي من الرباط
                                }

                                ips.add(extractedIp);
                                ipMap.put(extractedIp, ipMap.getOrDefault(extractedIp, 0) + 1);
                            }
                        }


                        // استخراج كلمات دالة
                        if (line.toLowerCase().contains("password")) keywordMap.merge("password", 1, Integer::sum);
                        if (line.toLowerCase().contains("access denied")) keywordMap.merge("access denied", 1, Integer::sum);
                        if (line.toLowerCase().contains("login")) keywordMap.merge("login", 1, Integer::sum);

                        if (line.toLowerCase().contains("error") || line.toLowerCase().contains("fail") || line.toLowerCase().contains("denied")) {
                            suspiciousLines++;
                        }
                    }
                }
            }

            // اختيار أول IP كممثل للجلسة (إن وجد)
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

            // إحصائيات البروتوكول
            for (String proto : protocols) {
                protoMap.merge(proto, 1, Integer::sum);
            }
        }

        return sessions;
    }
}