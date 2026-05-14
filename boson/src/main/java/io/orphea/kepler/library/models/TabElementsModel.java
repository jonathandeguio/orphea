package io.orphea.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_tab_elements")
public class TabElementsModel {
    private @Id
    UUID id;
    private String type;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String data;

    private String position;
    private String datasetId;

    @ManyToOne
    @JoinColumn(name = "tabsId")
    @JsonBackReference
    private TabsModel tabsModel;
}
