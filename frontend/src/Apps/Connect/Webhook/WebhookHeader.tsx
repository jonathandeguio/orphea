import CustomBreadCrumb from "components/Nav/Manage/breadCrumb";

import React, { useState } from "react";

import { ResourceTypeEnum } from "Apps/explorer/explorer.utils";
import { FormInstance, Popover } from "antd";
import { SaveIcon } from "assets/icons/boslerActionIcons";
import { GraphIcon } from "assets/icons/boslerChartIcons";
import { APIIcon } from "assets/icons/boslerInterfaceIcons";
import { PopOutIcon } from "assets/icons/boslerNavigationIcon";
import Avatars from "components/Avatars/Avatars";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import Comments from "components/Comments/Comments.view";
import { BoslerInfoPopover } from "components/CommonUI/BoslerInfoPopover/BoslerInfoPopover.view";
import SourcesTargets from "helpers/SourcesTargets";
import { useHotkeys } from "react-hotkeys-hook";
import { useDispatch } from "react-redux";
import { Link } from "react-router-dom";
import { getLanguageLabel } from "utils/utilities";
import { webhookExecution } from "../../../redux/webhookSlice";
import { ISourceConfig } from "../Sources/Source";
import { updateWebhookAPI } from "./Webhook.api";
import { IWebhook } from "./Webhook.types";
import { convertWebhookFieldsToString } from "./Webhook.utils";

interface IProps {
  source: ISourceConfig;
  webhook: IWebhook;
  form: FormInstance;
}

const WEBHOOK_SAVE_BTN = "WEBHOOK_SAVE_BTN";

const WebhookHeader = ({ source, webhook, form }: IProps) => {
  const dispatch = useDispatch();
  const [saveLoading, setSaveLoading] = useState<boolean>(false);
  const handleUpdate = async () => {};
  const updateWebhook = () => {
    if (!webhook) return;
    setSaveLoading(true);
    updateWebhookAPI(
      convertWebhookFieldsToString({ ...webhook, ...form.getFieldsValue() })
    )
      .then(({ data }) => {
        (window as any).makeButtonTemporarySuccess(WEBHOOK_SAVE_BTN);
      })
      .catch((error) => {
        (window as any).makeButtonTemporaryFailure(WEBHOOK_SAVE_BTN);
      })
      .finally(() => {
        setSaveLoading(false);
      });
  };

  useHotkeys("ctrl+S,meta+S", (event) => {
    event.preventDefault();
    handleUpdate();
  });

  const executeWebhook = () => {
    if (!webhook.id) return;
    dispatch(
      webhookExecution({
        executionLoading: true,
        executionResult: undefined,
        executionError: false,
      })
    );
    // executeWebhookAPI(webhook.id)
    //   .then(({ data }) => {
    //     dispatch(
    //       webhookExecution({
    //         executionLoading: false,
    //         executionResult: data,
    //         executionError: false,
    //       })
    //     );
    //   })
    //   .catch((error) => {
    //     dispatch(
    //       webhookExecution({
    //         executionLoading: false,
    //         executionResult: undefined,
    //         executionError: error.message,
    //       })
    //     );
    //   });
  };

  return (
    <div className="connect-container-header">
      <CustomBreadCrumb />
      <div className="connect-container-header-btns">
        <BoslerInfoPopover
          id={webhook.id as string}
          type={ResourceTypeEnum.WEBHOOK}
        />
        <Popover
          title={
            <Link to={`/portal/bezier/${source.id}/master`}>
              <div
                className="text-and-icon-center"
                style={{
                  justifyContent: "space-between",
                  width: "100%",
                  color: "var(--bosler-font-color-muted)",
                }}
              >
                {getLanguageLabel("dataLineage")}
                <PopOutIcon />
              </div>
            </Link>
          }
          content={
            <>
              <SourcesTargets id={source.id} branch={"master"} />
            </>
          }
          placement="bottom"
          overlayStyle={{ width: "20rem" }}
        >
          <Link to={`/portal/bezier/${source.id}/master`}>
            <BoslerButton
              icon={<GraphIcon />}
              icononly={true}
              minimal
              trimicononlypadding
            ></BoslerButton>
          </Link>
        </Popover>

        <Comments id={webhook.id} />
        <Avatars link={`/topic/${webhook.id}`} />
        <BoslerButton
          intent={"warning"}
          icon={<APIIcon />}
          onClick={executeWebhook}
        >
          Test
        </BoslerButton>
        <BoslerButton
          id={WEBHOOK_SAVE_BTN}
          intent={"action"}
          onClick={updateWebhook}
          icon={<SaveIcon />}
          loading={saveLoading}
        >
          Save
        </BoslerButton>
      </div>
    </div>
  );
};

export default WebhookHeader;
