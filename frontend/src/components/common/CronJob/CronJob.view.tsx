import { Alert, Divider, Tooltip } from "antd";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React, { useState } from "react";
import Cron, { CronError } from "react-js-cron";
import { getUser, getUserLanguage } from "utils/utilities";
import {
  DEFAULT_CRON_JOB,
  DIVIDER_TEXT,
  ERROR,
  FRENCH_LOCALE,
} from "./CronJob.constants";

import { ScheduledRunIcon } from "assets/icons/boslerInterfaceIcons";
import { InfoIcon } from "assets/icons/boslerMiscellaneousIcons";
import BoslerHeader from "components/CommonUI/Header/BoslerHeader";
import "./CronJob.scss";
import { getExpressionString } from "./CronJob.services";

const CronJobInput = ({
  cronExpression,
  setCronExpression,
  showGeneralCronExplanations = false,
}: {
  cronExpression: string;
  setCronExpression: any;
  showGeneralCronExplanations?: boolean;
}) => {
  const [error, onError] = useState<CronError>();
  const userLan = getUserLanguage(getUser());

  return (
    <>
      {showGeneralCronExplanations && (
        <>
          <BoslerHeader
            heading="Schedule by Time"
            description="Schedule a dataset by time. To do so, either select time from selector
        below or write a cron expression. The scheduler will run everytime at
        that particular moment."
            icon={<ScheduledRunIcon />}
            borderBottom
          />
          <div className="--flex-col-center --mt20">
            <div className="cron-expression">
              <Tooltip title="Learn about cron expression">
                <a
                  href="https://en.wikipedia.org/wiki/Cron"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="info-icon"
                >
                  <InfoIcon />
                </a>
              </Tooltip>
              <pre>* * * * *</pre>
              <div className="line">
                <div>Minute</div>
                <div>Hour</div>
                <div>Day of Month</div>
                <div>Month</div>
                <div>Day of Week</div>
              </div>
            </div>
          </div>
          <Divider />
        </>
      )}
      <div className="--flex-col-center">
        <Cron
          value={cronExpression}
          setValue={(newValue: string) => {
            setCronExpression(newValue);
          }}
          onError={onError}
          humanizeLabels
          displayError
          className={"react-js-cron-custom"}
          clearButton={false}
          locale={userLan == "fr" ? FRENCH_LOCALE : {}}
        />
        <div
          onClick={() => {
            setCronExpression(DEFAULT_CRON_JOB);
          }}
        >
          {/* <BoslerButton intent="dangerous" icononly icon={<CrossIcon />} /> */}
        </div>
      </div>
      <Divider>{DIVIDER_TEXT}</Divider>
      <BoslerInput
        value={cronExpression}
        onChange={(event) => {
          setCronExpression(event.target.value);
        }}
        style={{ marginBottom: "0.5rem" }}
      />

      {!error && (
        <Alert
          type="info"
          message={getExpressionString(cronExpression, userLan)}
          style={{ marginBottom: "0.5rem" }}
          banner
        />
      )}
      {error && (
        <p style={{ marginTop: "0.5rem", marginBottom: "0.5rem" }}>
          {ERROR}: {error.description}
        </p>
      )}
    </>
  );
};

export default CronJobInput;
