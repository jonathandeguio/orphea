package io.movetodata.passport.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

public class AuthUtils {

    public static String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

//    public static long getTTLFromJwt(String jwt) {
//
//        Claims claims = Jwts.parserBuilder()
//                .build()
//                .parse(jwt);
//
//        long expirationTime = claims.getExpiration().getTime();
//        long currentTime = System.currentTimeMillis();
//        long ttl = expirationTime - currentTime;
//        return ttl;
//    }

//    public static Date getTTLFromJwt(String token) {
//
//        // TODO : Spring 3 fix
//        DecodedJWT jwt = JWT.decode(token);
//        return jwt.getExpiresAt();
//
//        return new Date();
//
////        if (jwt.getExpiresAt().before(new Date())) {
////            System.out.println("token is expired");
////        }
//    }
}
