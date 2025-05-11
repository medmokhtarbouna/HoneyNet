package edu.wustl.honeyrj.gui;

import edu.wustl.honeyrj.geoip.DefaultWaypointWithLabel;
import edu.wustl.honeyrj.geoip.GeoIPEntry;
import edu.wustl.honeyrj.geoip.WaypointLabelPainter;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeoIPMapWindow extends JFrame {

    public GeoIPMapWindow(List<GeoIPEntry> geoEntries) {
        super("Carte des adresses IP suspectes");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // إنشاء الخريطة
        JXMapViewer mapViewer = new JXMapViewer();

        // إعداد مصدر البلاطات
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        mapViewer.setTileFactory(tileFactory);

        // إعداد النقاط
        Set<Waypoint> waypoints = new HashSet<>();
        for (GeoIPEntry entry : geoEntries) {
            GeoPosition pos = new GeoPosition(entry.getLatitude(), entry.getLongitude());
            waypoints.add(new DefaultWaypointWithLabel(pos, entry.getIp()));
        }

        // إعداد الرسام
        WaypointLabelPainter painter = new WaypointLabelPainter(waypoints);
        mapViewer.setOverlayPainter(painter);

        // تركيز على النقاط
        if (!geoEntries.isEmpty()) {
            double avgLat = geoEntries.stream().mapToDouble(GeoIPEntry::getLatitude).average().orElse(0);
            double avgLon = geoEntries.stream().mapToDouble(GeoIPEntry::getLongitude).average().orElse(0);
            mapViewer.setAddressLocation(new GeoPosition(avgLat, avgLon));
        } else {
            mapViewer.setAddressLocation(new GeoPosition(20.0, 0.0)); // وسط أفريقيا
        }

        mapViewer.setZoom(14);
        mapViewer.setBackground(Color.WHITE);

        // تفعيل التفاعل مع الفأرة
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

        // إضافة الخريطة
        getContentPane().add(mapViewer, BorderLayout.CENTER);

        // === أدوات التكبير والتصغير ===
        JButton zoomIn = new JButton("+");
        zoomIn.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() - 1));

        JButton zoomOut = new JButton("-");
        zoomOut.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() + 1));

        JPanel zoomPanel = new JPanel(new GridLayout(2, 1));
        zoomPanel.setOpaque(false);
        zoomPanel.add(zoomIn);
        zoomPanel.add(zoomOut);
        zoomPanel.setBounds(10, 10, 50, 80);

        mapViewer.setLayout(null); // يسمح بوضع يدوي للعناصر
        mapViewer.add(zoomPanel);

        // إظهار النافذة
        mapViewer.revalidate();
        mapViewer.repaint();
        setVisible(true);

        // Log
        System.out.println("✅ Carte affichée avec " + geoEntries.size() + " IPs.");
        System.out.println("🧭 Centrée sur: " + mapViewer.getAddressLocation().getLatitude() + ", " + mapViewer.getAddressLocation().getLongitude());
    }
}
