import { Form, Tooltip, Typography } from "antd";
import { AddUserIcon } from "assets/icons/orpheaInterfaceIcons";
import { createTokenAPI, fetchAllTokensAPI } from "apps/settings/apis";
import React, { Dispatch, SetStateAction, useState } from "react";
import { copyToClipboard, getLanguageLabel } from "utils/utilities";
import { CopyIcon } from "assets/icons/orpheaEditorIcons";
import { TickIcon } from "assets/icons/orpheaNavigationIcon";
import OrpheaModal from "components/OrpheaModalContainer/OrpheaModal";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
import OrpheaInput from "components/InputComponent/OrpheaInput";
import OrpheaDatePicker from "components/OrpheaDatePicker";

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
      <OrpheaModal
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
            <OrpheaButton
              icon={<CopyIcon />}
              intent="action"
              onClick={handleCopy}
            >
              {getLanguageLabel("copy")}
            </OrpheaButton>
          ) : (
            <OrpheaButton
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
            </OrpheaButton>
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
              <OrpheaInput autofocus type="name" />
            </Item>

            <Item name="expiration" label="Expiration" required>
              <OrpheaDatePicker />
            </Item>
          </>
        )}
      </OrpheaModal>
    </Form>
  );
};

export default CreateNewTokenModal;
