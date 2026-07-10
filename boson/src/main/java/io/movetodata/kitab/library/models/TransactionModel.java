package io.movetodata.kitab.library.models;

import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.kitab.library.enums.TransactionStatus;
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
//@Table(name = "kitab_transactions", uniqueConstraints = {@UniqueConstraint(columnNames = {"datasetId", "branch"})})
@Table(name = "kitab_transactions")
public class TransactionModel {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    public BuildTrigger trigger;
    public TransactionStatus status;
    public UUID datasetId;
    public String branch;
    public String buildId;

    @CreationTimestamp
    public Date createdAt = new Date();
    public UUID createdBy;

    @LastModifiedDate
    public Date finishedAt = new Date();
    public UUID finishedBy;
}
