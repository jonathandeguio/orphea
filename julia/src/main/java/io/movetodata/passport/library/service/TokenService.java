package io.movetodata.passport.library.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class TokenService {

    public String accessToken(HttpServletRequest request, HttpServletResponse response,
                            @AuthenticationPrincipal OAuth2User principal) throws IOException {

        Algorithm algorithm = Algorithm.HMAC256(System.getenv("JWT_SECRET").getBytes());
        String access_token = JWT.create()
                .withSubject(principal.getAttribute("login"))
                .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 10000))
                .withIssuer(request.getRequestURL().toString())
//                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);

        return access_token;
    }
}
