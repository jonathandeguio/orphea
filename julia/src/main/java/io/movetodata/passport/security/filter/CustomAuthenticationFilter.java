package io.movetodata.passport.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.movetodata.passport.library.models.LoginHistory;
import io.movetodata.passport.library.models.Users;
import io.movetodata.passport.library.repository.LoginHistoryRepo;
import io.movetodata.passport.library.repository.UserRepo;
import io.movetodata.passport.library.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
//@RequiredArgsConstructor
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private static String access_token;
    private final LoginHistoryRepo loginHistoryRepo;

    private final UserService userService;
    private final UserRepo userRepo;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, LoginHistoryRepo loginHistoryRepo, UserService userService, UserRepo userRepo) {
        this.authenticationManager = authenticationManager;
        this.loginHistoryRepo = loginHistoryRepo;
        this.userService = userService;
        this.userRepo = userRepo;
    }
    public static String getAccess_token () {
        return access_token;
    }

    @Override
    //grabbing info and passing it to UsernamePasswordAuthenticationToken
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        log.info("Username is: {}", username); log.info("Password is: {}", password);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,password);

        return authenticationManager.authenticate(authenticationToken);
    }

//    @Value( "${movetodata.secret}" )
//    public String movetodataSecret;

    @Override
    //if successful, then , this method will be called
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {

        //super.successfulAuthentication(request, response, chain, authResult);
        //giving access token

        User user = (User)authentication.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256(System.getenv("JWT_SECRET").getBytes());
        access_token = JWT.create()
                .withSubject(user.getUsername())
//                .withExpiresAt(new Date(System.currentTimeMillis() + 1000*60*10000))  // 6.94 days
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000*60*60*8))  // 8 hours
//                .withExpiresAt(new Date(System.currentTimeMillis() + 1000*60))  // 1  min for testing
                .withIssuer(request.getRequestURL().toString())
//                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);
        //giving refresh token
        String refresh_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 30*60*1000))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);
        /*1when user logins successfully , then , they may have the respective token */
        response.setHeader("access_token", access_token);
        response.setHeader("refresh_token", refresh_token);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);
        response.setContentType(APPLICATION_JSON_VALUE);


        LoginHistory loginHistory = new LoginHistory();
        Users userId = userService.getUser(user.getUsername());

        userId.setLastLoginAt(new Date());

        userRepo.save(userId);

        loginHistory.setAgent(request.getHeader("User-Agent"));

        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        loginHistory.setRemoteAddr(ipAddress);

        loginHistory.setUserId(userId.getId());
        loginHistory.setLastLoginAt(new Date());

        loginHistoryRepo.save(loginHistory);

        new ObjectMapper().writeValue(response.getOutputStream(), tokens);

    }
}