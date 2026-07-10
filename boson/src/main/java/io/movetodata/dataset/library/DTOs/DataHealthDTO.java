package io.movetodata.dataset.library.DTOs;

import io.movetodata.dataset.library.enums.DataHealthTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.UUID;

@Getter
@Setter
public class DataHealthDTO {
    public String rule;
    public boolean isEnabled;
    public String groups;
    public String notes;
    public boolean issue;
    private UUID id;
    @Enumerated(EnumType.STRING)
    private DataHealthTypeEnum dataHealthType;
}
