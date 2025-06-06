import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React from "react";
import { useDispatch } from "react-redux";
import { getLanguageLabel } from "utils/utilities";
import { TrashIcon } from "../../assets/icons/boslerMiscellaneousIcons";
import { ThunkAppDispatch } from "../../redux/types/store";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

const DeleteModal = ({
  deleteServiceDetails,
  setDeleteServiceDetails,
  handleDelete,
}: any) => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  return (
    <BoslerModal
      open={deleteServiceDetails.modalView}
      onCancel={() =>
        setDeleteServiceDetails({ ...deleteServiceDetails, modalView: false })
      }
      footerButtonArea={
        <BoslerButton
          intent="dangerous"
          disabled={deleteServiceDetails.disabled}
          onClick={() => handleDelete(deleteServiceDetails.id)}
          icon={<TrashIcon />}
        >
          {getLanguageLabel("delete")}
        </BoslerButton>
      }
      headingIcon={<TrashIcon color={"var(--DANGEROUS_COLOR)"} />}
      heading={"Delete"}
    >
      Please Type{" "}
      <b style={{ fontWeight: "bold" }}>{deleteServiceDetails.name}</b> to
      confirm.
      <BoslerInput
        bordered
        placeholder={getLanguageLabel("linkName")}
        onChange={(e) => {
          if (e.target.value === deleteServiceDetails.name) {
            setDeleteServiceDetails({
              ...deleteServiceDetails,
              disabled: false,
            });
          } else if (!deleteServiceDetails.disabled) {
            setDeleteServiceDetails({
              ...deleteServiceDetails,
              disabled: true,
            });
          }
        }}
      />
    </BoslerModal>
  );
};

export default DeleteModal;
