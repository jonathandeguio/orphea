import React, { Dispatch, SetStateAction, useState } from "react";
import { useDispatch } from "react-redux";
import axios from "axios";
import { Switch } from "antd";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { TickIcon } from "assets/icons/movetodataNavigationIcon";
import { getAllDeploymentDetails } from "redux/actions/DeploymentActions";
import { ThunkAppDispatch } from "redux/types/store";
import { AddIcon } from "assets/icons/movetodataActionIcons";
import { ErrorResponse } from "global";
import MoveToDataModal from "components/MoveToDataModalContainer";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";
import MoveToDataTimePicker from "components/MoveToDataTimePicker";

interface Props {
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
}

const CreateNewDeploymentModal = ({ isOpen, setIsOpen }: Props) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const initialDeploymentDetails = {
    name: "",
    location: "",
    address: "",
    contactDetails: "",
    email: "",
    deploymentMethod: "MANUAL",
    timeWindowStart: 0.0,
    timeWindowEnd: 0.0,
    branch: ""
  };

  const [newDeploymentDetails, setNewDeploymentDetails] = useState({
    ...initialDeploymentDetails,
  });

  const handleSwitchChange = (checked: boolean) => {
    const newDeploymentMethod = checked ? "AUTOMATIC" : "MANUAL";
    setNewDeploymentDetails((prevDetails) => ({
      ...prevDetails,
      deploymentMethod: newDeploymentMethod,
    }));
  };

  const handleOk = async () => {
    const {
      name,
      location,
      address,
      contactDetails,
      email,
      deploymentMethod,
      timeWindowStart,
      timeWindowEnd,
      branch
    } = newDeploymentDetails;

    if (
      !(
        name &&
        location &&
        address &&
        contactDetails &&
        email &&
        deploymentMethod &&
        timeWindowStart &&
        timeWindowEnd &&
        branch
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
      await axios.post(`/deployments/create`, JSON.stringify(newDeploymentDetails));
      dispatch(getAllDeploymentDetails());
      setIsOpen(false);
    } catch (error) {
      if (axios.isAxiosError(error) && isDefined(error.response)) {
        const errorMessage = (error.response.data as ErrorResponse).error;
        openNotification(errorMessage, "Please contact platform admin.", "error");
      }
    }
  };
  return (
    <MoveToDataModal
      headingIcon={<AddIcon />}
      heading={"New Deployment"}
      open={isOpen}
      onCancel={() => setIsOpen(false)}
      onOk={handleOk}
      footerButtonArea={
        <MoveToDataButton icon={<TickIcon />} intent="action" key="submit" onClick={handleOk}>
          {getLanguageLabel("create")}
        </MoveToDataButton>
      }
      width={600}
    >
      <div style={{ maxHeight: "60vh", overflow: "scroll", padding: "0 10px" }}>
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
          <div style={{ display: "flex", alignItems: "center" }}>
            <div style={{ flex: "0 0 150px" }}>Name</div>
            <MoveToDataInput
              autoFocus
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setNewDeploymentDetails((prevDetails) => ({
                  ...prevDetails,
                  name: e.target.value,
                }))
              }
              value={newDeploymentDetails.name}
              name="name"
              required
            />
          </div>
          <div style={{ display: "flex", alignItems: "center" }}>
            <div style={{ flex: "0 0 150px" }}>Branch</div>
            <MoveToDataInput
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setNewDeploymentDetails((prevDetails) => ({
                  ...prevDetails,
                  branch: e.target.value,
                }))
              }
              value={newDeploymentDetails.branch}
              name="branch"
              required
            />
          </div>

          <div style={{ display: "flex", alignItems: "center" }}>
            <div style={{ flex: "0 0 150px" }}>Location</div>
            <MoveToDataInput
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setNewDeploymentDetails((prevDetails) => ({
                  ...prevDetails,
                  location: e.target.value,
                }))
              }
              value={newDeploymentDetails.location}
              name="location"
              required
            />
          </div>
          <div style={{ display: "flex", alignItems: "center" }}>
            <div style={{ flex: "0 0 150px" }}>Address</div>
            <MoveToDataInput
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setNewDeploymentDetails((prevDetails) => ({
                  ...prevDetails,
                  address: e.target.value,
                }))
              }
              value={newDeploymentDetails.address}
              name="address"
              required
            />
          </div>
          <div style={{ display: "flex", alignItems: "center" }}>
            <div style={{ flex: "0 0 150px" }}>Contact Details</div>
            <MoveToDataInput
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setNewDeploymentDetails((prevDetails) => ({
                  ...prevDetails,
                  contactDetails: e.target.value,
                }))
              }
              value={newDeploymentDetails.contactDetails}
              name="contactDetails"
              required
            />
          </div>
          <div style={{ display: "flex", alignItems: "center" }}>
            <div style={{ flex: "0 0 150px" }}>Email</div>
            <MoveToDataInput
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setNewDeploymentDetails((prevDetails) => ({
                  ...prevDetails,
                  email: e.target.value,
                }))
              }
              value={newDeploymentDetails.email}
              name="email"
              required
            />
          </div>
          <div style={{ display: "flex", alignItems: "center" }}>
            <div style={{ flex: "0 0 150px" }}>Deployment Method</div>
            <Switch
              checked={newDeploymentDetails.deploymentMethod === "AUTOMATIC"}
              onChange={handleSwitchChange}
              checkedChildren="AUTOMATIC"
              unCheckedChildren="MANUAL"
            />
          </div>
          <div style={{ display: "flex", alignItems: "center" }}>

  <div style={{ flex: "0 0 150px" }}>Start Time</div>
  <MoveToDataTimePicker
    onChange={(value: number | undefined) => {
      setNewDeploymentDetails((prevDetails: any) => ({
        ...prevDetails,
        timeWindowStart: value,
      }));
    }}
    value={newDeploymentDetails.timeWindowStart}
  />
</div>
<div style={{ display: "flex", alignItems: "center" }}>
  <div style={{ flex: "0 0 150px" }}>End Time</div>
  <MoveToDataTimePicker
    onChange={(value: number | undefined) => {
      setNewDeploymentDetails((prevDetails: any) => ({
        ...prevDetails,
        timeWindowEnd: value,
      }));
    }}
    value={newDeploymentDetails.timeWindowEnd}
  />
</div>

        </div>
      </div>
    </MoveToDataModal>
  );
};

export default CreateNewDeploymentModal;
