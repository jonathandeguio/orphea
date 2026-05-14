package io.orphea.kepler.library.models;

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
@Table(name = "kepler_chart_customize")
public class ChartCustomizeModel {
    @Id
    @GeneratedValue
    private UUID id;

    private String colorScheme = "theme1";
    private String subHeader = "";
    private String bigNumberFontSize = "normal";
    private String subHeaderFontSize = "small";
    private String xAxis;
    private String xAxisTitlePosition = "middle";
    private String xAxisTitleMargin = "40";
    private String yAxis;
    private String yAxisTitlePosition = "end";
    private String yAxisTitleMargin = "40";
    private Boolean dataZoom = false;
    private Boolean stackedBars = false;
    private Boolean showLabel = false;
    private Boolean sortBars = false;
    private Boolean legend = true;
    private Boolean donut = true;
    private String legendType = "scroll";
    private String innerRadius = "40";
    private String outerRadius = "70";
    private String lineChartStyle = "linear";
    private String gridMarginTop = "60";
    private String gridMarginRight = "10%";
    private String gridMarginBottom = "60";
    private String gridMarginLeft = "10%";
}
