package calculadora.iva.gt;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CryptoUtilsTest {
    @Test
    public void testDecryption() {
        String expectedUrl = "http://edmenstudio.net:8001/;";
        String decryptedUrl = CryptoUtils.getStreamUrl();
        assertEquals(expectedUrl, decryptedUrl);
    }
}
