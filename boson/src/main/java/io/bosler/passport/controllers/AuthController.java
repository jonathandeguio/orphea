package io.bosler.passport.controllers;

import io.bosler.passport.enums.LoginType;
import io.bosler.passport.library.models.LoginHistory;
import io.bosler.passport.library.models.User;
import io.bosler.passport.library.repository.LoginHistoryRepository;
import io.bosler.passport.library.repository.UserRepository;
import io.bosler.passport.library.service.MfaService;
import io.bosler.passport.payload.AuthResponse;
import io.bosler.passport.payload.LoginRequest;
import io.bosler.passport.payload.MfaRequest;
import io.bosler.passport.security.TokenProvider;
import io.bosler.passport.util.AuthUtils;
import io.bosler.platform.library.repository.PlatformConfigRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/passport")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private PlatformConfigRepository platformConfigRepository;
    @Autowired
    private MfaService mfaService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOTP(@RequestBody MfaRequest mfaRequest, HttpServletRequest request) {
        User user = userRepository.findByUsername(mfaRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account: " + mfaRequest.getUsername()));

        // Retrieve the secret key for MFA verification
        String secretKey = (String) user.getMfaAttributes().get("secret");
        boolean isValid = mfaService.verifyCode(secretKey, mfaRequest.getOtp());

        if (isValid) {
            // Step 1: Authenticate the user after successful MFA
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            mfaRequest.getUsername(),
                            mfaRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = tokenProvider.createAccessToken(authentication);
            String refreshToken = tokenProvider.createRefreshToken(authentication);

            HttpHeaders headers = getAuthHeaders(accessToken, refreshToken, request.getScheme());

            AuthResponse authResponse = new AuthResponse();
            authResponse.setLoginType(LoginType.MFA.getDisplayName());
            authResponse.setAccessToken(accessToken);
            authResponse.setTokenType("bearer");

            updateLoginHistory(user, request, LoginType.MFA);

            // Step 3: Return the JWT token
            return new ResponseEntity<>(authResponse, headers, HttpStatus.OK);
        }

        // Return an error if the OTP is invalid
        return new ResponseEntity<>("Invalid OTP", HttpStatus.FORBIDDEN);
    }

    @PostMapping("/verify-internal")
    public ResponseEntity<?> verifyOTP(@RequestBody MfaRequest mfaRequest) {
        User user = userRepository.findByUsername(mfaRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account: " + mfaRequest.getUsername()));

        // Retrieve the secret key for MFA verification
        String secretKey = (String) user.getMfaAttributes().get("secret");
        boolean isValid = mfaService.verifyCode(secretKey, mfaRequest.getOtp());

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

    @PostMapping("/recovery")
    public ResponseEntity<?> verifyRecoveryCode(@RequestBody MfaRequest mfaRequest, HttpServletRequest request) {
        // Step 1: Authenticate the user with username and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        mfaRequest.getUsername(),
                        mfaRequest.getPassword()
                )
        );

        User user = userRepository.findByUsername(mfaRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account: " + mfaRequest.getUsername()));

        System.out.println(user.getMfaAttributes().get("recoveryCodes"));
        List<String> storedHashedRecoveryCodes = new ArrayList<>(
                scala.collection.JavaConverters.seqAsJavaListConverter(
                        (scala.collection.Seq<String>) user.getMfaAttributes().get("recoveryCodes")
                ).asJava()
        );

        String hashedRecoveryCode = hashCode(mfaRequest.getRecoveryCode());

        if (storedHashedRecoveryCodes.contains(hashedRecoveryCode)) {
            user.setIsMfaEnabled(false);
            user.setMfaAttributes(null);
            userRepository.save(user);


            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = tokenProvider.createAccessToken(authentication);
            String refreshToken = tokenProvider.createRefreshToken(authentication);

            HttpHeaders headers = getAuthHeaders(accessToken, refreshToken, request.getScheme());


            AuthResponse authResponse = new AuthResponse();
            authResponse.setLoginType(LoginType.MFA.getDisplayName());
            authResponse.setAccessToken(accessToken);
            authResponse.setTokenType("bearer");

            updateLoginHistory(user, request, LoginType.MFA);

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "Recovery code accepted. MFA verification successful.");
            response.put("authResponse", authResponse);

            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }

        return new ResponseEntity<>("Invalid recovery code", HttpStatus.FORBIDDEN);
    }

    @PostMapping("refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = AuthUtils.getRefreshTokenFromRequest(request);
        if(tokenProvider.validateRefreshToken(refreshToken)) {
            UUID uuid = tokenProvider.getUserIdFromRefreshToken(refreshToken);
            String accessToken = tokenProvider.createAccessToken(uuid);
            String newRefreshToken = tokenProvider.createRefreshToken(uuid);

            HttpHeaders headers = getAuthHeaders(accessToken, newRefreshToken, request.getScheme());

            return new ResponseEntity<>("OK", headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        HttpHeaders headers = getAuthHeaders("", "", request.getScheme());
        return new ResponseEntity<>("OK", headers, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {

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
                if(user.getIsMfaEnabled() != null && user.getIsMfaEnabled()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("username", user.getUsername());
                    response.put("mfaEnabled", user.getIsMfaEnabled());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = tokenProvider.createAccessToken(authentication);
            String refreshToken = tokenProvider.createRefreshToken(authentication);

            HttpHeaders headers = getAuthHeaders(accessToken, refreshToken, request.getScheme());

            user.setLastLoginAt(new Date());

            userRepository.save(user);

            AuthResponse authResponse = new AuthResponse();
            authResponse.setLoginType(LoginType.PLAIN.getDisplayName());
            authResponse.setAccessToken(accessToken);
            authResponse.setTokenType("bearer");

            updateLoginHistory(user, request, LoginType.PLAIN);

            return new ResponseEntity<>(authResponse, headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.OK);
    }

    public static @NotNull HttpHeaders getAuthHeaders(String accessToken, String refreshToken, String protocol) {
        HttpHeaders headers = new HttpHeaders();
        // Manually construct the cookie header string
        StringJoiner refreshTokenCookieString = new StringJoiner("; ");
        refreshTokenCookieString.add("bRT="+refreshToken);
        refreshTokenCookieString.add("Path=/");

        StringJoiner accessTokenCookieString = new StringJoiner("; ");
        accessTokenCookieString.add("bAT="+accessToken);
        accessTokenCookieString.add("Path=/");

        if(protocol.equals("https")) {
            accessTokenCookieString.add("SameSite=None");
            accessTokenCookieString.add("Secure=true");

            refreshTokenCookieString.add("SameSite=None");
            refreshTokenCookieString.add("Secure=true");
        } else {
            accessTokenCookieString.add("SameSite=None");
            accessTokenCookieString.add("Secure=false");

            refreshTokenCookieString.add("SameSite=None");
            refreshTokenCookieString.add("Secure=false");
        }

        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookieString.toString());
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookieString.toString());
        return headers;
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

    private void updateLoginHistory(User user, HttpServletRequest request, LoginType loginType) {
        user.setLastLoginAt(new Date());
        userRepository.save(user);

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
