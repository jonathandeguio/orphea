import { Form } from "antd";
import { FORM_FIELDS } from "Apps/ProjectsV2/utils/Projects.utils";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React from "react";
import { getLanguageLabel } from "utils/utilities";

interface IProps {
  resourceName: string;
  icon: React.ReactNode;
  isOpen: boolean;
  close: () => void;
  handleDelete: () => void;
}

const DeleteResourceModal = ({
  resourceName,
  icon,
  isOpen,
  close,
  handleDelete,
}: IProps) => {
  const [form] = Form.useForm();
  const name = Form.useWatch(FORM_FIELDS.NAME, form);
  return (
    <Form
      form={form}
      labelCol={{ span: 24 }}
      wrapperCol={{ span: 24 }}
      colon={false}
      onFinish={handleDelete}
      onKeyDown={(e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          form.submit();
        }
      }}
    >
      <BoslerModal
        open={isOpen}
        onCancel={close}
        footerButtonArea={
          <Form.Item>
            <BoslerButton
              intent="dangerous"
              htmlType="submit"
              disabled={name != resourceName}
              onClick={() => form.submit()}
              icon={<TrashIcon />}
            >
              {getLanguageLabel("delete")}
            </BoslerButton>
          </Form.Item>
        }
        headingIcon={<TrashIcon color={"var(--DANGEROUS_COLOR)"} />}
        heading={"Delete"}
      >
        <Form.Item
          label={
            <>
              Please type &nbsp; <strong>{resourceName}</strong> &nbsp; to
              confirm.
            </>
          }
          name={FORM_FIELDS.NAME}
        >
          <BoslerInput suffix={icon} placeholder={getLanguageLabel("name")} />
        </Form.Item>
      </BoslerModal>
    </Form>
  );
};

export default DeleteResourceModal;
