package io.movetodata.zoro.library.models;

import io.movetodata.passport.library.models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.spark.sql.types.StructType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_dataset_schema", uniqueConstraints = {@UniqueConstraint(columnNames = {"datasetId", "branch"})})
public class SchemaModel {

    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;


    private UUID datasetId;
    private String branch;

    private StructType schema;
    // private StructType renameSchema;

    @OneToOne(targetEntity = CustomSchemaModel.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "rg_fk", referencedColumnName = "id")
    private CustomSchemaModel customSchema;

    private String status;

    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
