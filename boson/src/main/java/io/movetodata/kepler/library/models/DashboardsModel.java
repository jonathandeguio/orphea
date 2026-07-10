package io.movetodata.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.movetodata.kitab.library.models.IResource;
import io.movetodata.sharedutils.MoveToDataUtils;
import io.movetodata.sharedutils.Serializers.ResourceSerializer;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "kepler_dashboards")
@IdClass(DashboardKey.class)
@JsonSerialize(using = ResourceSerializer.class)
public class DashboardsModel implements MoveToDataUtils, IResource {
    @Id
    private UUID id;
    @Id
    private Long versionId;
    private Date lastVersionedDate;

    @OneToMany(mappedBy = "dashboardsModel", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OrderBy("tab_order ASC")
    private List<TabsModel> tabs;

    @ManyToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    @JsonManagedReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Builder.Default
    private Set<ChartsModel> charts = new HashSet<>();

    // TODO BRANCH : remove this no need of brnach in dashboard and do sanity
    @Builder.Default
    private String branch = "master";
}
