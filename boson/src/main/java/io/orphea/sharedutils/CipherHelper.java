package io.orphea.sharedutils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherHelper {
    static KeyGenerator keyGenerator;

    static byte[] ivB = new byte[16];

    static {
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            new SecureRandom().nextBytes(ivB);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] decodedKey = Base64.getDecoder().decode(System.getenv("SECRET_KEY"));
    static final SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

    static final IvParameterSpec iv = new IvParameterSpec(ivB);
    static final String algorithm = "AES/CBC/PKCS5Padding";

    public static String encryptString(String input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    public static String decryptString(String cipherText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(cipherText));
        return new String(plainText);
    }

    public static byte[] encrypt(byte[] input) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return encryptString(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }
    public static byte[] decrypt(byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException  {
        return decryptString(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    public static String generateDecryptedFile(String datasetPath) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        File datasetFolder = new File(datasetPath);
        if(datasetFolder.isDirectory()) {
            String tempfoldername = datasetPath + "/tmp";
            File tmpFolder = new File(tempfoldername);
            tmpFolder.mkdir();
            File[] listFiles = datasetFolder.listFiles();

            for (File file : listFiles) {
                if(file.isDirectory())
                    continue;

                byte[] encBytes = Files.readAllBytes(file.toPath());
                byte[] decryptedBytes = CipherHelper.decrypt(encBytes);

                File decryptedFile = new File(tempfoldername + "/" + file.getName().substring(0, file.getName().length() - 4));

                try (FileOutputStream fos = new FileOutputStream(decryptedFile)) {
                    fos.write(decryptedBytes);
                }
            }
            return datasetPath + "/tmp";
        }
        return null;
    }
    public static void deleteDecryptedFiles(String path) {
        File folder = new File(path);
        if(folder.isDirectory()) {
            for(File f: folder.listFiles()) {
                f.delete();
            }
        }
        folder.delete();
    }
}
