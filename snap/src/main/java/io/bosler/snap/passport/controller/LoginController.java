package io.bosler.snap.passport.controller;

import io.bosler.snap.passport.enums.LoginType;
import io.bosler.snap.passport.library.models.LoginHistory;
import io.bosler.snap.passport.library.models.User;
import io.bosler.snap.passport.library.repository.LoginHistoryRepository;
import io.bosler.snap.passport.library.repository.UserRepository;
import io.bosler.snap.passport.library.service.MfaService;
import io.bosler.snap.passport.payload.AuthResponse;
import io.bosler.snap.passport.payload.LoginRequest;
import io.bosler.snap.passport.security.TokenProvider;
import io.bosler.snap.platform.library.repository.PlatformConfigRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/passport")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private MfaService mfaService;
    @Autowired
    private PlatformConfigRepository platformConfigRepository;

    @PostMapping("/verify/{userAccount}/{password}/{otp}")
    public ResponseEntity<?> verifyOTP(@PathVariable("userAccount") String userAccount, @PathVariable("password") String password, @PathVariable("otp") int otp, HttpServletRequest request) {
        User user = userRepository.findByUsername(userAccount)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account: " + userAccount));

        // Retrieve the secret key for MFA verification
        String secretKey = (String) user.getMfaAttributes().get("secret");
        boolean isValid = mfaService.verifyCode(secretKey, otp);

        if (isValid) {
            // Step 1: Authenticate the user after successful MFA
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userAccount,
                            password
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Step 2: Generate the JWT token after successful MFA
            String token = tokenProvider.createToken(authentication);

            AuthResponse authResponse = new AuthResponse();
            authResponse.setLoginType(LoginType.MFA.getDisplayName());
            authResponse.setAccessToken(token);
            authResponse.setTokenType("bearer");

            updateLoginHistory(user, request, LoginType.MFA);

            // Step 3: Return the JWT token
            return ResponseEntity.ok(authResponse);
        }

        // Return an error if the OTP is invalid
        return new ResponseEntity<>("Invalid OTP", HttpStatus.FORBIDDEN);
    }

    @PostMapping("/verify/{userAccount}/{otp}")
    public ResponseEntity<?> verifyOTP(@PathVariable("userAccount") String userAccount, @PathVariable("otp") int otp) {
        User user = userRepository.findByUsername(userAccount)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account: " + userAccount));

        // Retrieve the secret key for MFA verification
        String secretKey = (String) user.getMfaAttributes().get("secret");
        boolean isValid = mfaService.verifyCode(secretKey, otp);

        if (isValid) {
            Map<String, Object> response = new HashMap<>();

            user.setIsMfaEnabled(true);
            Map<String, Object> mfaAttributes = user.getMfaAttributes();

            List<String> recoveryCodes = generateRecoveryCodes();

            List<String> hashedRecoveryCodes = recoveryCodes.stream()
                    .map(this::hashCode) // Hash each recovery code before storing
                    .collect(Collectors.toList());
            mfaAttributes.put("recoveryCodes", hashedRecoveryCodes); // Store hashed recovery codes
            user.setIsMfaEnabled(true);
            user.setMfaAttributes(mfaAttributes);
            userRepository.save(user);
            response.put("status", true);
            response.put("recoveryCodes", String.join(", ", recoveryCodes));  // Send plaintext recovery codes to the user

            // Step 1: Authenticate the user after successful MFA
            return ResponseEntity.ok(response);
        }

        // Return an error if the OTP is invalid
        return new ResponseEntity<>(false, HttpStatus.FORBIDDEN);
    }

    @PostMapping("/recovery/{userAccount}/{password}/{recoveryCode}")
    public ResponseEntity<?> verifyRecoveryCode(@PathVariable("userAccount") String userAccount, @PathVariable("recoveryCode") String recoveryCode, @PathVariable("password") String password, HttpServletRequest request) {
        // Step 1: Authenticate the user with username and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userAccount,
                        password
                )
        );

        User user = userRepository.findByUsername(userAccount)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account: " + userAccount));

        System.out.println(user.getMfaAttributes().get("recoveryCodes"));
        List<String> storedHashedRecoveryCodes = new ArrayList<>(
                scala.collection.JavaConverters.seqAsJavaListConverter(
                        (scala.collection.Seq<String>) user.getMfaAttributes().get("recoveryCodes")
                ).asJava()
        );

        String hashedRecoveryCode = hashCode(recoveryCode);

        if (storedHashedRecoveryCodes.contains(hashedRecoveryCode)) {
            user.setIsMfaEnabled(false);
            user.setMfaAttributes(null);
            userRepository.save(user);


            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.createToken(authentication);


            AuthResponse authResponse = new AuthResponse();
            authResponse.setLoginType(LoginType.MFA.getDisplayName());
            authResponse.setAccessToken(token);
            authResponse.setTokenType("bearer");

            updateLoginHistory(user, request, LoginType.MFA);

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "Recovery code accepted. MFA verification successful.");
            response.put("authResponse", authResponse);

            return ResponseEntity.ok(response);
        }

        return new ResponseEntity<>("Invalid recovery code", HttpStatus.FORBIDDEN);
    }


    private List<String> generateRecoveryCodes() {
        List<String> recoveryCodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // Generate 5 recovery codes
            recoveryCodes.add(UUID.randomUUID().toString().replace("-", "").substring(0, 8)); // Generate random 8-character code
        }
        return recoveryCodes;
    }

    private String hashCode(String code) {
        // Simple hash example using SHA-256, adjust as needed
        return DigestUtils.sha256Hex(code);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        // Step 1: Authenticate the user with username and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username : " + loginRequest.getUsername()));
        if (Objects.equals(loginRequest.getLoginType(), LoginType.SSO.getDisplayName())) {
            if (user == null) {
                return new ResponseEntity<>(false, HttpStatus.FORBIDDEN);
            }
        } else if (Objects.equals(loginRequest.getLoginType(), LoginType.PLAIN.getDisplayName())) {
            if (platformConfigRepository.existsByName("platformConfig") && platformConfigRepository.existsByMfaEnabled(true)) {
                if (user.getIsMfaEnabled()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("username", user.getUsername());
                    response.put("mfaEnabled", user.getIsMfaEnabled());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            }
            // If no MFA required, proceed with normal login
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.createToken(authentication);


            AuthResponse authResponse = new AuthResponse();
            authResponse.setLoginType(LoginType.PLAIN.getDisplayName());
            authResponse.setAccessToken(token);
            authResponse.setTokenType("bearer");

            updateLoginHistory(user, request, LoginType.PLAIN);


            // Return the JWT token
            return ResponseEntity.ok(authResponse);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    private void updateLoginHistory(User user, HttpServletRequest request, LoginType loginType) {
        user.setLastLoginAt(new Date());
        userRepository.save(user);

        // Log the login history
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setLoginType(loginType);
        loginHistory.setAgent(request.getHeader("User-Agent"));

        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        loginHistory.setRemoteAddr(ipAddress);
        loginHistory.setUserId(user.getId());
        loginHistory.setLastLoginAt(new Date());

        loginHistoryRepository.save(loginHistory);
    }
}
