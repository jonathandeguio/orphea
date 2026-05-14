package io.orphea.passport.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.orphea.passport.security.AuthUser;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class AuthUtils {
    private AuthUtils() {}

    public static String getAccessTokenFromRequest(HttpServletRequest request) {
        return getTokenFromRequest(request, "bAT");
    }

    public static String getRefreshTokenFromRequest(HttpServletRequest request) {
        return getTokenFromRequest(request, "bRT");
    }
    private static @Nullable String getTokenFromRequest(HttpServletRequest request, String bAT) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (bAT.equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
        }
        return null;
    }

    public static Date getTTLFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getExpiresAt();
    }

    public static AuthUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
            return (AuthUser) authentication.getPrincipal();
        }
        return null;
    }
}
