import { Form, Tooltip, Typography } from "antd";
import { AddUserIcon } from "assets/icons/movetodataInterfaceIcons";
import { createTokenAPI, fetchAllTokensAPI } from "apps/settings/apis";
import React, { Dispatch, SetStateAction, useState } from "react";
import { copyToClipboard, getLanguageLabel } from "utils/utilities";
import { CopyIcon } from "assets/icons/movetodataEditorIcons";
import { TickIcon } from "assets/icons/movetodataNavigationIcon";
import MoveToDataModal from "components/MoveToDataModalContainer/MoveToDataModal";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";
import MoveToDataDatePicker from "components/MoveToDataDatePicker";

const { Title, Text } = Typography;
const { Item } = Form;
interface Props {
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
  setTokens: Dispatch<SetStateAction<never[]>>;
}

const CreateNewTokenModal = ({ isOpen, setIsOpen, setTokens }: Props) => {
  const [token, setToken] = useState("");
  const [form] = Form.useForm();

  const handleCancel = () => {
    setIsOpen(false);
    setToken("");
  };
  const handleCopy = () => {
    copyToClipboard(token);
    setIsOpen(false);
  };

  return (
    <Form form={form}>
      <MoveToDataModal
        headingIcon={<AddUserIcon />}
        heading={
          token
            ? getLanguageLabel("tokenVisibleMsg")
            : getLanguageLabel("createNewToken")
        }
        open={isOpen}
        onCancel={handleCancel}
        footerButtonArea={
          token ? (
            <MoveToDataButton
              icon={<CopyIcon />}
              intent="action"
              onClick={handleCopy}
            >
              {getLanguageLabel("copy")}
            </MoveToDataButton>
          ) : (
            <MoveToDataButton
              icon={<TickIcon />}
              intent="action"
              onClick={() => {
                createTokenAPI(form.getFieldsValue()).then(({ data }) => {
                  fetchAllTokensAPI().then(({ data }) => {
                    setTokens(data);
                  });
                  setToken(data.accessToken);
                  form.resetFields();
                });
              }}
              key="submit"
            >
              {getLanguageLabel("create")}
            </MoveToDataButton>
          )
        }
      >
        {token != "" ? (
          <div onClick={handleCopy}>
            <Tooltip title={getLanguageLabel("clickToCopyIntoClipboard")}>
              {token}
            </Tooltip>
          </div>
        ) : (
          <>
            <Item name="name" label="Name" required>
              <MoveToDataInput autofocus type="name" />
            </Item>

            <Item name="expiration" label="Expiration" required>
              <MoveToDataDatePicker />
            </Item>
          </>
        )}
      </MoveToDataModal>
    </Form>
  );
};

export default CreateNewTokenModal;
