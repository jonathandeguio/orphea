package io.orphea.passport.library.models;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterRequest {

  private String givenName;
  private String familyName;
  private String email;
  private String password;
}
