package calculadora.iva.gt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoUtils {

    // Encrypted URL: "http://edmenstudio.net:8001/;"
    // Key: "kor"
    private static final String KEY = "kor";
    private static final String ENCRYPTED_URL = "AxsGG1VdRAoWBgocGBsHDwYdRQEXH1VKW19DRFQ=";

    public static String getStreamUrl() {
        return decrypt(ENCRYPTED_URL, KEY);
    }

    private static String decrypt(String encrypted, String key) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] result = new byte[decodedBytes.length];

            for (int i = 0; i < decodedBytes.length; i++) {
                result[i] = (byte) (decodedBytes[i] ^ keyBytes[i % keyBytes.length]);
            }
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
