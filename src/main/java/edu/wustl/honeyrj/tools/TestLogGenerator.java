package edu.wustl.honeyrj.tools;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class TestLogGenerator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Entrer une adresse IP: ");
        String ip = scanner.nextLine().trim();

        System.out.print("Entrer un port: ");
        String port = scanner.nextLine().trim();

        String protocol;
        switch (port) {
            case "21":
                protocol = "FTP";
                break;
            case "22":
                protocol = "SSH";
                break;
            case "80":
            case "8080":
                protocol = "HTTP";
                break;
            case "6667":
                protocol = "IRC";
                break;
            default:
                protocol = "GENERIC";
        }

        File baseDir = new File(System.getProperty("user.home") + File.separator + "HoneyRJLogs");
        if (!baseDir.exists()) baseDir.mkdirs();

        String sessionName = "rj_" + System.currentTimeMillis();
        File sessionDir = new File(baseDir, sessionName);
        sessionDir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        File logFile = new File(sessionDir, protocol + "_" + System.currentTimeMillis() + ".log");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write("***********************************************\n");
            writer.write("*****Started at: " + timestamp + "*******\n");
            writer.write("TIMESTAMP,SRC_IP:PRT,DST_IP:PRT,PACKET\n");

            for (int i = 0; i < 5; i++) {
                writer.write(timestamp + "," + ip + ":" + port + ",192.168.0.1:" + port + ",SAMPLE_PACKET_" + i + "\n");
            }

            writer.write("*****Protocol " + protocol + " is finished talking to /" + ip + " using local port " + port + "*****\n");
            writer.write("*****Stopped at: " + timestamp + "*******\n");
            writer.write("***********************************************\n");

            System.out.println("Fichier généré : " + logFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du fichier : " + e.getMessage());
        }
    }
}
