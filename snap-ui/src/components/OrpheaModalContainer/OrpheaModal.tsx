import { Modal, ModalProps } from "antd";
import React from "react";
import OrpheaModalContainer from "./OrpheaModalContainer";
interface TOrpheaModalProps extends ModalProps {
  heading?: any;
  headingIcon?: any;
  extraActionHeading?: any;
  information?: any;
  footerButtonArea?: any;
  footerExtraText?: string;
  children?: any;
  backgroundColor?: "muted" | "default";
  dividers?: boolean;
}

const OrpheaModal = ({
  heading,
  headingIcon,
  extraActionHeading,
  information,
  footerButtonArea,
  footerExtraText,
  backgroundColor = "default",
  dividers = true,
  children,
  ...ModalProps
}: TOrpheaModalProps) => {
  return (
    <Modal
      destroyOnClose
      footer={null}
      styles={{
        mask: {
          backgroundColor: "rgba(248, 250, 251, 0.7)",
        },
      }}
      style={{
        width: "fit-content",
      }}
      closable={false}
      width={"fit-content"}
      className="orphea-modal"
      {...ModalProps}
    >
      <OrpheaModalContainer
        heading={heading}
        headingIcon={headingIcon}
        extraActionHeading={extraActionHeading}
        information={information}
        footerExtraText={footerExtraText}
        footerButtonArea={footerButtonArea}
        backgroundColor={backgroundColor}
        dividers={dividers}
      >
        {children}
      </OrpheaModalContainer>
    </Modal>
  );
};

export default OrpheaModal;
