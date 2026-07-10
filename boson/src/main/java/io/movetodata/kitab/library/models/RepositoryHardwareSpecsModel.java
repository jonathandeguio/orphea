package io.movetodata.kitab.library.models;

import io.movetodata.kitab.library.keys.HardwareSpecsKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_repository_hardware_specs")
@IdClass(HardwareSpecsKey.class)
public class RepositoryHardwareSpecsModel {

    @Id
    private UUID repository;
    @Id
    private String branch;
    @Id
    private String scriptPath;

    private int cores = 1;
    private String memory = "512m";
    private int numberOfExecutors = 1;
    private int failureRetries = 0;

    @CreationTimestamp
    private Date createdAt = new Date();
    @LastModifiedDate
    private Date updatedAt = new Date();

    private UUID createdBy;
    private UUID updatedBy;


}
