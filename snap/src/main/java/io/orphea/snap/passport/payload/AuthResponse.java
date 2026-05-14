package io.orphea.snap.passport.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String loginType;
    private String accessToken;
    private String tokenType = "Bearer";

}
