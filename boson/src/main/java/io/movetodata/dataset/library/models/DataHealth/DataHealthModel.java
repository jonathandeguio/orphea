package io.movetodata.dataset.library.models.DataHealth;

import io.movetodata.dataset.library.enums.DataHealthTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "data_health_model")
public class DataHealthModel {
    public String rule;
    public String groups;
    public String notes;
    public boolean issue;
    public boolean isEnabled;

    public Date createdAt;
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID datasetId;
    private String branch;

    @Enumerated(EnumType.STRING)
    private DataHealthTypeEnum dataHealthType;
}
