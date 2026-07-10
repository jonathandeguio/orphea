package io.movetodata.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "kepler_charts")
public class ChartsModel {
    private @Id
    UUID id;
    private String name;
    private String description;
    private UUID parent;
    private UUID datasetId;
    private String branch;

    @OneToOne(targetEntity = ChartConfigModel.class, cascade = CascadeType.ALL)
    private ChartConfigModel chartConfig;

    @OneToOne(targetEntity = ChartCustomizeModel.class, cascade = CascadeType.ALL)
    private ChartCustomizeModel chartCustomize;

    @ManyToMany()
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonBackReference
    private Set<DashboardsModel> dashboard = new HashSet<>();

    @ManyToMany(mappedBy = "chartsForTabs")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonBackReference
    private Set<TabsModel> tabsForCharts = new HashSet<>();

    @CreationTimestamp
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
