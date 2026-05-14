import { ChartReload } from "Apps/Kepler/chart/charts.utils";
import ChartErrorBoundary from "ErrorBoundary/ChartErrorBoundary";
import React, { LegacyRef, ReactNode, useState } from "react";
import ChartElement from "./ChartElement";
import { TooltipInfo } from "Apps/Kepler/kepler";
interface Props {
  children?: ReactNode;
  key?: string;
  style?: any;
  className?: any;
  onMouseDown?: any;
  onMouseUp?: any;
  onTouchEnd?: any;
  element?: any;
  elementType?: any;
  chartId?: any;
  fetchCachedData?: any;
  editable?: any;
  layout?: any;
  removeElement?: any;
  datasetId?: any;
  dashboardId: string;
  tabId: string;
  chartReload?: ChartReload;
  setTooltipInfo?: React.Dispatch<
    React.SetStateAction<TooltipInfo | undefined>
  >;
  tooltipInfo?: TooltipInfo;
}
export type Ref = LegacyRef<HTMLDivElement> | undefined;

export const CustomGridItemComponent = React.forwardRef<Ref, Props>(
  (props, ref) => {
    const [showChartMenuOptions, setShowChartMenuOptions] =
      useState<boolean>(false);
    if (props.elementType == "chart") {
      return (
        <div
          style={{ ...props.style }}
          className={props.className}
          ref={ref as any}
          key={props.key}
          onMouseEnter={() => setShowChartMenuOptions(true)}
          onMouseLeave={() => setShowChartMenuOptions(false)}
          {...props}
        >
          <ChartErrorBoundary
            removeHandler={(e) => {
              e.stopPropagation();
              props.removeElement(props.dashboardId, props.element.id);
            }}
            chartId={props.chartId}
          >
            <ChartElement
              chartId={props.chartId}
              fetchCachedData={props.fetchCachedData}
              // chart={chart}
              datasetId={props.datasetId}
              editable={props.editable}
              layout={props.layout}
              element={props.element}
              removeElement={props.removeElement}
              fullScreenRef={ref}
              showChartMenuOptions={showChartMenuOptions}
              dashboardId={props.dashboardId}
              tabId={props.tabId}
              chartReload={props.chartReload}
              tooltipInfo={props.tooltipInfo}
              setTooltipInfo={props.setTooltipInfo}
            />
            {props.editable && props.children}
          </ChartErrorBoundary>
        </div>
      );
    }

    return (
      <div
        style={{ ...props.style }}
        className={props.className}
        ref={ref as any}
        // onMouseDown={props.onMouseDown}
        // onMouseUp={props.onMouseUp}
        // onTouchEnd={props.onTouchEnd}
        {...props}
      >
        {props.children}
      </div>
    );
  }
);
