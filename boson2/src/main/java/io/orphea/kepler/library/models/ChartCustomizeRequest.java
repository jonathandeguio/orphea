package io.orphea.kepler.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChartCustomizeRequest {
    private String colorScheme;
    private String subHeader;
    private String bigNumberFontSize;
    private String subHeaderFontSize;
    private String xAxis;
    private String xAxisTitlePosition;
    private String xAxisTitleMargin;
    private String yAxis;
    private String yAxisTitlePosition;
    private String yAxisTitleMargin;
    private Boolean dataZoom;
    private Boolean stackedBars;
    private Boolean showLabel;
    private Boolean sortBars;
    private Boolean legend;
    private Boolean donut;
    private String legendType;
    private String innerRadius;
    private String outerRadius;
    private String lineChartStyle;
    private String gridMarginTop;
    private String gridMarginRight;
    private String gridMarginBottom;
    private String gridMarginLeft;
}
