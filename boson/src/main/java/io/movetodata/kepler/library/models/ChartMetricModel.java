package io.movetodata.kepler.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_chart_metric")
public class ChartMetricModel {
    @Id
    @GeneratedValue
    private UUID id;
    private String columnName;
    private String aggregate;
}
