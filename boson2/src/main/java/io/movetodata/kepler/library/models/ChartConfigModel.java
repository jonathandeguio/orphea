package io.movetodata.kepler.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_chart_config")
public class ChartConfigModel {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID datasetId;
    private String branch;
    private int rowSelect = 500000;
    private String chartId;

    @OneToOne(targetEntity = TimeModel.class, cascade = CascadeType.ALL)
    private TimeModel time;

    @OneToMany(targetEntity = ChartMetricModel.class, cascade = CascadeType.ALL)
    private List<ChartMetricModel> sortBy;

    @OneToMany(targetEntity = ChartMetricModel.class, cascade = CascadeType.ALL)
    private List<ChartMetricModel> metric;

    @ElementCollection
    private List<String> dimension;

    @OneToMany(targetEntity = ChartFilterModel.class, cascade = CascadeType.ALL)
    private List<ChartFilterModel> filter;
}
