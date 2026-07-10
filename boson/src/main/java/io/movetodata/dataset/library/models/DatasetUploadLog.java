package io.movetodata.dataset.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "dataset_upload_log")
public class DatasetUploadLog {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @CreatedDate
    public Date uploadedAt;
    public UUID uploadedBy;
    UUID datasetId;
    String branch;
}
