package io.movetodata.build.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SocketMessage {
    private String message;
    private String information;

    public SocketMessage(String newPreviewLog) {
        this.message = newPreviewLog;
    }
}
