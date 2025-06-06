import { KeplerSeries } from "Apps/Kepler/kepler";
import { Tooltip } from "antd";
import {
  GaugeIcon,
  GroupedColumnIcon,
  LineChartIcon,
  MapIcon,
  PieChartIcon,
  RadarIcon,
  ScatterIcon,
  SmallAreaChartIcon,
  StackedGroupedBarIcon,
  SunburstIcon,
  TreeMapIcon,
  WaterFallIcon,
  WordCloudIcon
} from "assets/icons/boslerChartIcons";
import { BigNumberIcon } from "assets/icons/boslerDataIcons";
import { MapLegendIcon } from "assets/icons/boslerInterfaceIcons";
import { TableCellIcon } from "assets/icons/boslerTableIcons";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel } from "utils/utilities";
import {
  fetchDataTrigger,
  updateQuery,
} from "../../../../redux/actions/keplerActions";
import { RootState, ThunkAppDispatch } from "../../../../redux/types/store";

function SliderController() {
  const query = useSelector((state: RootState) => state.kepler.query);

  const sliderOptions: { [key: string]: any } = {
    pieChart: {
      chartIcon: <PieChartIcon size={24} />,
      chartName: getLanguageLabel("pieChart"),
      key: 1,
      chartType: "pieChart",
      disabled: false,
    },
    VerticalAxisChart: {
      chartIcon: <GroupedColumnIcon size={24} />,
      chartName: getLanguageLabel("barChart"),
      key: 2,
      chartType: "VerticalAxisChart_barChart",
      disabled: false,
    },
    VerticalAxisChart1: {
      chartIcon: <LineChartIcon size={24} />,
      chartName: getLanguageLabel("lineChart"),
      key: 2,
      chartType: "VerticalAxisChart_lineChart",
      disabled: false,
    },
    VerticalAxisChart2: {
      chartIcon: <SmallAreaChartIcon size={24} />,
      chartName: getLanguageLabel("areaChart"),
      key: 2,
      chartType: "VerticalAxisChart_lineAreaChart",
      disabled: false,
    },
    VerticalAxisChart3: {
      chartIcon: <ScatterIcon size={24} />,
      chartName: getLanguageLabel("scatterChart"),
      key: 2,
      chartType: "VerticalAxisChart_scatterChart",
      disabled: false,
    },
    horizontalBarChart: {
      chartIcon: <StackedGroupedBarIcon size={24} />,
      chartName: getLanguageLabel("horizontalAxisChart"),
      key: 3,
      chartType: "horizontalBarChart",
      disabled: false,
    },
    bigNumber: {
      chartIcon: <BigNumberIcon size={24} />,
      chartName: getLanguageLabel("bigNumber"),
      key: 4,
      chartType: "bigNumber",
      disabled: false,
    },
    table: {
      chartIcon: <TableCellIcon size={24} />,
      chartName: getLanguageLabel("table"),
      key: 5,
      chartType: "table",
      disabled: false,
    },
    gaugeChart: {
      chartIcon: <GaugeIcon size={28} />,
      chartName: getLanguageLabel("gauge"),
      key: 6,
      chartType: "gaugeChart",
      disabled: false,
    },
    radarChart: {
      chartIcon: <RadarIcon size={26} />,
      chartName: getLanguageLabel("radar"),
      key: 7,
      chartType: "radarChart",
      disabled: false,
    },
    sunBurstChart: {
      chartIcon: <SunburstIcon size={26} />,
      chartName: getLanguageLabel("sunBurst"),
      key: 8,
      chartType: "sunBurstChart",
      disabled: false,
    },
    waterFallChart: {
      chartIcon: <WaterFallIcon size={24} />,
      chartName: getLanguageLabel("waterfallChart"),
      key: 11,
      chartType: "waterFallChart",
      disabled: false,
    },
    treeMapChart: {
      chartIcon: <TreeMapIcon size={24} />,
      chartName: getLanguageLabel("treemapChart"),
      key: 12,
      chartType: "treeMapChart",
      disabled: false,
    },
    wordCloudChart: {
      chartIcon: <WordCloudIcon size={28}/>,
      chartName: getLanguageLabel("wordCloudChart"),
      key: 11,
      chartType: "wordCloudChart",
      disabled: false,
    },
    mapChart: {
      chartIcon: <MapIcon size={24} />,
      chartName: getLanguageLabel("map"),
      key: 9,
      chartType: "mapChart",
      disabled: true,
    },
    parameterChart: {
      chartIcon: <MapLegendIcon size={24} />,
      chartName: getLanguageLabel("parameterChart"),
      key: 10,
      chartType: "parameterChart",
      disabled: false,
    },
  };

  const [selectedKey, setSelectedKey] = useState<string>("pieChart");

  const chartArray: Array<any> = [];

  for (const [key, value] of Object.entries(sliderOptions)) {
    chartArray.push(value);
  }

  useEffect(() => {
    if (query) {
      if (query.chartType === "VerticalAxisChart") {
        setSelectedKey(`${query.chartType}_${query.series[0]?.seriesType}`);
      } else {
        setSelectedKey(query.chartType);
      }
    }
  }, [query]);

  const dispatch = useDispatch<ThunkAppDispatch>();

  return (
    <div className="slider_container --pt20 --p15">
      <div className="slider">
        {chartArray.map((data) => (
          <Tooltip
            title={
              data.disabled
                ? `${data.chartName} : ${getLanguageLabel(
                    "notAvailable"
                  )} (${getLanguageLabel("subscriptionNeeded")})`
                : data.chartName
            }
          >
            <div
              className={`slider__wrapper ${
                selectedKey == data.chartType
                  ? "slider__wrapper--active"
                  : "slider__wrapper--inactive"
              } `}
            >
              <div
                className={`slider__option ${selectedKey} ${
                  data.disabled ? "slider__option--disabled" : ""
                }`}
                onClick={() => {
                  if (!data.disabled && selectedKey !== data.chartType) {
                    let queryUpdate: {
                      chartType: string;
                      series?: KeplerSeries[];
                    } = {
                      chartType: data.chartType,
                    };

                    const chartType = data.chartType.split("_");
                    if (chartType.length > 1) {
                      queryUpdate = {
                        chartType: chartType[0],
                        series: [
                          { ...query.series[0], seriesType: chartType[1] },
                          ...query.series.slice(1),
                        ],
                      };
                    }

                    dispatch(updateQuery(queryUpdate));
                    dispatch(fetchDataTrigger());
                  }
                }}
              >
                {data.chartIcon}
              </div>
            </div>
          </Tooltip>
        ))}
      </div>
    </div>
  );
}

export default SliderController;
