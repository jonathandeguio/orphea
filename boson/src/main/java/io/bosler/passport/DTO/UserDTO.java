package io.bosler.passport.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private UUID id;
    private String name;
    private String username;
    private String givenName;
    private String familyName;
    private String email;
    private String location;
    private String profileImage;
    private Date lastLoginAt;
}
