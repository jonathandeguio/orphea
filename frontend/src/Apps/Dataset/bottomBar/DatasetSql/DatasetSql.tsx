import { previewDatasetBySqlAPI } from "Apps/Dataset/Dataset.api";
import { CollapserHandler } from "components/BoslerComponents/ResizablePane/ResizablePaneUtil";
import PreviewResultDataset from "components/Builds/PreviewResultDataset";
import React, { useRef, useState } from "react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import { encodeToBase64, getLanguageLabel } from "utils/utilities";
import BoslerSwitch from "../../../../components/CommonUI/BoslerSwitch/BoslerSwitch";
import {
  IPreviewDatasetBySQL,
  IPreviewDatasetBySQLResults,
} from "./DatasetSql.types";
import DatasetSqlEditor from "./DatasetSqlEditor";
import DatasetSqlHistory from "./DatasetSqlHistory";

type TSqlEditorTab = "editor" | "history";
interface IProps {
  id: string;
  branch: string;
  transactionId: string;
}
const DatasetSql = ({ id, branch, transactionId }: IProps) => {
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>();
  const [selectedTab, setSelectedTab] = useState<TSqlEditorTab>("editor");
  const [previewResults, setPreviewResults] =
    useState<IPreviewDatasetBySQLResults>();
  const primaryPanelRef = useRef();

  const handleQueryExecution = (query: string) => {
    setIsLoading(true);
    setErrorMessage(undefined);
    const payload: IPreviewDatasetBySQL = {
      datasetId: id,
      branch: branch,
      transactionId: transactionId,
      query: encodeToBase64(query),
    };
    previewDatasetBySqlAPI(payload)
      .then(({ data }) => {
        setPreviewResults(data);
      })
      .catch((error) => {
        setErrorMessage(
          error?.response?.data
            ? error?.response?.data?.message ||
                error?.response?.data?.description
            : error.message
        );
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  return (
    <PanelGroup direction={"horizontal"}>
      <Panel>
        <BoslerSwitch
          items={[
            {
              label: getLanguageLabel("editor"),
              value: "editor",
              children: (
                <DatasetSqlEditor
                  handleQueryExecution={handleQueryExecution}
                  datasetId={id}
                />
              ),
            },
            {
              label: getLanguageLabel("history"),
              value: "history",
              children: <DatasetSqlHistory datasetId={id} branch={branch} />,
            },
          ]}
          value={selectedTab}
          onChange={(newVal: TSqlEditorTab) => {
            setSelectedTab(newVal);
          }}
        />
      </Panel>
      <PanelResizeHandle className="resizablePane-collapser">
        <CollapserHandler primaryPanelRef={primaryPanelRef} />
      </PanelResizeHandle>
      <Panel>
        <PreviewResultDataset
          data={previewResults}
          isLoading={isLoading}
          error={errorMessage}
        />
      </Panel>
    </PanelGroup>
  );
};

export default DatasetSql;
