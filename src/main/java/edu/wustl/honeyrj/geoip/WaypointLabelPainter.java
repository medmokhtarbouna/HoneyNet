package edu.wustl.honeyrj.geoip;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Set;

public class WaypointLabelPainter implements Painter<JXMapViewer> {

    private final Set<? extends Waypoint> waypoints;

    public WaypointLabelPainter(Set<? extends Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();

        Rectangle viewportBounds = map.getViewportBounds();

        for (Waypoint wp : waypoints) {
            if (!(wp instanceof DefaultWaypointWithLabel))
                continue;

            DefaultWaypointWithLabel wpl = (DefaultWaypointWithLabel) wp;
            GeoPosition pos = wpl.getPosition();

            Point2D pt = map.getTileFactory().geoToPixel(pos, map.getZoom());

            // تصحيح الإحداثيات بالنسبة إلى الـ Viewport
            int x = (int) (pt.getX() - viewportBounds.getX());
            int y = (int) (pt.getY() - viewportBounds.getY());

            // دائرة حمراء
            g.setColor(Color.RED);
            g.fillOval(x - 8, y - 8, 16, 16);

            // IP label
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(wpl.getLabel(), x + 18, y - 4);

            // Debug log
//            System.out.println("🖌️ Paint: " + wpl.getLabel() + " at (" + x + ", " + y + ")");
        }

        g.dispose();
    }
}
