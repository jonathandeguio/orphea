package io.orphea.kepler.library.models;

import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ManageElementUpdateRequest {
        private UUID tabId;
        private UUID elementId;
        private String type;
        private String position;
        @Lob
//        @Type(type = "org.hibernate.type.TextType")
        private String data;

}
