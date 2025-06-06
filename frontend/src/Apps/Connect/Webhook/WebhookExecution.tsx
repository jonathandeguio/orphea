import { Divider, Row } from "antd";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import UserInfo from "common/components/UserInfo";
import BoslerSwitch from "components/CommonUI/BoslerSwitch/BoslerSwitch";
import NoData from "components/CommonUI/NoData";
import BoslerLoader from "components/boslerLoader";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { getLanguageLabel, getTimeDisplay } from "utils/utilities";
import { webhookExecution } from "../../../redux/webhookSlice";
import RestAPIConnectorTest from "../Sources/RestAPIConnector/RestAPIConnectorTest";
import { webhookExecutionResulsAPI } from "./Webhook.api";
import { IWebhookExecutionResult } from "./Webhook.types";

type TSwitchValues = "current" | "all";

interface IAllExecutionsProps {
  webhookId: string;
}

interface IProps {
  webhookId: string;
}

const CurrentExecution = () => {
  const { executionLoading, executionResult, executionError } = useSelector(
    (state: RootState) => state.webhook
  );

  if (executionError) {
    return <NoData icon={<SearchEmptyState />} heading={executionError} />;
  }

  if (executionLoading) {
    return <BoslerLoader />;
  }

  if (!executionResult) {
    return (
      <NoData
        icon={<SearchEmptyState />}
        heading={getLanguageLabel("startPreviewToResults")}
      />
    );
  }

  return <RestAPIConnectorTest result={executionResult} />;
};

const AllExecutions = ({ webhookId }: IAllExecutionsProps) => {
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isError, setIsError] = useState<false | string>(false);

  const [executionResult, setExecutionResult] =
    useState<IWebhookExecutionResult[]>();

  const getAllExecutions = () => {
    setIsLoading(true);
    setIsError(false);
    webhookExecutionResulsAPI(webhookId)
      .then(({ data }) => {
        setExecutionResult(data);
      })
      .catch((error) => {
        setIsError(error.message);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  useEffect(() => {
    getAllExecutions();
  }, [webhookId]);

  if (isLoading) {
    return <BoslerLoader />;
  }

  if (isError) {
    return <NoData icon={<SearchEmptyState />} heading={isError} />;
  }

  if (!executionResult) {
    return <NoData icon={<SearchEmptyState />} heading={"No executions!"} />;
  }

  return (
    <div>
      {executionResult.map((result) => (
        <>
        <RestAPIConnectorTest result={result.calls} />
          <Row justify={"end"}><UserInfo userId={result.executedBy} /></Row>
          <Row justify={"end"}>{getTimeDisplay(result.executedAt)}</Row>
          <Divider />
        </>
      ))}
    </div>
  );
};

const WebhookExecution = ({ webhookId }: IProps) => {
  const dispatch = useDispatch();
  const [currentPanel, setCurrentPanel] = useState<TSwitchValues>("current");

  useEffect(() => {
    return () => {
      dispatch(
        webhookExecution({
          executionLoading: false,
          executionResult: undefined,
          executionError: false,
        })
      );
    };
  }, [webhookId]);

  return (
    <BoslerSwitch
      items={[
        {
          label: getLanguageLabel("currentExecution"),
          value: "current",
          children: <CurrentExecution />,
        },
        {
          label: getLanguageLabel("allExecutions"),
          value: "all",
          children: <AllExecutions webhookId={webhookId} />,
        },
      ]}
      value={currentPanel}
      onChange={(newVal: TSwitchValues) => {
        setCurrentPanel(newVal);
      }}
    />
  );
};

export default WebhookExecution;
