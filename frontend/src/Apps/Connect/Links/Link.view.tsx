import { Form, Tabs, Typography } from "antd";
import TabPane from "antd/es/tabs/TabPane";
import BoslerLoader from "components/boslerLoader";

import React, { useEffect, useRef, useState } from "react";
import { useHotkeys } from "react-hotkeys-hook";
import { useDispatch } from "react-redux";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import { useParams } from "react-router-dom";
import {
  decodeFromBase64,
  getLanguageLabel,
  notEmpty,
  openNotification,
} from "utils/utilities";
import { CollapserHandler } from "../../../components/BoslerComponents/ResizablePane/ResizablePaneUtil";
import { ThunkAppDispatch } from "../../../redux/types/store";
import {
  ConnectBuildAPI,
  getConnectElementAPI,
  getDatasetAPI,
  getParentAPI,
} from "../Connect.api";
import { initialLinkDetails } from "./Link.constants";

import { BottomBarLayout } from "common/components/BoslerLayout/BottomBarLayout";
import {
  initBottomBar,
  updateBottomBarItemState,
} from "common/components/BoslerLayout/bottomBarSlice";
import BoslerSwitch from "components/CommonUI/BoslerSwitch/BoslerSwitch";
import CsvPreprocessing from "components/CsvPreprocessing";
import { ICsvPreprocessing } from "components/CsvPreprocessing/CsvPreprocessing.types";
import { updateSourceQuery } from "../../../redux/actions/sourceActions";
import DbmsSourceTree from "../Sources/DbmsSourceTree";
import { SharepointSourceTree } from "../Sources/SharepointSourceTree";
import { initialSourceDetails } from "../Sources/Source.constants";
import { convertWebhookRequestsFieldsToJson } from "../Webhook/Webhook.utils";
import WebhookAPIs from "../Webhook/WebhookAPIs";
import WebhookExecution from "../Webhook/WebhookExecution";
import WebhookOutputDataset from "../Webhook/WebhookOutputDataset";
import { ILink } from "./Link.types";
import LinkEditor from "./LinkEditor";
import LinkHeader from "./LinkHeader.view";
import LinkInfoPanel from "./LinkInfoPanel";
import { getLinkBottombarItems } from "./link.utils";

const { Text } = Typography;
type TBoslerSwitch = "dataBrowser" | "info";

const LinkDetails = () => {
  const primaryPanelRef = useRef<any>(null);
  const secondaryPanelRef = useRef<any>(null);
  const [form] = Form.useForm();
  const { id } = useParams();

  const dispatch = useDispatch<ThunkAppDispatch>();
  const [parent, setParent] = useState({ name: "", id: "" });
  const [link, setLink] = useState<ILink>({ ...initialLinkDetails });

  const [dataset, setDataset] = useState({ name: "", id: "", branch: "" });
  const [source, setSource] = useState(initialSourceDetails);

  const [noChanges, setNoChanges] = useState(true);
  const [buildID, setBuildID] = useState(undefined);
  const [selectedDataSourceSwitch, setSelectedDataSourceSwitch] = useState<
    "dataBrowser" | "info"
  >("dataBrowser");

  const [selectedDataOutputType, setSelectedDataOutputType] = useState<
    "json" | "csv"
  >("json");

  const getLink = () => {
    getConnectElementAPI(id as string, "link").then(({ data }) => {
      if (data.type == "rest") {
        data.requests = convertWebhookRequestsFieldsToJson(data?.requests);
      }
      setLink(data);
      if (data.type.toUpperCase() === "JDBC") {
        dispatch(updateSourceQuery(id, decodeFromBase64(data.script)));
        setNoChanges(true);
      }
      getParentAPI(data.parent).then(({ data: p }) => setParent(p));

      if (data.datasetId) {
        if (!link.dataLiveLoad) {
          getDatasetAPI(data.datasetId).then(({ data: d }) => setDataset(d));
        }
      }

      getConnectElementAPI(data.sourceId, "source").then(({ data: s }) =>
        setSource(s)
      );
    });
  };

  const build = (id: $TSFixMe) => {
    if (!link.dataLiveLoad) {
      dispatch(
        updateBottomBarItemState({
          id: "datasetBuildLogPanel",
          openPane: true,
          props: {
            showEmpty: false,
          },
        })
      );
      ConnectBuildAPI(id).then(({ data }) => {
        setBuildID(data.id);
        dispatch(
          updateBottomBarItemState({
            id: "datasetBuildLogPanel",
            props: {
              id: data.id,
            },
          })
        );
        openNotification(
          "Build Started",
          <a href={`/portal/builds/${data.id}`}>Click to view logs</a>,
          "success"
        );
      });
    }
  };

  useEffect(() => {
    getLink();
  }, [id]);

  useEffect(() => {
    if (notEmpty(link.id)) {
      dispatch(
        initBottomBar({
          leftItems: getLinkBottombarItems(
            link.datasetId,
            link.id,
            link.branch,
            link.dataLiveLoad,
            buildID,
            source,
            ""
          ),
        })
      );
    }
  }, [link, source]);

  useHotkeys("ctrl+B,meta+B", (event) => {
    event.preventDefault();
    build(link.id);
  });

  if (!link || !link.id) {
    return <BoslerLoader />;
  }

  return (
    <BottomBarLayout>
      <div className="connect-container" style={{ height: "calc(100%)" }}>
        <LinkHeader
          linkDetails={link}
          setLinkDetails={setLink}
          dataset={dataset}
          source={source}
          parent={parent}
          updateLink={getLink}
          handleBuild={build}
          noChanges={noChanges}
          setNoChanges={setNoChanges}
          form={form}
        />

        <PanelGroup direction={"horizontal"}>
          {source.type === "rest" ? (
            <Panel
              style={{ overflowY: "scroll" }}
              collapsible={true}
              defaultSize={10}
              ref={primaryPanelRef}
            >
              <div>
                <LinkInfoPanel
                  dataset={dataset}
                  source={source}
                  getLink={getLink}
                  link={link}
                />
                <BoslerSwitch
                  items={[
                    {
                      label: "JSON",
                      value: "json",
                      children: (
                        <WebhookOutputDataset
                          form={form}
                          setNoChanges={setNoChanges}
                          link={link}
                        />
                      ),
                    },
                    {
                      label: "CSV",
                      value: "csv",
                      children: (
                        <CsvPreprocessing
                          initialValues={link.csvPreprocessing}
                          onValuesChange={(
                            values: ICsvPreprocessing,
                            allValues: ICsvPreprocessing
                          ) => {
                            setNoChanges(false);
                            form.setFieldValue("csvPreprocessing", allValues);
                          }}
                        />
                      ),
                    },
                  ]}
                  value={selectedDataOutputType}
                  onChange={(newSwitch: "json" | "csv") => {
                    setSelectedDataOutputType(newSwitch);
                  }}
                />
              </div>
            </Panel>
          ) : (
            <Panel collapsible={true} defaultSize={25} ref={primaryPanelRef}>
              <BoslerSwitch
                items={[
                  {
                    label: getLanguageLabel("dataBrowser"),
                    value: "dataBrowser",
                    children:
                      source &&
                      (source.type === "SHAREPOINT" ? (
                        <SharepointSourceTree source={source} />
                      ) : (
                        <DbmsSourceTree sourceId={source.id} page={"SOURCE"} />
                      )),
                  },
                  {
                    label: getLanguageLabel("info"),
                    value: "info",
                    children: (
                      <LinkInfoPanel
                        dataset={dataset}
                        source={source}
                        getLink={getLink}
                        link={link}
                      />
                    ),
                  },
                ]}
                value={selectedDataSourceSwitch}
                onChange={(newSwitch: TBoslerSwitch) => {
                  setSelectedDataSourceSwitch(newSwitch);
                }}
              />
            </Panel>
          )}
          <PanelResizeHandle className="resizablePane-collapser">
            <CollapserHandler primaryPanelRef={primaryPanelRef} />
          </PanelResizeHandle>
          <Panel>
            <>
              {link.type.toUpperCase() === "FOLDER" && (
                <>
                  <Tabs
                    type="card"
                    style={{
                      whiteSpace: "pre-wrap",
                    }}
                    tabBarStyle={{
                      padding: "0 0.5rem",
                    }}
                  >
                    <TabPane
                      tab={getLanguageLabel("subFolder")}
                      key="1"
                      style={{ padding: "0.5rem" }}
                    >
                      <Text>{link.script}</Text>
                    </TabPane>
                  </Tabs>
                </>
              )}
              {link.type.toUpperCase() === "SHAREPOINT" && (
                <>
                  <Tabs
                    type="card"
                    style={{
                      whiteSpace: "pre-wrap",
                    }}
                    tabBarStyle={{
                      padding: "0 0.5rem",
                    }}
                  >
                    <TabPane
                      tab={"FILE ID"}
                      key="1"
                      style={{ padding: "0.5rem" }}
                    >
                      <Text>{link.fileId}</Text>
                    </TabPane>
                  </Tabs>
                </>
              )}
              {link.type.toUpperCase() === "JDBC" && (
                <LinkEditor
                  link={link}
                  source={source}
                  build={build}
                  noChanges={noChanges}
                  setNoChanges={setNoChanges}
                />
              )}
              {link.type === "rest" && (
                <PanelGroup direction={"horizontal"}>
                  <Panel>
                    <WebhookAPIs
                      form={form}
                      webhook={link}
                      source={source}
                      setNoChanges={setNoChanges}
                    />
                  </Panel>
                  <PanelResizeHandle className="resizablePane-collapser">
                    <CollapserHandler
                      alignButton="left"
                      primaryPanelRef={secondaryPanelRef}
                    />
                  </PanelResizeHandle>
                  <Panel
                    defaultSize={30}
                    collapsible={true}
                    ref={secondaryPanelRef}
                  >
                    <WebhookExecution webhookId={id as string} />
                  </Panel>
                </PanelGroup>
              )}
            </>
          </Panel>
        </PanelGroup>
      </div>
    </BottomBarLayout>
  );
};
export default LinkDetails;
