package io.movetodata.passport.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
//import io.movetodata.config.AppProperties;
import io.movetodata.passport.library.models.Token;
import io.movetodata.passport.library.models.TokenLongLived;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.repository.TokenRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.GroupService;
import io.movetodata.passport.library.service.JwtService;
import io.movetodata.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin
@RestController
@RequestMapping("/api/passport/token")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class TokenController {

    @Autowired
    private TokenRepository tokenRepository;

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final GroupService groupService;

//    private AppProperties appProperties;


    @Operation(summary = "Create Long Lived Token")
    @PostMapping("/CreateLongLived")
    public void longLivedToken(HttpServletRequest request,
                               HttpServletResponse response, Principal principal,
                               @Valid @RequestBody Token tokenLongLived) throws IOException {

//        String access_token = tokenProvider.createLongLivedToken(principal, tokenLongLived);

        UUID userId = userService.getUser(principal.getName()).getId();

        var user = userRepository.findById(userId)
                .orElseThrow();
        var access_token = jwtService.generateToken(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", access_token);

        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);

    }

    @Operation(summary = "It provides list of all users tokens")
    @GetMapping("/GetLongLived")
    public ResponseEntity<List<Token>> getMyLongLivedTokens(HttpServletRequest request,
                                                            HttpServletResponse response,
                                                            Principal principal) {

        User user = userService.getUser(principal.getName());

        return ResponseEntity.ok().body(tokenRepository.getByUserId(user.id));
    }

}