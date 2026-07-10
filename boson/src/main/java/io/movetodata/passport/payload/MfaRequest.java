package io.movetodata.passport.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MfaRequest {
    private String username;
    private String password;
    private int otp;
    private String recoveryCode;

}

