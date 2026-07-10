package io.movetodata.dataset.library.DTOs;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterDTO {
    private UUID id;

    private String key;

    @NotNull
    private String operator;

    @NotNull
    private String value;


    public FilterDTO(FilterDTO other) {
        this.id = other.id;
        this.key = other.key;
        this.operator = other.operator;
        this.value = other.value;
    }
}
