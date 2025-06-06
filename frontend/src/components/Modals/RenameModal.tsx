import { Form } from "antd";
import { EditIcon } from "assets/icons/boslerEditorIcons";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

const RenameModal = ({
  renameServiceDetails,
  setRenameServiceDetails,
  handleUpdate,
}: any) => {
  const [form] = Form.useForm();

  return (
    <Form
      form={form}
      initialValues={{ name: renameServiceDetails.name }}
      onKeyPress={(e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          form.submit();
          setRenameServiceDetails({
            ...renameServiceDetails,
            modalView: false,
          });
        }
      }}
      onFinish={(values) => {
        handleUpdate(renameServiceDetails.id, values.name);
      }}
    >
      <BoslerModal
        open={renameServiceDetails.modalView}
        onCancel={() =>
          setRenameServiceDetails({ ...renameServiceDetails, modalView: false })
        }
        headingIcon={<EditIcon />}
        heading={"Rename"}
        footerButtonArea={
          <BoslerButton intent="action" onClick={() => form.submit()}>
            {getLanguageLabel("rename")}
          </BoslerButton>
        }
      >
        Rename "{renameServiceDetails.name}"
        <Form.Item name="name">
          <BoslerInput
            autoselect
            placeholder={getLanguageLabel("newName")}
            defaultValue={renameServiceDetails.name}
          />
        </Form.Item>
      </BoslerModal>
    </Form>
  );
};

export default RenameModal;
