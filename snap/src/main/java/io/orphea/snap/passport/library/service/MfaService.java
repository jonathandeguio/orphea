package io.orphea.snap.passport.library.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class MfaService {

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    // Generate secret key for the user and return GoogleAuthenticatorKey
    public GoogleAuthenticatorKey generateSecretKey() {
        return googleAuthenticator.createCredentials();
    }

    // Generate Base64 QR Code Image for MFA
    public String getQRCodeUrl(GoogleAuthenticatorKey key, String userEmail) throws Exception {
        // Ensure correct format for the otpauth URL
        String otpAuthURL = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                "Snap",  // Issuer (your app's name)
                userEmail,  // User email or account identifier
                key.getKey(),  // The generated secret key
                "Snap"  // Issuer again
        );

        // Return Base64 QR code image
        return generateQRCodeImage(otpAuthURL);
    }

    // Generate QR code image from text and return it as a Base64 string
    private String generateQRCodeImage(String barcodeText) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        // Encode as Base64 string and return
        return Base64.getEncoder().encodeToString(pngData);
    }

    // Verify OTP entered by the user
    public boolean verifyCode(String secretKey, int code) {
        return googleAuthenticator.authorize(secretKey, code);
    }
}
