import TextArea from "antd/es/input/TextArea";
import { CopyIcon } from "assets/icons/boslerEditorIcons";
import { KeyIcon } from "assets/icons/boslerInterfaceIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React from "react";
import { copyToClipboard, getLanguageLabel } from "utils/utilities";

interface IProps {
  isOpen: boolean;
  setIsOpen: (isOpen: boolean) => void;
  licenseKey: string;
}

export const CopyLicenseKeyModal = ({
  isOpen,
  setIsOpen,
  licenseKey,
}: IProps) => {
  return (
    <>
      <BoslerModal
        headingIcon={<KeyIcon />}
        heading={getLanguageLabel("productLicensing")}
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        width={800}
        footerButtonArea={
          <>
            <BoslerButton
              intent="action"
              icon={<CopyIcon />}
              onClick={() => {
                copyToClipboard(licenseKey);
              }}
            >
              Copy
            </BoslerButton>
          </>
        }
      >
        <TextArea value={licenseKey} disabled rows={5} />
      </BoslerModal>
    </>
  );
};
