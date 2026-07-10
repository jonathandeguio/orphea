package io.movetodata.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "dashboard_filter")
public class DashboardFilterModel {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID datasetId;
    private UUID datasetName;
    private String columnName;
    private String filterType;
    private String name;

    @CreationTimestamp
    private Date createdAt = new Date();
    @LastModifiedDate
    private Date updatedAt = new Date();
}
