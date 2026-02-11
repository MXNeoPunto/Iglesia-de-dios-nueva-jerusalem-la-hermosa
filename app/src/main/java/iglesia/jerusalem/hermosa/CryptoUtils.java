package iglesia.jerusalem.hermosa;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoUtils {

    // Encrypted URL: "https://app0102.sonicpanelradio.com/8070/stream"
    // Key: "NuevaJerusalemKey"
    private static final String KEY = "NuevaJerusalemKey";
    private static final String ENCRYPTED_URL = "JgERBhJwSl0UAxFcVF15SwohGwwVESsLFxkBAAgMAmUGFiNaXUZWekoBAQEEDQg=";

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
