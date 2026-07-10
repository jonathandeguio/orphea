package io.movetodata.logger.library.models;

import io.movetodata.logger.library.enums.BosonComponent;
import io.movetodata.logger.library.enums.LogType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogTagModel {
    private UUID traceId;
    @Enumerated(EnumType.STRING)
    private BosonComponent componentName;
    @Enumerated(EnumType.STRING)
    private LogType level;
    private String message;
    private String uri;
    private List<Object> payload;

}
