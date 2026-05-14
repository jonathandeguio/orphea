import axios from "axios";
import React, { Dispatch, SetStateAction, useState } from "react";
import { useDispatch } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { TickIcon } from "assets/icons/orpheaNavigationIcon";
import { getAllConfigurationDetails } from "redux/actions/ConfigurationActions";
import { ThunkAppDispatch } from "redux/types/store";

import { AddIcon } from "assets/icons/orpheaActionIcons";
import { ErrorResponse } from "global";
import OrpheaModal from "components/OrpheaModalContainer";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
import OrpheaInput from "components/InputComponent/OrpheaInput";

interface Props {
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
}

const CreateNewConfigurationModal = ({ isOpen, setIsOpen }: Props) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const initialConfigurationDetails = {
    license:""
    // create base modal here
  };

  const [newConfigurationDetails, setNewConfigurationDetails] = useState({
    ...initialConfigurationDetails,
  });

  const handleOk = async () => {
    if (
      !(
        newConfigurationDetails.license
      )
    ) {
      openNotification(
        "Details Incomplete",
        "Please enter the complete details",
        "warning"
      );
      return;
    }
    try {
      await axios.post(
        `/configuration/create`,
        JSON.stringify({
          ...newConfigurationDetails,
        })
      );
      //
    } catch (error) {
      if (axios.isAxiosError(error) && isDefined(error.response)) {
        const errorMessage = (error?.response?.data as ErrorResponse).error;
        openNotification(
          errorMessage,
          "Please contact platform admin.",
          "error"
        );
      }
    }
    setIsOpen(false);
    dispatch(getAllConfigurationDetails());
  };

  return (
    <>
      <OrpheaModal
        headingIcon={<AddIcon />}
        heading={"New Configuration"}
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        onOk={handleOk}
        footerButtonArea={
          <OrpheaButton
            icon={<TickIcon />}
            intent="action"
            key="submit"
            onClick={handleOk}
          >
            {getLanguageLabel("create")}
          </OrpheaButton>
        }
        width={600}
      >
        <div
          style={{
            height: "60vh",
            overflow: "scoll",
          }}
        >
          <div className="OrpheaHeader1">License</div>
          <OrpheaInput
            autofocus
            onChange={(e: any) =>
              setNewConfigurationDetails({
                ...newConfigurationDetails,
                license: e.target.value,
              })
            }
            value={newConfigurationDetails.license}
            name="name"
            required
          />
          
        </div>
      </OrpheaModal>
    </>
  );
};

export default CreateNewConfigurationModal;
