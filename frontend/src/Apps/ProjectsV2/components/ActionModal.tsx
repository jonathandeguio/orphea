import { Form } from "antd";
import { FORM_FIELDS } from "Apps/ProjectsV2/utils/Projects.utils";
import { TickIcon } from "assets/icons/boslerNavigationIcon";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React from "react";
import { getLanguageLabel } from "utils/utilities";

interface IProps {
  heading?: string;
  prefilled: string;
  isOpen: boolean;
  close: () => void;
  handleAction: any;
}

const ActionModal = ({
  isOpen,
  close,
  handleAction,
  prefilled,
  heading = "Change Name",
}: IProps) => {
  const [form] = Form.useForm();
  return (
    <Form
      form={form}
      labelCol={{ span: 24 }}
      wrapperCol={{ span: 24 }}
      colon={false}
      initialValues={{ [FORM_FIELDS.NAME]: prefilled }}
      onFinish={() => handleAction(form.getFieldValue(FORM_FIELDS.NAME))}
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
              intent="success"
              htmlType="submit"
              onClick={() => form.submit()}
              icon={<TickIcon />}
            >
              {getLanguageLabel("update")}
            </BoslerButton>
          </Form.Item>
        }
        heading={heading}
      >
        <Form.Item name={FORM_FIELDS.NAME}>
          <BoslerInput />
        </Form.Item>
      </BoslerModal>
    </Form>
  );
};

export default ActionModal;
