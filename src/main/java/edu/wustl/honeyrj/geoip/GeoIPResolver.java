package edu.wustl.honeyrj.geoip;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class GeoIPResolver {

    public static GeoIPEntry resolve(String ip) {
        try {
            String urlStr = "http://ip-api.com/json/" + ip + "?fields=lat,lon,status";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                JSONObject json = new JSONObject(response.toString());
                if ("success".equalsIgnoreCase(json.getString("status"))) {
                    double lat = json.getDouble("lat");
                    double lon = json.getDouble("lon");
                    return new GeoIPEntry(ip, lat, lon);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur localisation IP: " + ip + " → " + e.getMessage());
        }

        return null;
    }
}
