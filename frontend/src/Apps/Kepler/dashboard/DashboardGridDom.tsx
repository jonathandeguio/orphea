import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import React from "react";
import { TabElementOperationEnum } from "./Dashboard.types";
import { CustomGridItemComponent } from "./DashboardElements/CustomGridItemComponent";
import DividerElement from "./DashboardElements/DividerElement";
import EditorElement from "./DashboardElements/EditorElement";
import FileElement from "./DashboardElements/FileElement";
import HeaderElement from "./DashboardElements/HeaderElement";
import MarkdownElement from "./DashboardElements/MarkdownElement";
import TextElement from "./DashboardElements/TextElement";

export default function generateDom(
  layout: any[],
  tabElementsMap: any,
  editable: boolean,
  removeElement: (elementId: string) => void,
  handleUpdateTabElement: (elementId: string, data: string) => void,
  dashboardId: string,
  tabId: string,
  fullScreenRef: any,
  setTooltipInfo: any,
  tooltipInfo: any
) {
  return layout.map((l: any) => {
    const typeElement = l.type;
    if (tabElementsMap[l.i].operation == TabElementOperationEnum.DELETE) {
      return;
    }
    if (typeElement === "header") {
      return (
        <CustomGridItemComponent
          key={l.i}
          data-grid={l}
          style={{
            zIndex: l.zIndex,
          }}
          dashboardId={dashboardId}
          tabId={tabId}
        >
          <HeaderElement
            layout={l}
            element={tabElementsMap[l.i]}
            editable={editable}
            removeElement={removeElement}
            dashboardId={dashboardId}
            tabId={tabId}
          />
        </CustomGridItemComponent>
      );
    } else if (typeElement === "editor") {
      return (
        <CustomGridItemComponent
          key={l.i}
          data-grid={l}
          style={{
            zIndex: l.zIndex,
          }}
          dashboardId={dashboardId}
          tabId={tabId}
        >
          <EditorElement
            layout={l}
            element={tabElementsMap[l.i]}
            editable={editable}
            removeElement={removeElement}
            dashboardId={dashboardId}
            tabId={tabId}
            updateTabElement={handleUpdateTabElement}
          />
        </CustomGridItemComponent>
      );
    } else if (typeElement === "text") {
      return (
        <CustomGridItemComponent
          key={l.i}
          data-grid={l}
          style={{
            zIndex: l.zIndex,
          }}
          dashboardId={dashboardId}
          tabId={tabId}
        >
          <TextElement
            layout={l}
            element={tabElementsMap[l.i]}
            editable={editable}
            removeElement={removeElement}
            dashboardId={dashboardId}
            tabId={tabId}
          />
        </CustomGridItemComponent>
      );
    } else if (typeElement === "divider") {
      // Divider shouldnt be resizing even in editable mode
      // l.isResizable = false;
      return (
        <CustomGridItemComponent
          key={l.i}
          data-grid={l}
          style={{
            zIndex: l.zIndex,
          }}
          dashboardId={dashboardId}
          tabId={tabId}
        >
          <DividerElement
            layout={l}
            element={tabElementsMap[l.i]}
            dashboardId={dashboardId}
            tabId={tabId}
            editable={editable}
            removeElement={removeElement}
            updateTabElement={handleUpdateTabElement}
          />
        </CustomGridItemComponent>
      );
    } else if (typeElement === "file") {
      return (
        <CustomGridItemComponent
          key={l.i}
          data-grid={l}
          style={{
            zIndex: l.zIndex,
          }}
          dashboardId={dashboardId}
          tabId={tabId}
        >
          <FileElement
            layout={l}
            element={tabElementsMap[l.i]}
            dashboardId={dashboardId}
            tabId={tabId}
            editable={editable}
            removeElement={removeElement}
          />
        </CustomGridItemComponent>
      );
    } else if (typeElement === "markdown") {
      return (
        <CustomGridItemComponent
          key={l.i}
          data-grid={l}
          style={{
            zIndex: l.zIndex,
          }}
          dashboardId={dashboardId}
          tabId={tabId}
        >
          <MarkdownElement
            layout={l}
            element={tabElementsMap[l.i]}
            editable={editable}
            removeElement={removeElement}
            tabId={tabId}
            dashboardId={dashboardId}
          />
        </CustomGridItemComponent>
      );
    } else if (typeElement === "chart") {
      if (!tabElementsMap.hasOwnProperty(l.i)) {
        return;
      }
      if (tabElementsMap[l.i].data == "") {
        return (
          <div key={l.i} data-grid={l}>
            <span className="text">{l.i}</span>
            <div className="type">{"Type:" + typeElement}</div>
          </div>
        );
      }

      return (
        <CustomGridItemComponent
          key={l.i}
          data-grid={l}
          style={{
            borderRadius: "2px",
            height: "none",
            background: "var(--background-color)",
            border: !editable
              ? "0.25px solid var(--bosler-border-color-default)"
              : "",
            zIndex: l.zIndex,
          }}
          chartId={tabElementsMap[l.i].data}
          datasetId={tabElementsMap[l.i].datasetId}
          fetchCachedData={true}
          editable={editable}
          layout={l}
          removeElement={removeElement}
          elementType="chart"
          element={tabElementsMap[l.i]}
          ref={fullScreenRef}
          setTooltipInfo={setTooltipInfo}
          tooltipInfo={tooltipInfo}
          dashboardId={dashboardId}
          tabId={tabId}
          // chartReload={chartReload}
        />
      );
    } else {
      return (
        <div
          key={l.i}
          data-grid={l}
          className={
            editable ? "editableElementBorder" : "nonEditableElementBorder"
          }
        >
          <span className="text">{l.i}</span>
          <div className="type">{"Type:" + typeElement}</div>
          <div
            className="editableElementBorder-delete"
            onClick={() => removeElement(l.i)}
          >
            <TrashIcon />
          </div>
        </div>
      );
    }
  });
}
