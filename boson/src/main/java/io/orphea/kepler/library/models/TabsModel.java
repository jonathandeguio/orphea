package io.orphea.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.*;

@Entity
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
    private int tabOrder;

    @ManyToOne()
    @JoinColumns({
            @JoinColumn(name = "dashboardId_FK"),
            @JoinColumn(name = "versionId_FK")
    })
    @JsonBackReference
    private DashboardsModel dashboardsModel;
    //    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.REMOVE, CascadeType.REFRESH})
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<ChartsModel> chartsForTabs = new HashSet<>();

    @OneToMany(mappedBy = "tabsModel", cascade = CascadeType.ALL)
    private List<TabElementsModel> tabElements = new ArrayList<>();

    // We don't define these properties default value, so that they can pick system default color based on light/dark mode
    private String chartHeadingTextColor;
    private String chartHeadingBg;
    private String chartBodyBg;
    private String canvasBg;
    private String pageBg;

    private int topPadding = 2;
    private int rightPadding = 2;
    private int bottomPadding = 2;
    private int leftPadding = 2;

    private boolean preventCollision = false;
    private boolean allowOverlap = true;

    @CreationTimestamp
    private Date createdAt = new Date();
    @LastModifiedDate
    private Date updatedAt = new Date();
    private UUID createdBy;
    private UUID updatedBy;
}
