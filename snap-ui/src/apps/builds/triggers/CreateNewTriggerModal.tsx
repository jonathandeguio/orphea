import axios from "axios";
import React, { Dispatch, SetStateAction } from "react";
import { useDispatch } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { TickIcon } from "assets/icons/movetodataNavigationIcon";
import { getAllTriggerDetails } from "redux/actions/TriggerActions";
import { ThunkAppDispatch } from "redux/types/store";

import { Form, Select } from "antd";
import { AddIcon } from "assets/icons/movetodataActionIcons";
import { ErrorResponse } from "global";
import MoveToDataModal from "components/MoveToDataModalContainer";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";
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
      <MoveToDataModal
        headingIcon={<AddIcon />}
        heading={"New Trigger"}
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        footerButtonArea={
          <Item>
            <MoveToDataButton
              icon={<TickIcon />}
              intent="action"
              htmlType="submit"
              onClick={() => form.submit()}
            >
              {getLanguageLabel("create")}
            </MoveToDataButton>
          </Item>
        }
        width={600}
      >
        <Item label="Name" name="name" rules={[{ required: true }]}>
          <MoveToDataInput autofocus />
        </Item>

        <Item
          label="Description"
          name="description"
          rules={[{ required: true }]}
        >
          <MoveToDataInput />
        </Item>

        <Item
          label="Docker File Name"
          name="configFileName"
          rules={[{ required: true }]}
        >
          <MoveToDataInput />
        </Item>

        <Item
          label="Registry"
          name="harborProjectName"
          rules={[{ required: true }]}
        >
          <MoveToDataInput />
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

        <span className="MoveToDataHeader1">{getLanguageLabel("repository")}</span>
        <Item label="Name" name="repositoryName" rules={[{ required: true }]}>
          <MoveToDataInput />
        </Item>
        <Item label="URL" name="repositoryURL" rules={[{ required: true }]}>
          <MoveToDataInput />
        </Item>
        <Item
          label="Branch"
          name="repositoryBranch"
          rules={[{ required: true }]}
        >
          <MoveToDataInput />
        </Item>
      </MoveToDataModal>
    </Form>
  );
};

export default CreateNewTriggerModal;
