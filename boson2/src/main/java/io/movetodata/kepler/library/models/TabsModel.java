package io.movetodata.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.*;

@Entity(name = "kepler_tabs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_tabs")
public class TabsModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dashboardId")
    @JsonBackReference
    private DashboardsModel dashboardsModel;
//    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.REMOVE, CascadeType.REFRESH})
    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonManagedReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<ChartsModel> chartsForTabs = new HashSet<>();

    @OneToMany(mappedBy = "tabsModel")
    private List<TabElementsModel> tabElements = new ArrayList<>();


    @CreationTimestamp
    private Date createdAt = new Date();
    @LastModifiedDate
    private Date updatedAt = new Date();
    private UUID createdBy;
    private UUID updatedBy;

}
