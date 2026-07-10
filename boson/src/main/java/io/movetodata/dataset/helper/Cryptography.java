package io.movetodata.dataset.helper;

public class Cryptography {
//    private final String secretKey = System.getenv("SECRET_KEY");
//
//    FileEncrypterDecrypter(SecretKey secretKey, String transformation) {
//        this.secretKey = secretKey;
//        this.cipher = Cipher.getInstance(transformation);
//    }
//
//    void encrypt(String content, String fileName) {
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//        byte[] iv = cipher.getIV();
//
//        try (FileOutputStream fileOut = new FileOutputStream(fileName);
//             CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)) {
//            fileOut.write(iv);
//            cipherOut.write(content.getBytes());
//        }
//    }
//
//    String decrypt(String fileName) {
//        String content;
//
//        try (FileInputStream fileIn = new FileInputStream(fileName)) {
//            byte[] fileIv = new byte[16];
//            fileIn.read(fileIv);
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fileIv));
//
//            try (
//                    CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher);
//                    InputStreamReader inputReader = new InputStreamReader(cipherIn);
//                    BufferedReader reader = new BufferedReader(inputReader)
//            ) {
//
//                StringBuilder sb = new StringBuilder();
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    sb.append(line);
//                }
//                content = sb.toString();
//            }
//
//        }
//        return content;
//    }
}
