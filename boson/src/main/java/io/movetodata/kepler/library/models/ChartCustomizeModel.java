package io.movetodata.kepler.library.models;

import io.movetodata.sharedutils.MoveToDataUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_chart_customize")
public class ChartCustomizeModel implements MoveToDataUtils {
    @Id
    @GeneratedValue
    private UUID id;
    private String colorTheme = "theme1";
    private String subHeader = "";
    private String bigNumberFontSize = "normal";
    private String subHeaderFontSize = "small";
    private String scatterColor;
    private String suffix = "";
    private String prefix = "";
    private String xAxis;
    private String xAxisTitlePosition = "middle";
    private String xAxisTitleMargin = "40";
    private String xAxisSplitLine = "solid";
    private String mapChartTileLayer = "Stadia.OSMBright";

    private String yAxisLeft;
    private String yAxisLeftTitlePosition = "middle";
    private String yAxisLeftTitleMargin = "70";
    private String yAxisSplitLine = "solid";

    private String yAxisRight;
    private String yAxisRightTitlePosition = "middle";
    private String yAxisRightTitleMargin = "70";

    private Boolean dataZoom = false;
    private Boolean sortBars = false;
    private Boolean legend = true;
    private Boolean tooltip = true;
    private Boolean tooltipAxisPointer = true;
    private String tooltipAxisTrigger = "item";
    private String legendType = "scroll";
    private Boolean donut = true;
    private String innerRadius = "40";
    private Boolean nightangle;
    private String outerRadius = "70";
    private String gridMarginTop = "60";
    private String gridMarginRight = "10%";
    private String gridMarginBottom = "60";
    private String gridMarginLeft = "10%";
    private String legendPosition = "right";

    private String lineChartStyle = "linear";

    @Column(columnDefinition = "TEXT")
    private String colorScheme = "{}";
    private Boolean stackedBars = false;
    private Boolean showLabel = false;
    private Boolean reversed = false;

    @Column(columnDefinition = "TEXT")
    private String seriesCustomize = "[{}]";

    private String bigNumberColor;
    private String bigNumberSubheaderColor;
    private String bigNumberTop;
    private String subheaderTop;

    private String tableHeaderFontSize;
    private String tableBodyFontSize;

    private Boolean sunBurstTreeMap;

    public ChartCustomizeModel(ChartCustomizeModel original) {
        this.copyNonNullProperties(original);
    }
}
