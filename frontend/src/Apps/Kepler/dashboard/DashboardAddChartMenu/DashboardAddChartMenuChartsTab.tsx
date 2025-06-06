import { TreeExplorer } from "Apps/explorer";
import { ProjectDropdownButton } from "Apps/explorer/ProjectDropdownButton";
import { Skeleton } from "antd";
import BoslerSwitch from "components/CommonUI/BoslerSwitch/BoslerSwitch";
import { useFileExplorerService } from "hooks/useFileExplorerService";
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useParams } from "react-router";
import { RootState } from "redux/types/store";
import { getLanguageLabel, notEmpty, openNotification } from "utils/utilities";
import { getDashboardDataAPI } from "../Dashboard.api";
import DashboardAddChartMenuElementsTab from "./DashboardAddChartMenuElementsTab";
import DashboardChartsSuggestedSearch from "./DashboardChartsSuggestedSearch";
const DashboardAddChartMenuChartsTab = () => {
  const { id } = useParams();
  if (!id) {
    return null;
  }
  const [treeData, setTreeData] = useState();
  const [selectedTab, setSelectedTab] = useState<"suggested" | "explorer">(
    "explorer"
  );
  const [project, setProject] = useState<string | undefined>(undefined);

  const fileIndexes = useSelector(
    (state: RootState) => state.indexes.fileIndexes
  );
  const { tabId }: { chartAction: any; tabId: string } = useSelector(
    (state: RootState) => state.dashboardEdit
  );

  const { fetchResourceTree, getFileIndex } = useFileExplorerService();

  useEffect(() => {
    if (notEmpty(project)) {
      fetchResourceTree(project);
      getFileIndex(project).then((data) => setTreeData(data));
    }
  }, [project]);

  useEffect(() => {
    if (notEmpty(project)) {
      getFileIndex(project).then((data) => setTreeData(data));
    }
  }, [fileIndexes]);

  useEffect(() => {
    if (notEmpty(id)) {
      getDashboardDataAPI(id)
        .then(({ data }) => {
          setProject(data["project"]);
        })
        .catch(() => {
          openNotification("Dashboard not fetched", " ", "error");
        });
    }
  }, [id]);

  return (
    <>
      <DashboardAddChartMenuElementsTab dashboardId={id} tabId={tabId} />

      <BoslerSwitch
        style={{
          flex: "1 1 auto",
        }}
        items={[
          {
            label: getLanguageLabel("explorer"),
            value: "explorer",
            children: (
              <>
                <div style={{ marginBottom: "0.5rem" }}>
                  {project ? (
                    <ProjectDropdownButton
                      onSelect={(id) => {
                        setTreeData(undefined);
                        setProject(id);
                      }}
                      showNewButton={false}
                      defaultProject={project}
                    />
                  ) : (
                    <Skeleton />
                  )}
                </div>
                <TreeExplorer
                  treeData={treeData}
                  defaultActiveId={id}
                  type={["CHART", "FILE"]}
                  dynamicFetching={false}
                  openOnSingleClick={true}
                />
              </>
            ),
          },
          {
            label: getLanguageLabel("suggested"),
            value: "suggested",
            children: (
              <div className="dashboard_charts_filter">
                <DashboardChartsSuggestedSearch />
              </div>
            ),
          },
        ]}
        value={selectedTab}
        onChange={(newVal: "explorer" | "suggested") => {
          setSelectedTab(newVal);
        }}
        divider
      />
    </>
  );
};

export default DashboardAddChartMenuChartsTab;
