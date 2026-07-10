package io.movetodata.passport.security;

import io.movetodata.config.AppProperties;
import io.movetodata.passport.library.models.TokenLongLived;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.repository.TokenRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.Principal;
import java.util.Date;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TokenProvider {
    @Autowired
    private AppProperties appProperties;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;

    public String createRefreshToken(Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return createToken(authUser.getId(), appProperties.getAuth().getRefreshTokenExpirationMsec(), appProperties.getAuth().getRefreshTokenSecret());
    }

    public String createAccessToken(Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return createToken(authUser.getId(), appProperties.getAuth().getTokenExpirationMsec(), appProperties.getAuth().getTokenSecret());
    }
    public String createAccessToken(UUID userId) {
        return createToken(userId, appProperties.getAuth().getTokenExpirationMsec(), appProperties.getAuth().getTokenSecret());
    }
    public String createRefreshToken(UUID userId) {
        return createToken(userId, appProperties.getAuth().getRefreshTokenExpirationMsec(), appProperties.getAuth().getRefreshTokenSecret());
    }

    private String createToken(UUID userId, long appProperties, String appProperties1) {

        Date expiryDate = new Date(new Date().getTime() + appProperties);

        byte[] keyBytes = Decoders.BASE64.decode(appProperties1);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public UUID getUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getRefreshTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    public UUID getUserIdFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateAccessToken(String authToken) {
        return verifyToken(appProperties.getAuth().getTokenSecret(), authToken);
    }

    public boolean validateRefreshToken(String refreshToken) {
        return verifyToken(appProperties.getAuth().getRefreshTokenSecret(), refreshToken);
    }

    private boolean verifyToken(String tokenSecret, String authToken) {
        try {
            Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    public String createLongLivedToken(Principal principal, TokenLongLived tokenLongLived) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username : " + principal.getName())
                );


        // First add the token label and expiration to db
        tokenLongLived.setUserId(user.id);
        tokenRepository.save(tokenLongLived);

        byte[] keyBytes = Decoders.BASE64.decode(appProperties.getAuth().getTokenSecret());
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(tokenLongLived.getExpiration())
                .signWith(key)
                .compact();
    }

    public String buildToken(UUID userId, Date expiration) {

        byte[] keyBytes = Decoders.BASE64.decode(appProperties.getAuth().getTokenSecret());
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

}
