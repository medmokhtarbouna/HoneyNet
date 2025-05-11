package edu.wustl.honeyrj.geoip;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

public class DefaultWaypointWithLabel extends DefaultWaypoint {
    private final String label;

    public DefaultWaypointWithLabel(GeoPosition pos, String label) {
        super(pos);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
