import { Form, Tooltip, Typography } from "antd";
import { AddUserIcon } from "assets/icons/boslerInterfaceIcons";
import { createTokenAPI, fetchAllTokensAPI } from "apps/settings/apis";
import React, { Dispatch, SetStateAction, useState } from "react";
import { copyToClipboard, getLanguageLabel } from "utils/utilities";
import { CopyIcon } from "assets/icons/boslerEditorIcons";
import { TickIcon } from "assets/icons/boslerNavigationIcon";
import BoslerModal from "components/BoslerModalContainer/BoslerModal";
import BoslerButton from "components/ButtonComponent/BoslerButton";
import BoslerInput from "components/InputComponent/BoslerInput";
import BoslerDatePicker from "components/BoslerDatePicker";

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
      <BoslerModal
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
            <BoslerButton
              icon={<CopyIcon />}
              intent="action"
              onClick={handleCopy}
            >
              {getLanguageLabel("copy")}
            </BoslerButton>
          ) : (
            <BoslerButton
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
            </BoslerButton>
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
              <BoslerInput autofocus type="name" />
            </Item>

            <Item name="expiration" label="Expiration" required>
              <BoslerDatePicker />
            </Item>
          </>
        )}
      </BoslerModal>
    </Form>
  );
};

export default CreateNewTokenModal;
