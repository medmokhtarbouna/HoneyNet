package edu.wustl.honeyrj.geoip;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class GeoIPResolver {

    // خريطة محلية بديلة لبعض IP المعروفة
    private static final Map<String, double[]> localMap = new HashMap<>();
    static {
        localMap.put("105.235.137.77", new double[]{34.020882, -6.841650}); // الرباط
        localMap.put("127.0.0.1", new double[]{0.0, 0.0}); // loopback
    }

    public static GeoIPEntry resolve(String ip) {
        // أولاً نحاول الاتصال بـ API
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
            System.err.println("❌ Erreur localisation IP (API): " + ip + " → " + e.getMessage());
        }

        // إذا فشل، نحاول من الخريطة المحلية
        if (localMap.containsKey(ip)) {
            double[] coords = localMap.get(ip);
            return new GeoIPEntry(ip, coords[0], coords[1]);
        }

        return null;
    }
}
