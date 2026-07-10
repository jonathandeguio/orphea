package io.movetodata.comments.library.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor

public class CommentRequestModel {
    private UUID resourceId;
    private UUID parent;
    private String message;
    private String status;
}
