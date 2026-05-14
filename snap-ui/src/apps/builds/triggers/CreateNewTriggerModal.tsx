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

interface Props {
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
}

const { Item } = Form;

const CreateNewTriggerModal = ({ isOpen, setIsOpen }: Props) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [form] = Form.useForm();

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
        headingIcon={<AddIcon />}
        heading={"New Trigger"}
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
              {getLanguageLabel("create")}
            </OrpheaButton>
          </Item>
        }
        width={600}
      >
        <Item label="Name" name="name" rules={[{ required: true }]}>
          <OrpheaInput autofocus />
        </Item>

        <Item
          label="Description"
          name="description"
          rules={[{ required: true }]}
        >
          <OrpheaInput />
        </Item>

        <Item
          label="Docker File Name"
          name="configFileName"
          rules={[{ required: true }]}
        >
          <OrpheaInput />
        </Item>

        <Item
          label="Registry"
          name="harborProjectName"
          rules={[{ required: true }]}
        >
          <OrpheaInput />
        </Item>

        <Item label="Build Type" name="buildType" rules={[{ required: true }]}>
          <Select
            defaultValue="Manual"
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
        <Item label="Name" name="repositoryName" rules={[{ required: true }]}>
          <OrpheaInput />
        </Item>
        <Item label="URL" name="repositoryURL" rules={[{ required: true }]}>
          <OrpheaInput />
        </Item>
        <Item
          label="Branch"
          name="repositoryBranch"
          rules={[{ required: true }]}
        >
          <OrpheaInput />
        </Item>
      </OrpheaModal>
    </Form>
  );
};

export default CreateNewTriggerModal;
