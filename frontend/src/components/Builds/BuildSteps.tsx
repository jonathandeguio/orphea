import { Col, Row, Steps, Typography } from "antd";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import { TBuildLog } from "./Builds.types";
import { getBuildSteps } from "./Builds.utils";
const { Title, Text } = Typography;
const { Step } = Steps;
interface TProps {
  datasetBuildLog: TBuildLog;
}
const BuildSteps = ({ datasetBuildLog }: TProps) => {
  if (!datasetBuildLog) {
    return null;
  }

  const buildSteps = getBuildSteps(datasetBuildLog);

  return (
    <Steps style={{ padding: "1rem" }} direction="vertical" size="small">
      <Step
        status={buildSteps.starting.status as any}
        icon={
          buildSteps.starting.status != "error" ? (
            <div
              className={
                buildSteps.starting.status == "wait"
                  ? "muted-tick-circle text-and-icon-center"
                  : buildSteps.starting.status == "process"
                  ? "text-and-icon-center"
                  : "success-tick-circle text-and-icon-center"
              }
            >
              {buildSteps.starting.icon}
            </div>
          ) : null
        }
        title={<Text>{buildSteps.starting.title}</Text>}
        description={
          <Row justify="end">
            <Col>{buildSteps.starting.description}</Col>
          </Row>
        }
      />
      <Step
        status={buildSteps.preparing.status as any}
        icon={
          buildSteps.preparing.status != "error" ? (
            <div
              className={
                buildSteps.preparing.status == "wait"
                  ? "muted-tick-circle text-and-icon-center"
                  : buildSteps.preparing.status == "process"
                  ? "text-and-icon-center"
                  : "success-tick-circle text-and-icon-center"
              }
            >
              {buildSteps.preparing.icon}
            </div>
          ) : null
        }
        title={<Text>{buildSteps.preparing.title}</Text>}
        description={
          <Row justify="end">
            <Col>{buildSteps.preparing.description}</Col>
          </Row>
        }
      />
      <Step
        status={buildSteps.running.status as any}
        icon={
          buildSteps.running.status != "error" ? (
            <div
              className={
                buildSteps.running.status == "wait"
                  ? "muted-tick-circle text-and-icon-center"
                  : buildSteps.running.status == "process"
                  ? "text-and-icon-center"
                  : "success-tick-circle text-and-icon-center"
              }
            >
              {buildSteps.running.icon}
            </div>
          ) : null
        }
        title={<Text>{buildSteps.running.title}</Text>}
        description={
          <Row justify="end">
            <Col>{buildSteps.running.description}</Col>
          </Row>
        }
      />
      <Step
        status={buildSteps.finished.status as any}
        icon={
          buildSteps.finished.status != "error" ? (
            <div
              className={
                buildSteps.finished.status == "wait"
                  ? "muted-tick-circle text-and-icon-center"
                  : buildSteps.finished.status == "process"
                  ? "text-and-icon-center"
                  : "success-tick-circle text-and-icon-center"
              }
            >
              {buildSteps.finished.icon}
            </div>
          ) : null
        }
        title={<Text>{buildSteps.finished.title}</Text>}
        description={
          <Row justify="end">
            <Col>
              {buildSteps.finished.description != ""
                ? `${getLanguageLabel("total")} : ${
                    buildSteps.finished.description
                  }`
                : ""}
            </Col>
          </Row>
        }
      />
    </Steps>
  );
};

export default BuildSteps;
