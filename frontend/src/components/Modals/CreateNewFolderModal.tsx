import { Form, Typography } from "antd";
import React, { useState } from "react";

import { useDispatch } from "react-redux";
import { getLanguageLabel, openNotification } from "utils/utilities";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

import { FolderIcon } from "assets/icons/boslerFileIcons";
import { createFolderApi } from "common/common.api";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import { LockIcon } from "../../assets/icons/boslerActionIcons";
import { InfoIcon } from "../../assets/icons/boslerMiscellaneousIcons";
import { TickIcon } from "../../assets/icons/boslerNavigationIcon";
import { addNewResource } from "../../redux/fileIndexSlice";
import { ThunkAppDispatch } from "../../redux/types/store";
import BoslerInput from "../BoslerComponents/InputComponent/BoslerInput";
import { ResourceTypeEnum } from "Apps/explorer/explorer.utils";

const { Text } = Typography;

export default ({ id, isVisible, setIsVisible }: any) => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const [loading, setLoading] = useState(false);

  const [form] = Form.useForm();

  const handleOk = (name: string, description: string) => {
    setLoading(true);
    if (name !== "") {
      createFolderApi({
        name: name,
        description: description,
        parent: id,
        type: ResourceTypeEnum.FOLDER,
      })
        .then(({ data }) => {
          if (data.id) {
            dispatch(addNewResource(data));
          }
        })
        .catch(({ response }: any) => {
          openNotification(
            response.data.error,
            response.data.description,
            "error"
          );
        })
        .finally(() => {
          setLoading(false);
          setIsVisible(false);
          form.resetFields();
        });
    }
  };

  return (
    <Form
      form={form}
      onFinish={(values) => handleOk(values.name, values.description)}
      labelCol={{ span: 24 }}
      wrapperCol={{ span: 24 }}
      colon={false}
      onKeyDown={(e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          form.submit();
        }
      }}
    >
      <BoslerModal
        open={isVisible}
        onCancel={() => setIsVisible(false)}
        headingIcon={<FolderIcon />}
        heading={getLanguageLabel("folder")}
        footerExtraText={getLanguageLabel("accessMessage")}
        footerButtonArea={
          <Form.Item>
            <BoslerButton
              loading={loading}
              intent="primary"
              htmlType={"submit"}
              onClick={() => form.submit()}
              icon={<TickIcon />}
            >
              {getLanguageLabel("create")}
            </BoslerButton>
          </Form.Item>
        }
        information={
          <div style={{ width: "200px" }}>
            <div className="text-and-icon-align">
              <InfoIcon />
              <Text strong>Info</Text>
            </div>
            <div style={{ paddingTop: "2px", paddingLeft: "20px" }}>
              <Text style={{ fontSize: "0.8rem" }}>
                {getLanguageLabel("folderMessage")}
              </Text>
            </div>
            <br />
            <div className="text-and-icon-align">
              <LockIcon />
              <Text strong>Permissions</Text>
            </div>
            <div style={{ paddingTop: "2px", paddingLeft: "20px" }}>
              <Text style={{ fontSize: "0.8rem" }}>
                {getLanguageLabel("folderPermissionsMessage")}
              </Text>
            </div>
          </div>
        }
      >
        <Form.Item
          name="name"
          label={getLanguageLabel("name").toUpperCase()}
          rules={[
            {
              required: true,
            },
          ]}
        >
          <BoslerInput autofocus placeholder={getLanguageLabel("folder")} />
        </Form.Item>

        <Form.Item
          name="description"
          label={getLanguageLabel("description").toUpperCase()}
        >
          <BoslerInput placeholder={getLanguageLabel("descriptionOpt")} />
        </Form.Item>
      </BoslerModal>
    </Form>
  );
};
