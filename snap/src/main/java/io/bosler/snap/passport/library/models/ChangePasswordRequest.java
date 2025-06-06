package io.bosler.snap.passport.library.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChangePasswordRequest {
    String currentPassword;
    UUID userId;
    String password;
}
