package io.movetodata.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_tab_elements")
public class TabElementsModel {

    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    private String type;

    @Lob
//    @Type(type = "org.hibernate.type.TextType")
//    @Column(columnDefinition = "text")
    private String data;

    private String position;
    private String datasetId;

    @ManyToOne()
    @JoinColumn(name = "tabsId")
    @JsonBackReference
    private TabsModel tabsModel;

}
