import { Form, Switch } from "antd";
import React, { useCallback, useMemo } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel, makeDebounceFunction } from "utils/utilities";
import { RootState } from "../../../../redux/types/store";
import { chartConfig } from "../charts.config";
import { BigNumberCustomizer } from "./BigNumberCustomizer";
import "./ChartsCustomizer.scss";
import { GaugeChartCustomizer } from "./GaugeChartCustomizer";
import { GridCustomizer } from "./GridCustomizer";
import { LeftYAxisCustomizer } from "./LeftYAxisCustomizer";
import { LegendCustomizer } from "./LegendCustomizer";
import { MapCustomizer } from "./MapCustomizer";
import { ParameterChartCustomizer } from "./ParameterChartCustomizer";
import { PieChartCustomizer } from "./PieChartCustomizer";
import { RadarChartCustomizer } from "./RadarChartCustomizer";
import { RightYAxisCustomizer } from "./RightYAxisCustomizer";
import { SeriesCutomizer } from "./SeriesCutomizer";
import { SunBurstCustomizer } from "./SunBurstCustomizer";
import { TableCustomizer } from "./TableCustomizer";
import { ThemeSelector } from "./ThemeSelector";
import { ThresholdCustomizer } from "./ThresholdCustomizer";
import { TitleCustomizer } from "./TitleCustomizer";
import { TooltipCustomizer } from "./TooltipCustomizer";
import { TreeMapCustomizer } from "./TreeMapCustomizer";
import { WaterFallChartCustomizer } from "./WaterFallChartCustomizer";
import { WordCloudChartCustomizer } from "./WordCloudChartCustomizer";
import { XAxisCustomizer } from "./XAxisCustomizer";

function customizeForm() {
  const dispatch = useDispatch();
  const defaultCustomize = useSelector(
    (state: RootState) => state.kepler.customize
  );
  const query = useSelector((state: RootState) => state.kepler.query);
  const customizeForm = useSelector(
    (state: RootState) => state.kepler.customizeForm
  );

  const hasRightAxis: boolean =
    query?.series?.filter((s: any) => {
      return s.seriesIndex === "right";
    }).length > 0;

  const skeleton = useMemo(
    () => chartConfig[query?.chartType],
    [query?.chartType]
  );

  const debouncedUpdateDispatch = useCallback(
    makeDebounceFunction((b: any) => {
      dispatch({
        type: "UPDATE_CUSTOMIZE",
        payload: b,
      });
    }, 650),
    [dispatch]
  );

  return (
    <div className="customizer">
      <Form
        form={customizeForm}
        initialValues={defaultCustomize}
        labelCol={{ span: 10 }}
        wrapperCol={{ span: 14 }}
        colon={false}
        onValuesChange={(a, b) => {
          debouncedUpdateDispatch(b);
        }}
      >
        {/* THEME SELECTOR NOT SHOWN FOR BIGNUMBER AND TABLE */}
        {skeleton.meta.hasTheme && <ThemeSelector />}
        {/* DATA ZOOM OPTION NOT FOR TABLE, PIECHART, BIGNUMBER */}
        {/* {skeleton.meta.isAxisChart && (
          <Form.Item
            name="dataZoom"
            valuePropName="checked"
            label={
              <div className="query_item__heading">
                {getLanguageLabel("dataZoom")}
              </div>
            }
          >
            <Switch size={"small"} />
          </Form.Item>
        )} */}

        {skeleton.meta.hasTitle && <TitleCustomizer skeleton={skeleton} />}

        {/* Waterfall Chart Options */}
        {query.chartType === "waterFallChart" && <WaterFallChartCustomizer />}

        {/* Parameter Chart Options */}
        {query.chartType === "wordCloudChart" && <WordCloudChartCustomizer />}

        {skeleton.meta.isAxisChart && (
          <>
            {/* X AXIS PANEL */}
            {skeleton.hasOwnProperty("xaxis") && <XAxisCustomizer />}

            {/* Y AXIS PANEL */}
            {skeleton.meta.isAxisChart && <LeftYAxisCustomizer />}

            {/* RIGHT Y AXIS PANEL */}
            {skeleton.meta.isAxisChart && hasRightAxis && (
              <RightYAxisCustomizer />
            )}
          </>
        )}

        {/* SERIES CUSTOMIZER */}
        {/* TODO : FIXME */}
        {skeleton.customization.series && (
          <SeriesCutomizer chartType={query.chartType} />
        )}

        {/* BIG NUMBER OPTIONS */}
        {query.chartType === "bigNumber" && (
          <BigNumberCustomizer customizeForm={customizeForm} />
        )}

        {/* TABLE CHART OPTIONS */}
        {query.chartType === "table" && <TableCustomizer />}

        {/* TABLE CHART OPTIONS */}
        {query.chartType === "pieChart" && <PieChartCustomizer />}

        {/* MAP CHART OPTIONS */}
        {query.chartType === "mapChart" && <MapCustomizer />}

        {/* Sunburst CHART OPTIONS */}
        {query.chartType === "sunBurstChart" && <SunBurstCustomizer />}

        {/* Gauge CHART OPTIONS */}
        {query.chartType === "gaugeChart" && <GaugeChartCustomizer />}

        {/* Gauge CHART OPTIONS */}
        {query.chartType === "radarChart" && <RadarChartCustomizer />}

        {/* Parameter Chart Options */}
        {query.chartType === "parameterChart" && <ParameterChartCustomizer />}

        {/* Parameter Chart Options */}
        {query.chartType === "treeMapChart" && <TreeMapCustomizer />}

        {skeleton.meta.legend && (
          <LegendCustomizer query={query} form={customizeForm} />
        )}
        {/* TOOLTIP OPTIONS */}

        {skeleton.meta.tooltip && (
          <TooltipCustomizer chartType={query.chartType} />
        )}

        {skeleton.meta.threshold !== "DISABLED" && (
          <ThresholdCustomizer name={"chart"} />
        )}

        <GridCustomizer type="OUTER" query={query} />
        {/* {skeleton.meta.isAxisChart && (
          <GridCustomizer type="INNER" query={query} />
        )} */}
      </Form>
    </div>
  );
}

export default customizeForm;
