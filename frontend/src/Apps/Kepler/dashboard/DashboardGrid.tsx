import BoslerLoader from "components/boslerLoader";
import React, { useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  generateUUID,
  getLanguageLabel,
  openNotification,
} from "utils/utilities";

import NoData from "components/CommonUI/NoData";
import useEffectOnlyOnDependencyUpdate from "hooks/useEffectOnlyOnDependencyUpdate";
import { EmptyChartIcon } from "../../../assets/icons/boslerMiscellaneousIcons";
import {
  isChartAdded,
  isDashboardChanged,
  revertSaveDashboardStatus,
} from "../../../redux/actions/dashboardActions";
import { RootState, ThunkAppDispatch } from "../../../redux/types/store";
import { TooltipInfo } from "../kepler";
import { getTabElementsAPI, updateTabElementAPI } from "./Dashboard.api";
import { GRID_CONFIG } from "./Dashboard.contants";
import styles from "./Dashboard.module.scss";
import { TabElementOperationEnum } from "./Dashboard.types";
import { getDefaultMeasurementsOfElements } from "./Dashboard.utils";
import generateDom from "./DashboardGridDom";
import DashboardEmptyTab from "./DashboardTabs/DashboardEmptyTab";

const { Responsive, WidthProvider } = require("react-grid-layout");
const ResponsiveGridLayout = WidthProvider(Responsive);

interface IProps {
  editable: boolean;
  tabId: string;
  dashboardId: string;
  gridRef: any;
}

const DashboardGrid = ({ editable, tabId, dashboardId, gridRef }: IProps) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const fullScreenRef = useRef(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [tooltipInfo, setTooltipInfo] = useState<TooltipInfo | undefined>(
    undefined
  );

  const [originalLayout, setOriginalLayout] = useState<any>();
  const [tabElementsMap, setTabElementsMap] = useState<any>();

  const { subscribeMenu, gridConfig, filterMenu, triggerSave } = useSelector(
    (state: RootState) => state.dashboardEdit
  );

  const resourcePermission = useSelector(
    (state) => (state as $TSFixMe).resourcePermission[dashboardId]
  );

  function handleUpdateTabElement(elementId: string, data: string) {
    tabElementsMap[elementId] = { ...tabElementsMap[elementId], data: data };
    setTabElementsMap(tabElementsMap);
  }

  function removeElement(elementId: string) {
    if (tabElementsMap[elementId].operation == TabElementOperationEnum.CREATE) {
      delete tabElementsMap[elementId];
      const newLayout = originalLayout.filter(
        (item: any) => item.i !== elementId
      );
      setOriginalLayout(newLayout);
    } else {
      tabElementsMap[elementId] = {
        ...tabElementsMap[elementId],
        operation: TabElementOperationEnum.DELETE,
      };
      setOriginalLayout((originalLayout: any) =>
        originalLayout.map((item: any) =>
          item.i == elementId
            ? { ...item, operation: TabElementOperationEnum.DELETE }
            : item
        )
      );
    }

    setTabElementsMap(tabElementsMap);
  }

  function handleLayoutChange(layout: any, layouts: any) {
    const updatedLayout = layout.map((newItem: any) => {
      let updatedItem = newItem;
      originalLayout.map((item: any) => {
        if (item.i === newItem.i) {
          updatedItem = {
            ...item,
            h: newItem.h,
            w: newItem.w,
            x: newItem.x,
            y: newItem.y,
          };
        }
      });
      return updatedItem;
    });

    setOriginalLayout(updatedLayout);
  }

  function onDrop(layout: any, layoutItem: any, _event: any) {
    const layoutElement = _event.dataTransfer.getData("text/plain");
    const chartId = _event.dataTransfer.getData("chart");
    console.log("CHART ID : ", chartId);
    console.log("LAYOUT ELE : ", layoutElement);
    const file = _event.dataTransfer.getData("file");
    const layoutElementType = layoutElement.substr(
      layoutElement.indexOf("-") + 1
    );

    if (
      !(
        layoutElementType == "chart" ||
        layoutElementType == "editor" ||
        layoutElementType == "divider" ||
        layoutElementType == "file"
      )
    ) {
      return;
    }

    // Condition Check: If a chart is already added to tab
    let _isChartAlreadyAddedFlag = false;
    Object.values(tabElementsMap).forEach((value: any) => {
      if (value.data == chartId) {
        openNotification("Chart already added", "", "error");
        _isChartAlreadyAddedFlag = true;
        return;
      }
    });

    if (_isChartAlreadyAddedFlag) return;

    const measurements = getDefaultMeasurementsOfElements(layoutElementType);
    const height: number = measurements.height;
    const width: number = measurements.width;
    const minHeight: number = measurements.minHeight;
    const maxHeight: number = measurements.maxHeight;
    const minWidth: number = measurements.minWidth;
    const maxWidth: number = measurements.maxWidth;

    const elementData = () => {
      if (layoutElementType == "header") {
        return JSON.stringify(
          JSON.stringify({
            heading: "",
            fontSize: "1",
            backgroundColor: "1",
          })
        );
      } else if (layoutElementType == "divider") {
        return "#e7e7e7";
      } else if (layoutElementType == "file") {
        return file;
      } else {
        return chartId;
      }
    };
    // // In other cases no mapping is required
    // // Element creation is required for all cases
    const elementId = generateUUID();
    const elementPayload = {
      id: elementId,
      dashboardId: dashboardId,
      tabId: tabId,
      type: layoutElementType,
      position: JSON.stringify(
        JSON.stringify({
          x: layoutItem.x,
          y: layoutItem.y,
          h: height,
          w: width,
          minH: minHeight,
          minW: minWidth,
          maxH: maxHeight,
          maxW: maxWidth,
          type: layoutElementType,
        })
      ),
      data: elementData(),
      operation: TabElementOperationEnum.CREATE,
    };

    const newLayout = [
      ...originalLayout,
      {
        x: layoutItem.x,
        y: layoutItem.y,
        h: height,
        w: width,
        minH: minHeight,
        minW: minWidth,
        maxH: maxHeight,
        maxW: maxWidth,
        type: layoutElementType,
        i: elementId,
        data: elementData(),
      },
    ];

    tabElementsMap[elementId] = elementPayload;
    setTabElementsMap(tabElementsMap);
    setOriginalLayout(newLayout);

    // To update the columns in filter strip for new chart dataset
    if (layoutElementType == "chart") {
      dispatch(isChartAdded(elementData()));
    }
  }

  const getTabElements = () => {
    setIsLoading(true);
    getTabElementsAPI(dashboardId, tabId)
      .then(({ data: tabElements }) => {
        const layout: any[] = [];
        const tabElementsMap: any = {};
        tabElements.map((element: any) => {
          const currentLayout = JSON.parse(JSON.parse(element.position));
          currentLayout.i = element.id;
          currentLayout.type = element.type;
          layout.push(currentLayout);
          tabElementsMap[element.id] = element;
        });
        setTabElementsMap(tabElementsMap);
        console.log("LAYOUT SETTING : ", layout);
        setOriginalLayout(layout);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  const handleSaveDashboard = () => {
    const tabElementsPayload: any[] = [];
    originalLayout.map((layoutEle: any) => {
      tabElementsPayload.push({
        dashboardId: dashboardId,
        tabId: tabId,
        elementId: layoutEle.i,
        position: JSON.stringify(
          JSON.stringify({
            x: layoutEle.x,
            y: layoutEle.y,
            h: layoutEle.h,
            w: layoutEle.w,
            minH: layoutEle.minH,
            minW: layoutEle.minW,
            maxH: layoutEle.maxW,
            maxW: layoutEle.maxW,
            type: tabElementsMap[layoutEle.i].type,
          })
        ),
        type: tabElementsMap[layoutEle.i].type,
        data: tabElementsMap[layoutEle.i].data,
        operation: tabElementsMap[layoutEle.i].operation,
      });
    });

    updateTabElementAPI(dashboardId, tabId, tabElementsPayload)
      .then(() => {
        let newLayout = originalLayout;
        Object.values(tabElementsMap).forEach((value: any) => {
          if (value.operation == TabElementOperationEnum.CREATE) {
            tabElementsMap[value.id].operation = TabElementOperationEnum.NONE;
          } else if (value.operation == TabElementOperationEnum.DELETE) {
            delete tabElementsMap[value.id];
            newLayout = newLayout.filter((item: any) => item.i !== value.id);
          }
        });

        setOriginalLayout(newLayout);
        setTabElementsMap(tabElementsMap);
        dispatch(revertSaveDashboardStatus(true));
      })
      .catch(() => {
        dispatch(revertSaveDashboardStatus(false));
      });
  };

  useEffect(() => {
    getTabElements();
  }, [editable]);

  /*
  https://stackoverflow.com/questions/65547160/how-can-re-render-react-grid-layout-columns-width-on-parents-resizing
  https://github.com/react-grid-layout/react-grid-layout/issues/1369
  */

  useEffect(() => {
    // when isCollapsed changes, we need to dispatch a resize event to the window to force the grid to re-render
    setTimeout(() => {
      window.dispatchEvent(new Event("resize"));
    }, 200);
  }, [filterMenu, subscribeMenu, editable, resourcePermission]);

  useEffectOnlyOnDependencyUpdate(() => {
    handleSaveDashboard();
  }, [triggerSave]);

  if (originalLayout === undefined || isLoading) {
    return <BoslerLoader />;
  }

  if (!editable && originalLayout.length == 0)
    return (
      <div className={styles.emptyTabContainer}>
        <NoData
          icon={<EmptyChartIcon size={160} />}
          heading={getLanguageLabel("noComponentPresentAddViaEditButton")}
        />
      </div>
    );

  return (
    <div
      className={styles.gridContainer}
      style={{
        // Pick config, else pick system default
        background: gridConfig.pageBg
          ? gridConfig.pageBg
          : "var(--bosler-bkg-color-muted)",
        padding: `${gridConfig.topPadding}px ${gridConfig.rightPadding}px ${gridConfig.bottomPadding}px ${gridConfig.leftPadding}px`,
      }}
      ref={gridRef}
    >
      <ResponsiveGridLayout
        {...GRID_CONFIG}
        layout={originalLayout}
        isDraggable={editable ? true : false}
        isResizable={editable ? true : false}
        isDroppable={editable ? true : false}
        onLayoutChange={() => {
          dispatch(isDashboardChanged(true));
        }}
        onDrop={onDrop}
        onDragStop={(newLayout: any, previousItem: any, newItem: any) => {
          handleLayoutChange(newLayout, null);
        }}
        onResizeStop={(newLayout: any, oldItem: any, newItem: any) => {
          handleLayoutChange(newLayout, null);
        }}
        style={
          editable
            ? {
                // border: "1px dashed var(--PRIMARY_COLOR)",
                minHeight: "calc(100% - 1rem - 3rem)",
                background: gridConfig.canvasBg
                  ? gridConfig.canvasBg
                  : "var(--bosler-bkg-color-muted)",
              }
            : {
                background: gridConfig.canvasBg
                  ? gridConfig.canvasBg
                  : "var(--bosler-bkg-color-muted)",
              }
        }
        draggableCancel=".cancelSelectorName"
        preventCollision={false}
      >
        {generateDom(
          originalLayout,
          tabElementsMap,
          editable,
          removeElement,
          handleUpdateTabElement,
          dashboardId,
          tabId,
          fullScreenRef,
          setTooltipInfo,
          tooltipInfo
        )}
      </ResponsiveGridLayout>
      {originalLayout.length == 0 && <DashboardEmptyTab />}
    </div>
  );
};

export default React.memo(DashboardGrid);
