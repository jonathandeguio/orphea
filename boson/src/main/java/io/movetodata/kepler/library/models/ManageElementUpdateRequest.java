package io.movetodata.kepler.library.models;

import io.movetodata.kepler.enums.TabElementOperationEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Lob;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ManageElementUpdateRequest {
    private UUID dashboardId;
    private UUID tabId;
    private UUID elementId;
    private String type;
    private String position;
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String data;
    private TabElementOperationEnum operation = TabElementOperationEnum.NONE;
}
