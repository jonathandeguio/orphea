import { Form } from "antd";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import { CollapserHandler } from "components/BoslerComponents/ResizablePane/ResizablePaneUtil";
import NoData from "components/CommonUI/NoData";
import BoslerLoader from "components/boslerLoader";
import React, { useEffect, useRef, useState } from "react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import { useParams } from "react-router";
import { getSourceDetailsAPI } from "../Connect.api";
import { ISourceConfig } from "../Sources/Source";
import { getWebhookAPI } from "./Webhook.api";
import { IWebhook } from "./Webhook.types";
import { convertWebhookFieldsToJson } from "./Webhook.utils";
import WebhookDetails from "./WebhookDetails";
import WebhookExecution from "./WebhookExecution";
import WebhookHeader from "./WebhookHeader";

const Webhook = () => {
  const { id } = useParams();
  const [webhookData, setWebhookData] = useState<IWebhook>();
  const [restSource, setRestSource] = useState<ISourceConfig>();
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | false>(false);
  const primaryPanelRef = useRef<any>(null);
  const secondaryPanelRef = useRef<any>(null);
  const [form] = Form.useForm();

  const getWebhook = (webhookId: string) => {
    setError(false);
    setIsLoading(true);
    getWebhookAPI(webhookId)
      .then(({ data }) => {
        setWebhookData(convertWebhookFieldsToJson(data));
        getSourceDetailsAPI(data.sourceId)
          .then(({ data }) => {
            setRestSource(data);
          })
          .catch((error) => {
            throw error;
          });
      })
      .catch((error) => {
        setError(error.message);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  useEffect(() => {
    if (id) {
      getWebhook(id);
    }
  }, [id]);

  if (error) {
    return (
      <NoData
        heading={"Failed to get webhook"}
        subHeading={error}
        icon={<SearchEmptyState />}
      />
    );
  }

  if (isLoading) {
    return <BoslerLoader />;
  }

  if (!restSource || !webhookData) {
    return <NoData heading={"No such webhook"} icon={<SearchEmptyState />} />;
  }

  return (
    <div className="connect-container" style={{ height: "calc(100%)" }}>
      <WebhookHeader source={restSource} webhook={webhookData} form={form} />
      <PanelGroup direction={"horizontal"}>
        <Panel collapsible={true} defaultSize={20} ref={primaryPanelRef}>
          <WebhookDetails source={restSource} webhook={webhookData} />
        </Panel>
        <PanelResizeHandle className="resizablePane-collapser">
          <CollapserHandler primaryPanelRef={primaryPanelRef} />
        </PanelResizeHandle>
        <Panel>
          {/* <WebhookAPIs form={form} webhook={webhookData} source={restSource} /> */}
        </Panel>
        <PanelResizeHandle className="resizablePane-collapser">
          <CollapserHandler primaryPanelRef={secondaryPanelRef} />
        </PanelResizeHandle>
        <Panel defaultSize={30} collapsible={true} ref={secondaryPanelRef}>
          <WebhookExecution webhookId={id as string} />
        </Panel>
      </PanelGroup>
    </div>
  );
};

export default Webhook;
