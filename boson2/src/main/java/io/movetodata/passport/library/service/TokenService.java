package io.movetodata.passport.library.service;

import io.movetodata.passport.library.models.TokenLongLived;
import org.jvnet.hk2.annotations.Service;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public interface TokenService {

    List<TokenLongLived> getMyLongLivedTokens(UUID userId);

    TokenLongLived saveLongLivedToken(TokenLongLived tokenLongLived);
}
