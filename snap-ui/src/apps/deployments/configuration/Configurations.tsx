import { Tabs } from "antd";
import MoveToDataLoader from "components/movetodataLoader";
import { TickIcon, UndoIcon } from "assets/icons/movetodataNavigationIcon";
import React, { useEffect, useRef, useState } from "react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import { useParams } from "react-router";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import { CollapserHandler } from "components/ResizablePane/ResizablePaneUtil";
import "./configuration.scss";
import CreateNewConfigurationTargetModal from "./CreateNewConfigurationTargetModal";
import CreateNewLicenseModal from "./CreateNewLicenseModal";
import MoveToDataModal from "components/MoveToDataModalContainer";
import {
  fetchDeploymentDetails,
  getConfigurationColumns,
  getTabItems,
  handleRevert,
  mergedData,
} from "./configuration";
import LeftPanel from "components/Configurations/LeftPanel";
import SelectedTable from "components/Configurations/SelectedTable";

const Configuration = () => {
  // Define a type for ComponentState

  const [isCreateNewLicenseModalOpen, setIsCreateNewLicenseModalOpen] =
    useState(false);

  const [
    isCreateNewConfigurationTargetModalOpen,
    setIsCreateConfigurationTargetModalOpen,
  ] = useState(false);

  const [isRevertStateModalOpen, setIsRevertStateModalOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);

  const handleRevertModal = (record: any) => {
    setSelectedRecord(record);
    setIsRevertStateModalOpen(true);
  };

  const handleConfirmRevert = () => {
    // Perform the revert action here
    console.log("Reverting state for record:", selectedRecord);
    handleRevert({
      selectedRecord,
      deploymentDetails,
      deploymentId,
      setIsLoading,
      setDeploymentDetails,
    });
    setIsRevertStateModalOpen(false);
  };

  const handleCancelRevert = () => {
    setIsRevertStateModalOpen(false);
  };

  const { id: deploymentId } = useParams();
  const primaryPanelRef = useRef<any>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [deploymentDetails, setDeploymentDetails] = useState({
    id: "",
    name: "",
    location: "",
    address: "",
    contactDetails: "",
    email: "",
    deploymentMethod: "",
    configurationComponentsModel: [],
    licenseModel: [],
    pausedUntil: "",
    timeWindowStart: 0.0,
    timeWindowEnd: 0.0,
    overRideTimeWindow: 0,
    branch: "",
  });
  const [dataSource, setDataSource] = useState<any[]>([]);

  useEffect(() => {
    fetchDeploymentDetails({
      deploymentId,
      setIsLoading,
      setDeploymentDetails,
    });
  }, [deploymentId]);

  function onChange(pagination: any, filters: any, sorter: any, extra: any) {
    //
  }

  const [activeTabKey, setActiveTabKey] = useState(
    localStorage.getItem("activeTabKey") || "1"
  );

  const handleTabChange = (key: any) => {
    setActiveTabKey(key);
    localStorage.setItem("activeTabKey", key);
  };

  const shouldHighlightActiveRecord = (record: any) => {
    return record.active !== record.target;
  };

  const configurationColumns = getConfigurationColumns(handleRevertModal);

  useEffect(() => {
    const fetchData = async () => {
      const data = await mergedData(
        deploymentDetails.configurationComponentsModel,
        deploymentDetails
      );
      setDataSource(data);
    };

    if (deploymentDetails) {
      fetchData();
    }
  }, [deploymentDetails]);

  const tabItems = getTabItems({
    setIsCreateConfigurationTargetModalOpen,
    deploymentDetails,
    setIsCreateNewLicenseModalOpen,
    onChange,
    dataSource,
    configurationColumns,
    shouldHighlightActiveRecord,
  });

  if (isLoading) {
    return <MoveToDataLoader size="large" />;
  }
  return (
    <>
      <PanelGroup direction={"horizontal"}>
        <LeftPanel
          primaryPanelRef={primaryPanelRef}
          deploymentDetails={deploymentDetails}
          onDeploymentUpdated={() =>
            fetchDeploymentDetails({
              deploymentId,
              setIsLoading,
              setDeploymentDetails,
            })
          }
        />
        <PanelResizeHandle className="resizablePane-collapser">
          <CollapserHandler primaryPanelRef={primaryPanelRef} />
        </PanelResizeHandle>
        <CreateNewConfigurationTargetModal
          deploymentDetails={deploymentDetails}
          isOpen={isCreateNewConfigurationTargetModalOpen}
          setIsOpen={setIsCreateConfigurationTargetModalOpen}
          onTarget={() =>
            fetchDeploymentDetails({
              deploymentId,
              setIsLoading,
              setDeploymentDetails,
            })
          }
        />
        <CreateNewLicenseModal
          deploymentId={deploymentDetails.id}
          isOpen={isCreateNewLicenseModalOpen}
          setIsOpen={setIsCreateNewLicenseModalOpen}
          onLicenseCreated={() =>
            fetchDeploymentDetails({
              deploymentId,
              setIsLoading,
              setDeploymentDetails,
            })
          } // Pass the callback here
        />
        <Panel style={{ overflow: "scroll" }}>
          <Tabs
            style={{ padding: "5px" }}
            defaultActiveKey={activeTabKey}
            activeKey={activeTabKey}
            onChange={handleTabChange}
            items={tabItems}
          />
        </Panel>
        <MoveToDataModal
          headingIcon={<UndoIcon />}
          heading="Rollback"
          open={isRevertStateModalOpen}
          onCancel={handleCancelRevert}
          width={800}
          footerButtonArea={
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                padding: "0 16px",
                width: "100%",
              }}
            >
              <div style={{ display: "flex", gap: "8px" }}>
                <MoveToDataButton
                  icon={<TickIcon />}
                  intent="action"
                  key="submit"
                  onClick={handleConfirmRevert}
                >
                  Revert
                </MoveToDataButton>
                <MoveToDataButton key="cancel" onClick={handleCancelRevert}>
                  Cancel
                </MoveToDataButton>
              </div>
            </div>
          }
        >
          {selectedRecord && <SelectedTable selectedRecord={selectedRecord} />}
        </MoveToDataModal>
      </PanelGroup>
    </>
  );
};

export default Configuration;
