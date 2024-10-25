import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

/**
 * singletion for HOTP code generation.
 */
public class HotpGenerator {
    private static HotpGenerator instance;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HotpGenerator getInstance() {
        if (instance == null) {
            instance = new HotpGenerator();
        }
        return instance;
    }

    /**
     * Generate hotp code as a string.
     *
     * @param key the key
     * @return the code
     */
    public String generateHOTPcode(String key) {
        String verifiedKey = (key.length() % 2 == 0) ? key : key.substring(0, key.length() - 1);
        try {
            byte[] keyBytes = this.hexStringToByteArray(verifiedKey);
            long counter = System.currentTimeMillis() / 1000 / 30;
            byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();

            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKey keySpec = new SecretKeySpec(keyBytes, "HmacSHA1");
            mac.init(keySpec);

            byte[] hmac = mac.doFinal(counterBytes);
            int otp = this.dynamicTruncation(hmac) % 1_000_000;

            return String.format("%06d", otp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int dynamicTruncation(byte[] hmac) {
        int offset = hmac[hmac.length - 1] & 0x0F;
        return ((hmac[offset] & 0x7F) << 24)
                | ((hmac[offset + 1] & 0xFF) << 16)
                | ((hmac[offset + 2] & 0xFF) << 8)
                | (hmac[offset + 3] & 0xFF);
    }

    private byte[] hexStringToByteArray(String hex) {
        byte[] result = new byte[hex.length() / 2];
        IntStream.iterate(0, i -> i < hex.length(), i -> i + 2).forEach(i -> result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i + 1), 16)));
        return result;
    }

}
