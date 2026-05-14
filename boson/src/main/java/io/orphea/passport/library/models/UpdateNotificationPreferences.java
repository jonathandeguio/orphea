package io.orphea.passport.library.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNotificationPreferences {
    private boolean mention;
    private boolean subscription;
    private boolean accessManager;
}
