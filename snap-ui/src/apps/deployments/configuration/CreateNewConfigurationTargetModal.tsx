import { Form, Select } from "antd";
import { KeyIcon } from "assets/icons/movetodataInterfaceIcons";
import { TickIcon } from "assets/icons/movetodataNavigationIcon";
import axios from "axios";
import React, { Dispatch, SetStateAction, useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { openNotification } from "utils/utilities";
import MoveToDataModal from "components/MoveToDataModalContainer";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import { ThunkAppDispatch } from "redux/types/store";
import { updateTargetState } from "../apis";

const { Option } = Select;

interface Props {
  deploymentDetails: any;
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
  onTarget: any;
}

interface ConfigurationTargetDetails {
  frontend: string;
  boson: string;
  parler: string;
  julia: string;
  callisto: string;
  capture: string;
  movetodataDocs: string;
  sparkHistoryServer: string;
}

interface ConfigurationTargetDetailsWithLatest
  extends ConfigurationTargetDetails {
  frontend_latest: string;
  boson_latest: string;
  parler_latest: string;
  julia_latest: string;
  callisto_latest: string;
  capture_latest: string;
  movetodataDocs_latest: string;
  sparkHistoryServer_latest: string;
}

const initialConfigurationTargetDetails: ConfigurationTargetDetails = {
  frontend: "",
  boson: "",
  parler: "",
  julia: "",
  callisto: "",
  capture: "",
  movetodataDocs: "",
  sparkHistoryServer: "",
};

const triggerNameMapping: Record<keyof ConfigurationTargetDetails, string> = {
  frontend: "frontend",
  boson: "boson",
  parler: "parler",
  julia: "julia",
  callisto: "callisto",
  capture: "capture",
  movetodataDocs: "movetodata-docs",
  sparkHistoryServer: "spark-history-server",
};

const CreateNewConfigurationTargetModal = ({
  deploymentDetails,
  isOpen,
  setIsOpen,
  onTarget,
}: Props) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [newConfigurationTargetDetails, setNewConfigurationTargetDetails] =
    useState<ConfigurationTargetDetails>({
      ...initialConfigurationTargetDetails,
    });
  const [existingDetails, setExistingDetails] =
    useState<ConfigurationTargetDetails | null>(null);
  const [dropdownOptions, setDropdownOptions] = useState<
    Partial<Record<keyof ConfigurationTargetDetailsWithLatest, string[]>>
  >({});

  useEffect(() => {
    const fetchCurrentDetails = async () => {
      try {
        const response = await axios.get(`/deployments/${deploymentDetails.id}`);
        setExistingDetails(response.data.configurationComponentsModel);
      } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
          openNotification(
            error.response.data.error,
            "Failed to fetch current configuration details. Please contact platform admin.",
            "error"
          );
        }
      }
    };

    const fetchDropdownOptions = async (
      triggerName: keyof ConfigurationTargetDetails
    ) => {
      try {
        const endpoint = triggerNameMapping[triggerName];
        const response = await axios.get(
          `/artifact/listByTriggerName/${endpoint}/${deploymentDetails.branch}`
        );
        if (Array.isArray(response.data)) {
          let options = response.data.sort((a, b) =>
            b.localeCompare(a, undefined, { numeric: true })
          );

          const highestVersion = options[0];

          setDropdownOptions((prevOptions) => ({
            ...prevOptions,
            [triggerName]: options,
            [`${triggerName}_latest`]: [highestVersion],
          }));

          // Set the latest version as default
          setNewConfigurationTargetDetails((prevDetails) => ({
            ...prevDetails,
            [triggerName]: highestVersion,
          }));
        }
      } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
          openNotification(
            error.response.data.error,
            `Failed to fetch options for ${triggerName}. Please contact platform admin.`,
            "error"
          );
        }
      }
    };

    if (isOpen) {
      fetchCurrentDetails();
      setNewConfigurationTargetDetails({
        ...initialConfigurationTargetDetails,
      });
      (
        Object.keys(initialConfigurationTargetDetails) as Array<
          keyof ConfigurationTargetDetails
        >
      ).forEach((triggerName) => {
        fetchDropdownOptions(triggerName);
      });
    }
  }, [isOpen, deploymentDetails.id]);

  const handleOk = async () => {
    const updatedDetails = Object.keys(newConfigurationTargetDetails).reduce(
      (acc, key) => {
        if (
          newConfigurationTargetDetails[key as keyof ConfigurationTargetDetails]
        ) {
          acc[key as keyof ConfigurationTargetDetails] =
            newConfigurationTargetDetails[
              key as keyof ConfigurationTargetDetails
            ];
        }
        return acc;
      },
      {} as Partial<ConfigurationTargetDetails>
    );

    if (Object.keys(updatedDetails).length === 0) {
      openNotification(
        "Details Incomplete",
        "Please enter at least one detail to update",
        "warning"
      );
      return;
    }

    const finalDetails = { ...existingDetails, ...updatedDetails };

    try {
      await updateTargetState(deploymentDetails.id, finalDetails);
      openNotification(
        "Success",
        "Configuration target updated successfully",
        "success"
      );
      onTarget();
      setIsOpen(false);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        openNotification(
          error.response.data.error,
          "Please contact platform admin.",
          "error"
        );
      }
    }
  };

  const handleSelectChange = (
    triggerName: keyof ConfigurationTargetDetails,
    value: string
  ) => {
    setNewConfigurationTargetDetails({
      ...newConfigurationTargetDetails,
      [triggerName]: value,
    });
  };

  return (
    <Form layout="vertical">
      <MoveToDataModal
        headingIcon={<KeyIcon />}
        heading={"Target State Update"}
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        width={800}
        footerButtonArea={
          <>
            <MoveToDataButton
              icon={<TickIcon />}
              intent="action"
              key="submit"
              onClick={handleOk}
            >
              Update
            </MoveToDataButton>
          </>
        }
      >
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
          {(
            Object.keys(initialConfigurationTargetDetails) as Array<
              keyof ConfigurationTargetDetails
            >
          ).map((triggerName) => (
            <div style={{ display: "flex", alignItems: "center" }} key={triggerName}>
              <div style={{ flex: "0 0 200px" }}>
                {triggerName.charAt(0).toUpperCase() + triggerName.slice(1)}
              </div>
              <Select
                placeholder={`Select ${triggerName}`}
                onChange={(value) => handleSelectChange(triggerName, value)}
                value={newConfigurationTargetDetails[triggerName]}
                defaultValue={dropdownOptions[`${triggerName}_latest`]?.[0]}
                style={{ flex: 1 }}
              >
                {(dropdownOptions[triggerName] ?? []).map((option) => (
                  <Option key={option} value={option}>
                    {dropdownOptions[`${triggerName}_latest`]?.includes(option)
                      ? `${option} (latest)`
                      : option}
                  </Option>
                ))}
              </Select>
            </div>
          ))}
        </div>
      </MoveToDataModal>
    </Form>
  );
};

export default CreateNewConfigurationTargetModal;
