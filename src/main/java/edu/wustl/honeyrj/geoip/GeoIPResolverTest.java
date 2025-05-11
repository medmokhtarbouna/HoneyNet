package edu.wustl.honeyrj.geoip;

public class GeoIPResolverTest {
    public static void main(String[] args) {
        String testIp = "8.8.8.8"; // IP خاص بـ Google

        GeoIPEntry result = GeoIPResolver.resolve(testIp);

        if (result != null) {
            System.out.println("✅ Résolu avec succès:");
            System.out.println("IP: " + result.getIp());
            System.out.println("Latitude: " + result.getLatitude());
            System.out.println("Longitude: " + result.getLongitude());
        } else {
            System.out.println("❌ Échec de la résolution de l'adresse IP.");
        }
    }
}
