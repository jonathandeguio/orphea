import { AddUserIcon } from "assets/icons/boslerInterfaceIcons";
import { TickIcon } from "assets/icons/boslerNavigationIcon";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React, { Dispatch, SetStateAction } from "react";
import { getLanguageLabel } from "utils/utilities";
import { Form } from "antd";
import { getAllGroups } from "../../../../redux/actions/authActions";
import { ThunkAppDispatch } from "redux/types/store";
import { useDispatch } from "react-redux";
import { createGroupAPI } from "../Groups.api";
import TextArea from "antd/es/input/TextArea";

interface IProps {
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
}

const { Item } = Form;

export const CreateNewGroupModal = ({ isOpen, setIsOpen }: IProps) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [form] = Form.useForm();

  const handleCreate = () => {
    createGroupAPI(form.getFieldsValue()).then(() => {
      setIsOpen(false);
      dispatch(getAllGroups());
    });
  };
  return (
    <Form
      form={form}
      onFinish={handleCreate}
      onKeyPress={(e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          form.submit();
        }
      }}
    >
      <BoslerModal
        headingIcon={<AddUserIcon />}
        heading={getLanguageLabel("createNewGroup")}
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        footerButtonArea={
          <BoslerButton
            icon={<TickIcon />}
            intent="action"
            htmlType="submit"
            onClick={handleCreate}
          >
            {getLanguageLabel("create")}
          </BoslerButton>
        }
      >
        <div className="BoslerHeader1">{getLanguageLabel("groupName")}</div>
        <Item
          name="name"
          rules={[
            {
              required: true,
            },
          ]}
        >
          <BoslerInput required />
        </Item>
        <div className="BoslerHeader1">{getLanguageLabel("description")}</div>
        <Item name="description">
          <TextArea />
        </Item>
      </BoslerModal>
    </Form>
  );
};
