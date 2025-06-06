import React from "react";

import { getLanguageLabel } from "utils/utilities";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

import { reUploadDatasetAPI } from "Apps/Dataset/Table/BoslerTable.api";
import { TableIcon } from "assets/icons/boslerTableIcons";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import { useDispatch } from "react-redux";
import { useParams } from "react-router";
import { CrossIcon } from "../../assets/icons/boslerActionIcons";
import { TickIcon } from "../../assets/icons/boslerNavigationIcon";
import { ThunkAppDispatch } from "../../redux/types/store";

export default ({ isVisible, setIsVisible }: any) => {
  const { id, branch } = useParams();

  const dispatch = useDispatch<ThunkAppDispatch>();

  const handleOk = () => {
    setIsVisible(false);
    reUploadDatasetAPI(id as string, branch as string).then(() => {
      // dispatch(checkTransaction(id, branch, true));
    });
  };

  return (
    <BoslerModal
      open={isVisible}
      onCancel={() => setIsVisible(false)}
      headingIcon={<TableIcon />}
      heading="Re-upload"
      footerButtonArea={
        <>
          <BoslerButton
            icon={<TickIcon />}
            intent="dangerous"
            onClick={handleOk}
          >
            {getLanguageLabel("re-Upload")}
          </BoslerButton>
          <BoslerButton
            icon={<CrossIcon />}
            intent="primary"
            onClick={() => setIsVisible(false)}
          >
            {getLanguageLabel("cancel")}
          </BoslerButton>
        </>
      }
    >
      Are you sure you want to Re-Upload?
    </BoslerModal>
  );
};
