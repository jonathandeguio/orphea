//package io.orphea.passport.security;
//
//;
//import io.orphea.passport.library.models.Token;
//import io.orphea.passport.library.models.User;
//import io.orphea.passport.library.repository.TokenRepository;
//import io.orphea.passport.library.repository.UserRepository;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.security.Key;
//import java.security.Principal;
//import java.util.Date;
//import java.util.UUID;
//
//@Service
//public class TokenProvider {
//
//    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
//
////    private AppProperties appProperties;
//
//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    TokenRepository tokenRepository;
//
////    @Autowired
////    TokenService tokenService;
//
////    public TokenProvider(AppProperties appProperties) {
////        this.appProperties = appProperties;
////    }
//
//    public String createToken(Authentication authentication) {
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + 10000);
//
//        byte[] keyBytes = Decoders.BASE64.decode("appProperties.getAuth().getTokenSecret()");
//        Key key = Keys.hmacShaKeyFor(keyBytes);
//
//        return Jwts.builder()
//                .setSubject(String.valueOf(userPrincipal.getId()))
//                .setIssuedAt(new Date())
//                .setExpiration(expiryDate)
//                .signWith(key)
//                .compact();
//    }
//
//    public UUID getUserIdFromToken(String token) {
//        Claims claims = Jwts.parser()
//                .setSigningKey("appProperties.getAuth().getTokenSecret()")
//                .parseClaimsJws(token)
//                .getBody();
//
//        return UUID.fromString(claims.getSubject());
//    }
//
//    public boolean validateToken(String authToken) {
//        try {
//            Jwts.parser().setSigningKey("appProperties.getAuth().getTokenSecret()").parseClaimsJws(authToken);
//            return true;
//        } catch (SignatureException ex) {
//            logger.error("Invalid JWT signature");
//        } catch (MalformedJwtException ex) {
//            logger.error("Invalid JWT token");
//        } catch (ExpiredJwtException ex) {
//            logger.error("Expired JWT token");
//        } catch (UnsupportedJwtException ex) {
//            logger.error("Unsupported JWT token");
//        } catch (IllegalArgumentException ex) {
//            logger.error("JWT claims string is empty.");
//        }
//        return false;
//    }
//
//    public String createLongLivedToken(Principal principal, Token tokenLongLived) {
//
//        User user = userRepository.findByUsername(principal.getName())
//                .orElseThrow(() ->
//                        new UsernameNotFoundException("User not found with username : " + principal.getName())
//                );
//
//
//        // First add the token label and expiration to db
////        tokenLongLived.setUserId(user.id);
//        tokenRepository.save(tokenLongLived);
//
//        byte[] keyBytes = Decoders.BASE64.decode("appProperties.getAuth().getTokenSecret()");
//        Key key = Keys.hmacShaKeyFor(keyBytes);
//
//        return Jwts.builder()
//                .setSubject(String.valueOf(user.id))
//                .setIssuedAt(new Date())
//                .setExpiration(new Date())  // TODO :  Spring3 need resolving
//                .signWith(key)
//                .compact();
//    }
//
//    public String buildToken(UUID userId, Date expiration) {
//
//        byte[] keyBytes = Decoders.BASE64.decode("appProperties.getAuth().getTokenSecret()");
//        Key key = Keys.hmacShaKeyFor(keyBytes);
//
//        return Jwts.builder()
//                .setSubject(String.valueOf(userId))
//                .setIssuedAt(new Date())
//                .setExpiration(expiration)
//                .signWith(key)
//                .compact();
//    }
//
//}
