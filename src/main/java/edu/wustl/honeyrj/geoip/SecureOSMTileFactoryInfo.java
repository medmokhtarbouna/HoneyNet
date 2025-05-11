package edu.wustl.honeyrj.geoip;

import org.jxmapviewer.viewer.TileFactoryInfo;

public class SecureOSMTileFactoryInfo extends TileFactoryInfo {
    public SecureOSMTileFactoryInfo() {
        super("OpenStreetMap HTTPS",
                1, 19, 17,
                256, true, true,
                "https://tile.openstreetmap.org",
                "x", "y", "z");
    }

    @Override
    public String getTileUrl(int x, int y, int zoom) {
        int z = getTotalMapZoom() - zoom;
        return this.baseURL + "/" + z + "/" + x + "/" + y + ".png";
    }
}
