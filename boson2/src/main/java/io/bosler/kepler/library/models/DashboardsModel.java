package io.bosler.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "kepler_dashboards")
public class DashboardsModel {

    private @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    private String name;
    private String description;
    private String type = "dashboard";

    private UUID parent;

    @OneToMany(mappedBy = "dashboardsModel", cascade=CascadeType.ALL)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<TabsModel> tabs;

    @ManyToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    @JsonManagedReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<ChartsModel> charts = new HashSet<>();

    private String branch = "master";

    @OneToMany(targetEntity = DashboardFilterModel.class, cascade = CascadeType.ALL)
    @JsonManagedReference
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnoreProperties(ignoreUnknown=true)
    private List<DashboardFilterModel> filters;

    @CreationTimestamp
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

}
