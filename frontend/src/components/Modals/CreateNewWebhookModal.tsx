import { Form, Typography } from "antd";
import React, { useState } from "react";

import { useDispatch } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

import { createWebhookAPI } from "Apps/Connect/Webhook/Webhook.api";
import { IWebhook } from "Apps/Connect/Webhook/Webhook.types";
import { SourceIcon } from "assets/icons/boslerDataIcons";
import { TableIcon } from "assets/icons/boslerTableIcons";
import axios from "axios";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import { ErrorResponse } from "global";
import { InfoIcon } from "../../assets/icons/boslerMiscellaneousIcons";
import { TickIcon } from "../../assets/icons/boslerNavigationIcon";
import { openFileExplorerModal } from "../../redux/ModalSlice";
import { addNewResource } from "../../redux/fileIndexSlice";
import { ThunkAppDispatch } from "../../redux/types/store";
import BoslerInput from "../BoslerComponents/InputComponent/BoslerInput";

const { Text } = Typography;

interface IProps {
  isVisible: boolean;
  setIsVisible: any;
  defaultParent?: string;
}

export default ({ isVisible, setIsVisible, defaultParent }: IProps) => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const [form] = Form.useForm();
  const [webhookDetails, setWebhookDetails] = useState<IWebhook>({
    name: "Webhook",
    parent: defaultParent,
    sourceId: undefined,
    requests: [],
  });
  const [sourceName, setSourceName] = useState<string>();

  const onCreateNew = (child: any) => {
    dispatch(addNewResource(child));
  };

  const onSelectSource = ({ id, name, path, metaData }: any) => {
    setWebhookDetails({
      ...webhookDetails,
      sourceId: id,
    });
    setSourceName(name);
  };

  const handleOk = (webhookDetails: IWebhook) => {
    if (sourceName) {
      createWebhookAPI(webhookDetails)
        .then((data: $TSFixMe) => {
          setIsVisible(false);
          onCreateNew(data);
        })
        .catch((err: $TSFixMe) => {
          if (axios.isAxiosError(err) && isDefined(err.response)) {
            const data = err?.response?.data as ErrorResponse;
            const error = data.error;
            const description = data.description;

            openNotification(error, description, "error");
          }
        });
    }
  };

  return (
    <Form
      form={form}
      initialValues={{ groups: true, folders: true }}
      onKeyPress={(e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          form.submit();
        }
      }}
      onFinish={(values) => {
        handleOk({
          ...webhookDetails,
          name: values.name,
          description: values.description,
          parent: defaultParent,
        });
      }}
    >
      <BoslerModal
        heading={"Webhook"}
        headingIcon={<TableIcon />}
        open={isVisible}
        onCancel={() => setIsVisible(false)}
        footerExtraText={getLanguageLabel("accessMessage")}
        footerButtonArea={
          <BoslerButton
            intent="primary"
            onClick={() => form.submit()}
            icon={<TickIcon />}
          >
            {getLanguageLabel("create")}
          </BoslerButton>
        }
        information={
          <div style={{ padding: "15px", width: "200px" }}>
            <div className="text-and-icon-align">
              <InfoIcon />
              <Text strong>Info</Text>
            </div>
            <div style={{ paddingTop: "2px", paddingLeft: "20px" }}>
              <Text style={{ fontSize: "0.8rem" }}>Webhook description</Text>
            </div>
          </div>
        }
      >
        <div className="BoslerHeader1">{getLanguageLabel("name")}</div>
        <Form.Item
          name="name"
          rules={[
            {
              required: true,
            },
          ]}
        >
          <BoslerInput
            autofocus
            placeholder={"Name"}
            style={{ width: "20vw", minWidth: "300px" }}
          />
        </Form.Item>
        <div className="BoslerHeader1">{getLanguageLabel("description")}</div>
        <Form.Item name="description">
          <BoslerInput
            placeholder={getLanguageLabel("descriptionOpt")}
            style={{ width: "20vw", minWidth: "300px" }}
          />
        </Form.Item>
        <BoslerButton
          icon={<SourceIcon />}
          onClick={() => {
            dispatch(
              openFileExplorerModal({
                type: ["SOURCE"],
                action: (data) => {
                  onSelectSource(data);
                },
                activeId: defaultParent,
              })
            );
          }}
          style={{
            display: "inline-flex",
            alignItems: "center",
            cursor: "pointer",
          }}
          intent={sourceName ? "success" : "warning"}
        >
          {sourceName ? sourceName : getLanguageLabel("sources")}
        </BoslerButton>
      </BoslerModal>
    </Form>
  );
};
