package io.movetodata.passport.controllers;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.MfaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/passport/mfa")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class MfaController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MfaService mfaService;

    @PostMapping("/enable/{userAccount}")
    public ResponseEntity<Map<String, String>> enableMFA(@PathVariable("userAccount") String userAccount, HttpServletRequest request) {
        User user = userRepository.findByUsername(userAccount)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account: " + userAccount));

        GoogleAuthenticatorKey key = mfaService.generateSecretKey();

        String qrCodeBase64;
        try {
            qrCodeBase64 = mfaService.getQRCodeUrl(key, user.getEmail(), request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate QR code"));
        }

        Map<String, Object> mfaAttributes = new HashMap<>();
        mfaAttributes.put("secret", key.getKey());
        user.setMfaAttributes(mfaAttributes);
        userRepository.save(user);
        Map<String, String> response = new HashMap<>();
        response.put("qrCodeBase64", qrCodeBase64);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable/{userAccount}")
    public ResponseEntity<String> disableMFA(@PathVariable("userAccount") String userAccount) {
        User user = userRepository.findByUsername(userAccount)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account: " + userAccount));

        user.setMfaAttributes(null);
        user.setIsMfaEnabled(false);
        userRepository.save(user);

        return ResponseEntity.ok("MFA disabled successfully!");
    }
}
