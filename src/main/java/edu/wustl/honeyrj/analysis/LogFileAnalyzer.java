package edu.wustl.honeyrj.analysis;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class LogFileAnalyzer {

    private static final Pattern IP_PATTERN = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
    private static final List<String> suspiciousKeywords = Arrays.asList(
            "admin", "root", "ls", "nmap", "whoami", "access denied", "←", "login", "password"
    );

    private Map<String, Integer> ipCounts = new HashMap<>();
    private Map<String, Integer> protocolCounts = new HashMap<>();
    private Map<String, Integer> keywordCounts = new HashMap<>();

    public void analyzeLogs(String directoryPath) throws IOException {
        Files.walk(Paths.get(directoryPath))
                .filter(p -> p.toString().endsWith(".log"))
                .forEach(this::processLogFile);
        generateReport(directoryPath);
    }

    private void processLogFile(Path logPath) {
        String protocol = logPath.getFileName().toString().split("_")[0];
        protocolCounts.merge(protocol, 1, Integer::sum);

        try (BufferedReader reader = new BufferedReader(new FileReader(logPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher ipMatcher = IP_PATTERN.matcher(line);
                if (ipMatcher.find()) {
                    String ip = ipMatcher.group(1);
                    ipCounts.merge(ip, 1, Integer::sum);
                }

                for (String keyword : suspiciousKeywords) {
                    if (line.toLowerCase().contains(keyword)) {
                        keywordCounts.merge(keyword, 1, Integer::sum);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Échec de lecture : " + logPath);
        }
    }

    private void generateReport(String outputDir) {
        File report = new File(outputDir, "résumé_des_logs.txt");
        try (PrintWriter writer = new PrintWriter(report)) {
            writer.println("=== Résumé des journaux HoneyRJ ===");
            writer.println("\n--- Utilisation des protocoles ---");
            protocolCounts.forEach((p, c) -> writer.printf("%s : %d%n", p, c));

            writer.println("\n--- Comptes d’interaction par IP ---");
            ipCounts.forEach((ip, c) -> writer.printf("%s : %d%n", ip, c));

            writer.println("\n--- Mots-clés suspects détectés ---");
            keywordCounts.forEach((kw, c) -> writer.printf("%s : %d%n", kw, c));
        } catch (IOException e) {
            System.err.println("Échec de l’écriture du rapport : " + e.getMessage());
        }
    }

    public static String analyze(String logsDirPath) throws IOException {
        Path logDir = Paths.get(logsDirPath);
        if (!Files.isDirectory(logDir)) throw new IOException("Le chemin n’est pas valide");

        int totalLines = 0;
        int suspiciousCount = 0;
        StringBuilder summary = new StringBuilder("Résumé de l’analyse :\n");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDir, "*.log")) {
            for (Path entry : stream) {
                List<String> lines = Files.readAllLines(entry);
                totalLines += lines.size();
                for (String line : lines) {
                    if (line.toLowerCase().contains("access denied") || line.contains("←")
                            || line.toLowerCase().contains("login") || line.toLowerCase().contains("password")) {
                        suspiciousCount++;
                    }
                }
                summary.append("- ").append(entry.getFileName()).append(" : ")
                        .append(lines.size()).append(" lignes\n");
            }
        }

        summary.append("\nTotal : ").append(totalLines).append(" lignes\n")
                .append("Tentatives suspectes : ").append(suspiciousCount).append("\n");

        return summary.toString();
    }

    public static String analyzeAll(File baseDir) throws IOException {
        File[] sessions = baseDir.listFiles(File::isDirectory);
        if (sessions == null || sessions.length == 0) return "Aucune session à analyser.";

        int totalLines = 0;
        int totalSuspicious = 0;
        Map<String, Integer> suspiciousByProtocol = new HashMap<>();

        for (File session : sessions) {
            File[] logs = session.listFiles((dir, name) -> name.endsWith(".log"));
            if (logs == null) continue;

            for (File log : logs) {
                List<String> lines = Files.readAllLines(log.toPath());
                int sessionSuspicious = 0;
                for (String line : lines) {
                    totalLines++;
                    if (line.toLowerCase().contains("password") || line.toLowerCase().contains("login")
                            || line.toLowerCase().contains("access denied") || line.contains("←")) {
                        sessionSuspicious++;
                    }
                }
                String proto = log.getName().split("_")[0];
                suspiciousByProtocol.put(proto, suspiciousByProtocol.getOrDefault(proto, 0) + sessionSuspicious);
                totalSuspicious += sessionSuspicious;
            }
        }

        StringBuilder summary = new StringBuilder();
        summary.append("📊 Analyse complète :\n");
        summary.append("Total de lignes : ").append(totalLines).append("\n");
        summary.append("Total des tentatives suspectes : ").append(totalSuspicious).append("\n\n");

        for (Map.Entry<String, Integer> entry : suspiciousByProtocol.entrySet()) {
            summary.append("↪ ").append(entry.getKey()).append(" : ")
                    .append(entry.getValue()).append(" tentatives suspectes\n");
        }

        return summary.toString();
    }

    public static List<SessionStats> analyzeAllSessions(File baseDir,
                                                        Map<String, Integer> protocolCounts,
                                                        Map<String, Integer> ipCounts,
                                                        Map<String, Integer> keywordCounts) throws IOException {

        List<SessionStats> sessions = new ArrayList<>();

        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new IOException("Le dossier est introuvable ou invalide");
        }

        File[] sessionDirs = baseDir.listFiles(File::isDirectory);
        if (sessionDirs == null) return sessions;

        for (File dir : sessionDirs) {
            File[] files = dir.listFiles((f, n) -> n.endsWith(".log"));
            if (files == null) continue;

            List<String> protocolsUsed = new ArrayList<>();
            int totalLines = 0;
            int suspiciousLines = 0;

            for (File f : files) {
                String protocol = f.getName().split("_")[0];
                if (!protocolsUsed.contains(protocol)) {
                    protocolsUsed.add(protocol);
                }
                protocolCounts.merge(protocol, 1, Integer::sum);

                List<String> lines = Files.readAllLines(f.toPath());
                totalLines += lines.size();

                for (String line : lines) {
                    if (line.toLowerCase().contains("access denied") || line.contains("←")
                            || line.toLowerCase().contains("login") || line.toLowerCase().contains("password")) {
                        suspiciousLines++;
                    }
                    Matcher ipMatcher = IP_PATTERN.matcher(line);
                    if (ipMatcher.find()) {
                        ipCounts.merge(ipMatcher.group(1), 1, Integer::sum);
                    }
                    for (String kw : suspiciousKeywords) {
                        if (line.toLowerCase().contains(kw)) {
                            keywordCounts.merge(kw, 1, Integer::sum);
                        }
                    }
                }
            }

            sessions.add(new SessionStats(
                    dir.getName(),
                    files.length,
                    totalLines,
                    suspiciousLines,
                    protocolsUsed
            ));
        }

        return sessions;
    }


}
