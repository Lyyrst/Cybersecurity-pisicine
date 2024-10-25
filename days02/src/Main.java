import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class Main {
    private static final Path OUTPUT_FILE_PATH = Path.of("ft_otp.key");
    private static final String SECRET_KEY = "1234567890123456";

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new Error("Error: Invalid argument: -h for help");
        }

        switch (args[0]) {
            case "-g":
                String key = getKeyFromArgs(args[1]).trim();
                if (isValidKey(key)) {
                    try {
                        Files.write(OUTPUT_FILE_PATH, encryptKey(key).getBytes());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new Error("Invalid key");
                }
                break;
            case "-k":
                try {
                    String decryptedKey = decryptKey(getKeyFromArgs(args[1]).trim());
                    System.out.println(decryptedKey);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case "-h":
                printUsage();
                System.exit(0);
            default:
                throw new Error("Error: Invalid argument: -h for help");
        }
    }

    private static String getKeyFromArgs(String keyArgument) {
        File keyFile = new File(keyArgument);
        if (keyFile.isFile()) {
            try {
                return new String(Files.readAllBytes(keyFile.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return keyArgument;
        }
    }

    private static boolean isValidKey(String key) {
        return key.length() >= 64 && key.matches("^[0-9A-Fa-f]+$");
    }

    private static String encryptKey(String key) throws Exception {
        byte[] encryptedBytes = initCipher(Cipher.ENCRYPT_MODE).doFinal(key.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String decryptKey(String encryptedKey) throws Exception {
        byte[] decryptedBytes = initCipher(Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(encryptedKey));
        return new String(decryptedBytes);
    }

    private static Cipher initCipher(int mode) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, secretKeySpec);
        return cipher;
    }

    private static void printUsage() {
        System.out.println("""
                === Usage: ===
                
                To store a key:
                ./ft_otp -g <hex_key>
                
                    -g: Saves a 64+ character hexadecimal key securely in 'ft_otp.key', encrypted.
                
                Example:
                    ./ft_otp -g 0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
                
                To generate a one-time password:
                ./ft_otp -k ft_otp.key
                
                    -k: Generates a 6-digit OTP using the key in 'ft_otp.key' based on HOTP (RFC 4226).
                
                Example:
                    ./ft_otp -k ft_otp.key
                
                Note:
                - The hex key must be at least 64 characters.
                - Keep 'ft_otp.key' secure.
                """);
    }
}