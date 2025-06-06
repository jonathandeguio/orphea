import { Form } from "antd";
import { EditIcon } from "assets/icons/boslerEditorIcons";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

const ChangeDescModal = ({
  changeDescServiceDetails,
  setChangeDescServiceDetails,
  handleUpdate,
}: any) => {
  const [form] = Form.useForm();
  return (
    <Form
      form={form}
      initialValues={{ description: changeDescServiceDetails.desc }}
      onKeyPress={(e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          form.submit();
        }
      }}
      onFinish={(values) => {
        handleUpdate(changeDescServiceDetails.id, values.description);
      }}
    >
      <BoslerModal
        open={changeDescServiceDetails.modalView}
        onCancel={() =>
          setChangeDescServiceDetails({
            ...changeDescServiceDetails,
            modalView: false,
          })
        }
        headingIcon={<EditIcon />}
        heading={getLanguageLabel("changeDescription")}
        footerButtonArea={
          <BoslerButton
            intent="action"
            onClick={() => form.submit()}
            // icon={<SaveIcon />}
            textTransform="none"
          >
            {getLanguageLabel("update")}
          </BoslerButton>
        }
      >
        <Form.Item name="description">
          <BoslerInput
            bordered
            autoselect
            placeholder={getLanguageLabel("newDescription")}
          />
        </Form.Item>
      </BoslerModal>
    </Form>
  );
};

export default ChangeDescModal;
