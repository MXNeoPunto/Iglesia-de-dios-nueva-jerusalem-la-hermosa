package iglesia.jerusalem.hermosa;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CryptoUtilsTest {
    @Test
    public void testDecryption() {
        String expectedUrl = "https://app0102.sonicpanelradio.com/8070/stream";
        String decryptedUrl = CryptoUtils.getStreamUrl();
        assertEquals(expectedUrl, decryptedUrl);
    }
}
