import { Divider, Tooltip, Typography } from "antd";
import { InfoIcon } from "assets/icons/boslerMiscellaneousIcons";
import useEffectOnlyOnDependencyUpdate from "hooks/useEffectOnlyOnDependencyUpdate";
import React, { useEffect, useState } from "react";
import { getLanguageLabel } from "utils/utilities";
import { ILink } from "../Links/Link.types";
import WebhookResponseParams from "./WebhookResponseParams";

const { Text } = Typography;

interface IProps {
  form: any;
  setNoChanges: any;
  link: ILink;
}

const WebhookOutputDataset = ({ form, setNoChanges, link }: IProps) => {
  const [restPreviewResponseParam, setRestPreviewResponseParam] =
    useState<string>(link?.responseParam ?? "@completeresponse");

  useEffectOnlyOnDependencyUpdate(() => {
    if (restPreviewResponseParam) {
      console.log(">> FIRED 1");
      form.setFieldValue("responseParam", restPreviewResponseParam);
      setNoChanges(false);
    }
  }, [restPreviewResponseParam]);

  useEffect(() => {
    console.log(">> FIRED 2");
    form.setFieldValue("responseParam", restPreviewResponseParam);
  }, []);

  return (
    <div className="--p20">
      <div className="text-and-icon-center">
        <div className="BoslerHeader1">{getLanguageLabel("preProcess")}</div>
        <Tooltip title={getLanguageLabel("jsonApiOnly")}>
          <InfoIcon />
        </Tooltip>
      </div>
      <Divider style={{ marginTop: 0 }} className="--mb10" />
      <WebhookResponseParams
        restPreviewResponseParam={restPreviewResponseParam}
        setRestPreviewResponseParam={setRestPreviewResponseParam}
      />
    </div>
  );
};

export default WebhookOutputDataset;
