import axios from "axios";
import React, { Dispatch, SetStateAction } from "react";
import { useDispatch } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { TickIcon } from "assets/icons/boslerNavigationIcon";
import { getAllTriggerDetails } from "redux/actions/TriggerActions";
import { ThunkAppDispatch } from "redux/types/store";

import { Form, Select } from "antd";
import { AddIcon } from "assets/icons/boslerActionIcons";
import { ErrorResponse } from "global";
import BoslerModal from "components/BoslerModalContainer";
import BoslerButton from "components/ButtonComponent/BoslerButton";
import BoslerInput from "components/InputComponent/BoslerInput";
import { createTriggerAPI } from "../apis";
import { EditIcon } from "assets/icons/boslerEditorIcons";

interface Props {
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
  selectedRecord: any;
}

const { Item } = Form;

const EditTriggerModal = ({ isOpen, setIsOpen, selectedRecord }: Props) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [form] = Form.useForm();
  const {
    name,
    description,
    branch,
    configFileName,
    buildType,
    harborProjectName,
  } = selectedRecord;

  return (
    <Form
      form={form}
      labelCol={{ span: 8 }}
      wrapperCol={{ span: 16 }}
      labelAlign="left"
      colon={false}
      onKeyPress={(e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          form.submit();
        }
      }}
      initialValues={{
        repositoryBranch: "main",
      }}
      onFinish={() => {
        createTriggerAPI(form.getFieldsValue())
          .then(() => dispatch(getAllTriggerDetails()))
          .catch((error) => {
            if (axios.isAxiosError(error) && isDefined(error.response)) {
              const errorMessage = (error?.response?.data as ErrorResponse)
                .error;
              openNotification(
                errorMessage,
                "Please contact platform admin.",
                "error"
              );
            }
          })
          .finally(() => setIsOpen(false));
      }}
    >
      <BoslerModal
        headingIcon={<EditIcon />}
        heading={`Edit ${name} trigger`}
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        footerButtonArea={
          <Item>
            <BoslerButton
              icon={<TickIcon />}
              intent="action"
              htmlType="submit"
              onClick={() => form.submit()}
            >
              {getLanguageLabel("edit")}
            </BoslerButton>
          </Item>
        }
        width={600}
      >
        <Item label="Description" name="description">
          <BoslerInput defaultValue={description} />
        </Item>

        <Item label="Docker File Name" name="configFileName">
          <BoslerInput defaultValue={configFileName} />
        </Item>

        <Item label="Registry" name="harborProjectName">
          <BoslerInput defaultValue={harborProjectName} />
        </Item>

        <Item label="Build Type" name="buildType">
          <Select
            defaultValue={buildType}
            options={[
              {
                value: "manual",
                label: "Manual",
              },
              {
                value: "automatic",
                label: "Automatic",
              },
            ]}
          />
        </Item>

        <span className="BoslerHeader1">{getLanguageLabel("repository")}</span>
        <Item
          label="Branch"
          name="repositoryBranch"

          //   rules={[{ required: true }]}
        >
          <BoslerInput defaultValue={branch} />
        </Item>
      </BoslerModal>
    </Form>
  );
};

export default EditTriggerModal;
