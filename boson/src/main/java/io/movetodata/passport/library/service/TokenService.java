package io.movetodata.passport.library.service;

import io.movetodata.passport.library.models.TokenLongLived;
import org.jvnet.hk2.annotations.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface TokenService {

    List<TokenLongLived> getMyLongLivedTokens(UUID userId);

    TokenLongLived saveLongLivedToken(TokenLongLived tokenLongLived);
}
