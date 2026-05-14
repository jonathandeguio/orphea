import axios from "axios";
import React, { Dispatch, SetStateAction } from "react";
import { useDispatch } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { TickIcon } from "assets/icons/orpheaNavigationIcon";
import { getAllTriggerDetails } from "redux/actions/TriggerActions";
import { ThunkAppDispatch } from "redux/types/store";

import { Form, Select } from "antd";
import { AddIcon } from "assets/icons/orpheaActionIcons";
import { ErrorResponse } from "global";
import OrpheaModal from "components/OrpheaModalContainer";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
import OrpheaInput from "components/InputComponent/OrpheaInput";
import { createTriggerAPI } from "../apis";
import { EditIcon } from "assets/icons/orpheaEditorIcons";

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
      <OrpheaModal
        headingIcon={<EditIcon />}
        heading={`Edit ${name} trigger`}
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        footerButtonArea={
          <Item>
            <OrpheaButton
              icon={<TickIcon />}
              intent="action"
              htmlType="submit"
              onClick={() => form.submit()}
            >
              {getLanguageLabel("edit")}
            </OrpheaButton>
          </Item>
        }
        width={600}
      >
        <Item label="Description" name="description">
          <OrpheaInput defaultValue={description} />
        </Item>

        <Item label="Docker File Name" name="configFileName">
          <OrpheaInput defaultValue={configFileName} />
        </Item>

        <Item label="Registry" name="harborProjectName">
          <OrpheaInput defaultValue={harborProjectName} />
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

        <span className="OrpheaHeader1">{getLanguageLabel("repository")}</span>
        <Item
          label="Branch"
          name="repositoryBranch"

          //   rules={[{ required: true }]}
        >
          <OrpheaInput defaultValue={branch} />
        </Item>
      </OrpheaModal>
    </Form>
  );
};

export default EditTriggerModal;
