package edu.wustl.honeyrj.gui;
import java.util.stream.Collectors;

import edu.wustl.honeyrj.analysis.LogFileAnalyzer;
import edu.wustl.honeyrj.analysis.SessionStats;
import edu.wustl.honeyrj.analysis.CredentialAttempt;
import edu.wustl.honeyrj.analysis.CredentialStore;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.List;
import java.util.*;

import java.nio.file.Files;

import edu.wustl.honeyrj.geoip.GeoIPEntry;

import edu.wustl.honeyrj.geoip.GeoIPResolver;

public class HoneyRJDashboard extends JFrame {

    private JTextArea reportArea;
    private JTable sessionTable;
    private JPanel chartPanel;

    public HoneyRJDashboard() {
        super("SmartHoneyNet - Tableau de bord");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1290, 600);
        setLayout(new BorderLayout());

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        JScrollPane scrollReport = new JScrollPane(reportArea);

        sessionTable = new JTable();
        JScrollPane scrollTable = new JScrollPane(sessionTable);

        chartPanel = new JPanel(new GridLayout(1, 2));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton analyzeBtn = new JButton("Nouvelle analyse");
        JButton exportPdfBtn = new JButton("\ud83d\udcc4 Exporter en PDF");
        JButton exportCsvBtn = new JButton("\ud83d\udcca Exporter en CSV");

        JButton geoIpBtn = new JButton("🌐 Voir la carte GeoIP");
        geoIpBtn.addActionListener(e -> showGeoIpMap());
        topPanel.add(geoIpBtn);

        analyzeBtn.addActionListener(e -> refreshAnalysis());
        exportPdfBtn.addActionListener(this::exportPDF);
        exportCsvBtn.addActionListener(this::exportCSV);

        topPanel.add(analyzeBtn);
        topPanel.add(exportPdfBtn);
        topPanel.add(exportCsvBtn);

        JButton exportCredsCsvBtn = new JButton("⬇️ Exporter Credentials CSV");
        JButton exportCredsPdfBtn = new JButton("⬇️ Exporter Credentials PDF");

        exportCredsCsvBtn.addActionListener(e -> exportCredentialsToCSV());
        exportCredsPdfBtn.addActionListener(e -> exportCredentialsToPDF());

        topPanel.add(exportCredsCsvBtn);
        topPanel.add(exportCredsPdfBtn);


        add(topPanel, BorderLayout.NORTH);
        add(scrollReport, BorderLayout.SOUTH);
        add(chartPanel, BorderLayout.CENTER);
        add(scrollTable, BorderLayout.EAST);

        refreshAnalysis();
        setVisible(true);
    }

//    private Map<String, GeoIPEntry> getTestGeoMap() {
//        Map<String, GeoIPEntry> geoMap = new HashMap<>();
//        geoMap.put("192.168.1.1", new GeoIPEntry("192.168.1.1", 48.8566, 2.3522)); // Paris
//        geoMap.put("8.8.8.8", new GeoIPEntry("8.8.8.8", 37.3861, -122.0839));     // Mountain View, Google
//        geoMap.put("1.1.1.1", new GeoIPEntry("1.1.1.1", -33.4940, 143.2104));     // Cloudflare
//        geoMap.put("41.226.5.10", new GeoIPEntry("41.226.5.10", 36.8065, 10.1815)); // Tunis
//        geoMap.put("102.168.120.55", new GeoIPEntry("102.168.120.55", 18.0790, -15.9650)); // Nouakchott
//        return geoMap;
//    }

//    private void showGeoIpMap() {
//        Map<String, GeoIPEntry> geoMap = getTestGeoMap();
//
//        List<GeoIPEntry> entries = new ArrayList<>(geoMap.values()); // كل الإدخالات التجريبية
//
//        new GeoIPMapWindow(entries);
//    }

    private void showGeoIpMap() {
        Set<String> allIps = new HashSet<>();
        for (int i = 0; i < sessionTable.getRowCount(); i++) {
            Object ipObj = sessionTable.getValueAt(i, 6); // عمود IP قد يكون في العمود 6 أو غيره حسب الترتيب
            if (ipObj != null) {
                String ip = ipObj.toString().trim();
                if (!ip.equalsIgnoreCase("N/A") && !ip.isEmpty() && !ip.equals("0")) {
                    allIps.add(ip);
                }
            }
        }


        if (allIps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune adresse IP détectée dans les sessions.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // الآن نحتاج إلى GeoIPResolver لترجمة IP إلى إحداثيات
        List<GeoIPEntry> resolvedEntries = new ArrayList<>();
        for (String ip : allIps) {
            GeoIPEntry entry = GeoIPResolver.resolve(ip);
            if (entry != null) {
                resolvedEntries.add(entry);
            }
        }

        if (resolvedEntries.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Impossible de localiser les IPs.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        new GeoIPMapWindow(resolvedEntries);
    }




    private void refreshAnalysis() {
        File baseDir = new File(System.getProperty("user.home") + File.separator + "HoneyRJLogs");
        try {
            Map<String, Integer> protoMap = new HashMap<>();
            Map<String, Integer> ipMap = new HashMap<>();
            Map<String, Integer> kwMap = new HashMap<>();
            List<SessionStats> sessions = LogFileAnalyzer.analyzeAllSessions(baseDir, protoMap, ipMap, kwMap);

            chartPanel.removeAll();
            chartPanel.add(ChartUtils.createPieChart("Activité par protocole", protoMap));
            chartPanel.add(ChartUtils.createBarChart("Activité par IP", "Adresse IP", "Nombre de messages", ipMap));
            chartPanel.revalidate();

            StringBuilder summary = new StringBuilder();
            summary.append("\ud83d\udcca Total des sessions : ").append(sessions.size()).append("\n\n");
            for (Map.Entry<String, Integer> e : kwMap.entrySet()) {
                summary.append("\ud83d\udd0d ").append(e.getKey()).append(" : ").append(e.getValue()).append("\n");
            }
            reportArea.setText(summary.toString());

            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Session", "Date", "Fichiers", "Lignes", "Suspectes", "Protocoles", "IP"}, 0);

            for (SessionStats stat : sessions) {
                model.addRow(new Object[]{
                        stat.name,
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(stat.timestamp),
                        stat.numFiles,
                        stat.totalLines,
                        stat.suspiciousLines,
                        String.join(", ", stat.protocolsUsed),
                        stat.ipAddress
                });
            }

            sessionTable.setModel(model);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Échec de l’analyse : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new java.util.Date());
    }

    private void exportCSV(ActionEvent e) {
        File exportDir = new File(System.getProperty("user.home") + File.separator + "HoneyRJExports");
        if (!exportDir.exists()) exportDir.mkdirs();

        JFileChooser chooser = new JFileChooser(exportDir);
        chooser.setDialogTitle("Exporter vers CSV");
        String defaultName = "sessions_" + getTimestamp() + ".csv";
        chooser.setSelectedFile(new File(exportDir, defaultName));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath());
                 CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Session", "Fichiers", "Lignes", "Suspectes", "Protocoles"))) {
                for (int i = 0; i < sessionTable.getRowCount(); i++) {
                    printer.printRecord(
                            sessionTable.getValueAt(i, 0),
                            sessionTable.getValueAt(i, 1),
                            sessionTable.getValueAt(i, 2),
                            sessionTable.getValueAt(i, 3),
                            sessionTable.getValueAt(i, 4));
                }
                JOptionPane.showMessageDialog(this, "Export CSV terminé.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                openExportFolder(exportDir);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur export CSV : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportPDF(ActionEvent e) {
        File exportDir = new File(System.getProperty("user.home") + File.separator + "HoneyRJExports");
        if (!exportDir.exists()) exportDir.mkdirs();

        JFileChooser chooser = new JFileChooser(exportDir);
        chooser.setDialogTitle("Exporter vers PDF");
        String defaultName = "rapport_" + getTimestamp() + ".pdf";
        chooser.setSelectedFile(new File(exportDir, defaultName));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                PdfPTable table = new PdfPTable(sessionTable.getColumnCount());
                for (int i = 0; i < sessionTable.getColumnCount(); i++) {
                    table.addCell(new PdfPCell(new Phrase(sessionTable.getColumnName(i))));
                }
                for (int row = 0; row < sessionTable.getRowCount(); row++) {
                    for (int col = 0; col < sessionTable.getColumnCount(); col++) {
                        Object value = sessionTable.getValueAt(row, col);
                        table.addCell(value != null ? value.toString() : "");
                    }
                }
                document.add(table);
                document.close();
                JOptionPane.showMessageDialog(this, "Export PDF terminé.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                openExportFolder(exportDir);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur export PDF : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openExportFolder(File folder) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folder);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Impossible d’ouvrir le dossier : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCredentialsToCSV() {
        List<CredentialAttempt> creds = CredentialStore.getCredentials();
        if (creds == null || creds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune donnée de connexion à exporter.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File exportDir = new File(System.getProperty("user.home") + File.separator + "HoneyRJExports");
        if (!exportDir.exists()) exportDir.mkdirs();

        JFileChooser chooser = new JFileChooser(exportDir);
        chooser.setDialogTitle("Exporter les identifiants vers CSV");
        String defaultName = "credentials_" + getTimestamp() + ".csv";
        chooser.setSelectedFile(new File(exportDir, defaultName));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath());
                 CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("IP", "Utilisateur", "Mot de passe", "Protocole", "Horodatage"))) {
                for (CredentialAttempt ca : creds) {
                    printer.printRecord(ca.ip, ca.username, ca.password, ca.protocol, ca.timestamp);
                }
                JOptionPane.showMessageDialog(this, "Export des identifiants CSV terminé.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                openExportFolder(exportDir);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'export CSV : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCredentialsToPDF() {
        List<CredentialAttempt> creds = CredentialStore.getCredentials();
        if (creds == null || creds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune donnée de connexion à exporter.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File exportDir = new File(System.getProperty("user.home") + File.separator + "HoneyRJExports");
        if (!exportDir.exists()) exportDir.mkdirs();

        JFileChooser chooser = new JFileChooser(exportDir);
        chooser.setDialogTitle("Exporter les identifiants vers PDF");
        String defaultName = "credentials_" + getTimestamp() + ".pdf";
        chooser.setSelectedFile(new File(exportDir, defaultName));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                PdfPTable table = new PdfPTable(5);
                table.addCell("IP");
                table.addCell("Utilisateur");
                table.addCell("Mot de passe");
                table.addCell("Protocole");
                table.addCell("Horodatage");

                for (CredentialAttempt ca : creds) {
                    table.addCell(ca.ip);
                    table.addCell(ca.username);
                    table.addCell(ca.password);
                    table.addCell(ca.protocol);
                    table.addCell(ca.timestamp.toString());
                }

                document.add(table);
                document.close();
                JOptionPane.showMessageDialog(this, "Export des identifiants PDF terminé.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                openExportFolder(exportDir);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'export PDF : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }




}
