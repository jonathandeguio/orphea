package io.movetodata.build.library.models;

import io.movetodata.build.BobEnums.BuildLanguage;
import io.movetodata.build.library.enums.WriteModeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "build_specifications")
public class BuildSpecification {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    public UUID repository;
    public String scriptPath;
    public String fileName;
    public String lineNo;
    @Enumerated(EnumType.STRING)
    public BuildLanguage language;
    public String branchId;
    public String commitId;

    public UUID datasetId;
    public String branch;
    public UUID transactionId;
    @Enumerated(EnumType.STRING)
    public WriteModeEnum writeMode = WriteModeEnum.SNAPSHOT;

    public UUID buildId;

    @CreationTimestamp
    public Date createdAt = new Date();
    public UUID createdBy;

    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID updatedBy;
}
