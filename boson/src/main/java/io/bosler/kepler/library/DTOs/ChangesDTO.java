package io.bosler.kepler.library.DTOs;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangesDTO {
    public String key;
    public boolean treat;

}
