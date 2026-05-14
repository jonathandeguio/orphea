package io.orphea.snap.sharedutils.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

enum Fallback {
    POPUP,
    ERROR_PAGE,
}


@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDTO {
    private Long timestamp;
    private Integer status;

    @NotNull
    private String error;
    @NotNull
    private String description;
    @NotNull
    private Fallback fallback;

    public ErrorDTO(Integer status, String error) {
        this.error = error;
        this.status = status;
    }

    public ErrorDTO(Integer status, String error, String description) {
        this.error = error;
        this.status = status;
        this.description = description;
    }

    @Override
    public String toString() {
        return "ErrorDTO{" +
                "status=" + status +
                ", timestamp=" + timestamp +
                ", error='" + error + '\'' +
                ", fallback=" + fallback +
                ", description='" + description + '\'' +
                '}';
    }
}

