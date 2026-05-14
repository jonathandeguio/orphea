package io.orphea.dataset.library.models.DataHealth;

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
@Table(name = "data_health_log_model")
public class DataHealthLogModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    public UUID healthCheckId;
    public UUID datasetId;
    public String branch;

    public Boolean isPassed;
    public String message;
    public Boolean isCritical;

    public Date startedAt;
    public Date finishedAt;
}
