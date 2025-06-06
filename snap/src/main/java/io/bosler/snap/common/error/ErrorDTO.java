package io.bosler.snap.common.error;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorDTO {
    private String name;
    private String message;
    private String stack;
    private String componentStack;
}
