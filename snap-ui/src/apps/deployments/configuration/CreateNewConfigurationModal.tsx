import axios from "axios";
import React, { Dispatch, SetStateAction, useState } from "react";
import { useDispatch } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { TickIcon } from "assets/icons/movetodataNavigationIcon";
import { getAllConfigurationDetails } from "redux/actions/ConfigurationActions";
import { ThunkAppDispatch } from "redux/types/store";

import { AddIcon } from "assets/icons/movetodataActionIcons";
import { ErrorResponse } from "global";
import MoveToDataModal from "components/MoveToDataModalContainer";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";

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
      <MoveToDataModal
        headingIcon={<AddIcon />}
        heading={"New Configuration"}
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        onOk={handleOk}
        footerButtonArea={
          <MoveToDataButton
            icon={<TickIcon />}
            intent="action"
            key="submit"
            onClick={handleOk}
          >
            {getLanguageLabel("create")}
          </MoveToDataButton>
        }
        width={600}
      >
        <div
          style={{
            height: "60vh",
            overflow: "scoll",
          }}
        >
          <div className="MoveToDataHeader1">License</div>
          <MoveToDataInput
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
      </MoveToDataModal>
    </>
  );
};

export default CreateNewConfigurationModal;
