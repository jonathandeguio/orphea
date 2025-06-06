import DatasetUpload from "Apps/Dataset/DatasetUpload";
import { UploadIcon } from "assets/icons/boslerInterfaceIcons";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React from "react";
import { getLanguageLabel } from "utils/utilities";

interface TProps {
  id: string;
  branch: string;
  isVisible: boolean;
  setIsVisible: any;
}
const DatasetUploadModal = ({
  id,
  branch,
  isVisible,
  setIsVisible,
}: TProps) => {
  return (
    <BoslerModal
      headingIcon={<UploadIcon />}
      heading={getLanguageLabel("upload") + getLanguageLabel("dataset")}
      open={isVisible}
      width={750}
      onCancel={() => setIsVisible(false)}
      //   footerButtonArea={BUTTON_AREA}
      //   footerExtraText={FOOTER_TEXT}
    >
      <DatasetUpload id={id} branch={branch} />
    </BoslerModal>
  );
};

export default DatasetUploadModal;
