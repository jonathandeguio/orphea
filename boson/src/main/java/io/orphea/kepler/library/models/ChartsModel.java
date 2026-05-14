package io.orphea.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.orphea.kitab.library.models.IResource;
import io.orphea.sharedutils.OrpheaUtils;
import io.orphea.sharedutils.Serializers.ResourceSerializer;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "kepler_charts")
@IdClass(ChartKey.class)
@JsonSerialize(using = ResourceSerializer.class)
public class ChartsModel implements OrpheaUtils, IResource {
    @Id
    private UUID id;
    @Id
    private Long versionId;
    private Date lastVersionedDate;

    private UUID datasetId;
    private String branch;

    @OneToOne(targetEntity = ChartConfigModel.class, cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private ChartConfigModel chartConfig;

    //    @OneToOne(targetEntity = ChartCustomizeModel.class, cascade = CascadeType.ALL)
    @Column(columnDefinition = "TEXT")
    private String chartCustomize = "{}";

    @ManyToMany()
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonBackReference
    @Builder.Default
    private Set<DashboardsModel> dashboard = new HashSet<>();

    @ManyToMany(mappedBy = "chartsForTabs")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonBackReference
    @Builder.Default
    private Set<TabsModel> tabsForCharts = new HashSet<>();
}
