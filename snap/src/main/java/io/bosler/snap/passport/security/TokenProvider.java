package io.bosler.snap.passport.security;

import io.bosler.snap.config.AppProperties;
import io.bosler.snap.passport.library.models.TokenLongLived;
import io.bosler.snap.passport.library.models.User;
import io.bosler.snap.passport.library.repository.TokenRepository;
import io.bosler.snap.passport.library.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.Principal;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    private final AppProperties appProperties;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;

    public TokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String createToken(Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

        byte[] keyBytes = Decoders.BASE64.decode(appProperties.getAuth().getTokenSecret());
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject(String.valueOf(authUser.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
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
