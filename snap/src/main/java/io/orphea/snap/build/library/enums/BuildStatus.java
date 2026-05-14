package io.orphea.snap.build.library.enums;

import lombok.Getter;

@Getter
public enum BuildStatus {
    ABORTED("aborted"),
    
    ACTIVE("active"),
    ERROR("error"),
    FAILED("failed"),

    SUCCESS("success"),
    DELETED("deleted");

    private final String displayName;

    BuildStatus(String displayName) {
        this.displayName = displayName;
    }

}
