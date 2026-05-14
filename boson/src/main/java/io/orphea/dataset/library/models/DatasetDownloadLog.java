package io.orphea.dataset.library.models;


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
@Table(name = "dataset_download_log")
public class DatasetDownloadLog {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @CreatedDate
    public Date downloadedAt;
    public UUID downloadedBy;
    UUID datasetId;
    String branch;
    Integer numberOfRows;
    Long size;
}
