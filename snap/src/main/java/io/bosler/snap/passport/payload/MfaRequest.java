package io.bosler.snap.passport.payload;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class MfaRequest {
    @NotNull
    private String username;
    @NotNull
    private String mfaCode;
}
