package edu.wustl.honeyrj.geoip;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class GeoIPMapPanel extends JPanel {

    public GeoIPMapPanel() {
        setLayout(new BorderLayout());

        // إعداد الخريطة
        TileFactoryInfo info = new OSMTileFactoryInfo();
        TileFactory tileFactory = new org.jxmapviewer.viewer.DefaultTileFactory(info);
        JXMapViewer mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);

        // المركز الابتدائي للخريطة (يمكن تغييره)
        GeoPosition center = new GeoPosition(20.0, 0.0); // أفريقيا الوسطى كمثال
        mapViewer.setZoom(2);
        mapViewer.setAddressLocation(center);

        // نقاط اختبار (يمكن لاحقًا تعديلها ديناميكيًا)
        Set<DefaultWaypointWithLabel> waypoints = new HashSet<>();
        waypoints.add(new DefaultWaypointWithLabel(new GeoPosition(48.8566, 2.3522), "Paris"));
        waypoints.add(new DefaultWaypointWithLabel(new GeoPosition(40.7128, -74.0060), "New York"));
        waypoints.add(new DefaultWaypointWithLabel(new GeoPosition(35.6895, 139.6917), "Tokyo"));

        // رسم العلامات
        WaypointLabelPainter painter = new WaypointLabelPainter(waypoints);
        mapViewer.setOverlayPainter(painter);

        add(new JScrollPane(mapViewer), BorderLayout.CENTER);
    }
}
