import { Row } from "antd";
import React, { useEffect, useRef, useState } from "react";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { ITabConfig } from "../Dashboard";
import ChartElementDropDown from "./ChartElementDropDown";
import styles from "./DashboardElements.module.scss";

interface IProps {
  showChartMenuOptions: boolean;
  editable: any;
  layout: any;
  chartData: any;
  setChartData: any;
  reloadChart: any;
  setReloadChart: any;
  fullScreenRef: any;
  removeElement: Function;
}

const ChartElementHeader = ({
  showChartMenuOptions,
  editable,
  layout,
  chartData,
  setChartData,
  reloadChart,
  setReloadChart,
  fullScreenRef,
  removeElement,
}: IProps) => {
  const [fullScreenMode, setFullScreenMode] = useState(false);
  const [isCompact, setIsCompact] = useState(false);
  const headerRef = useRef<HTMLDivElement>(null);
  const gridConfig: ITabConfig = useSelector(
    (state: RootState) => state.dashboardEdit.gridConfig
  );

  useEffect(() => {
    const observer = new ResizeObserver(() => {
      if (headerRef.current) {
        const headerWidth = headerRef.current.offsetWidth;
        const threshold = 300; // Set your desired threshold width here
        setIsCompact(headerWidth < threshold);
      }
    });

    if (headerRef.current) {
      observer.observe(headerRef.current);
    }

    return () => {
      if (headerRef.current) {
        observer.unobserve(headerRef.current);
      }
    };
  }, []);

  const fullScreenModeSet = () => {
    const chartDiv = fullScreenRef.current as any;
    if (fullScreenMode) {
      chartDiv.classList.remove("body-fullscreen-mode");
    } else {
      chartDiv.className = "body-fullscreen-mode";
    }

    setFullScreenMode(!fullScreenMode);
  };

  return (
    <div
      className={`${styles.chart_element__header} ${
        showChartMenuOptions ? "display" : "hide"
      }`}
      key={layout.i + "chart"}
      style={{
        background: gridConfig.chartHeadingBg
          ? gridConfig.chartHeadingBg
          : "var(--background_primary)",
      }}
      ref={headerRef}
    >
      <Row className={"header-icon-row"} gutter={16} align="middle">
        <ChartElementDropDown
          fullScreenMode={fullScreenMode}
          fullScreenModeSet={fullScreenModeSet}
          chartData={chartData}
          setChartData={setChartData}
          reloadChart={reloadChart}
          setReloadChart={setReloadChart}
          editable={editable}
          removeElement={removeElement}
          layout={layout}
        />
      </Row>
    </div>
  );
};

export default ChartElementHeader;
