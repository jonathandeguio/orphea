import { Col, Divider, Form, Row, Select, Switch, Typography } from "antd";
import { KeyIcon } from "assets/icons/movetodataInterfaceIcons";
import {
  LibraryIcon,
  LightBulbIcon,
} from "assets/icons/movetodataMiscellaneousIcons";
import axios from "axios";
import MoveToDataDatePicker from "components/MoveToDataDatePicker";
import { ErrorResponse } from "global";
import React, { Dispatch, SetStateAction, useState } from "react";
import { useDispatch } from "react-redux";
import { Link } from "react-router-dom";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { TickIcon } from "assets/icons/movetodataNavigationIcon";
import MoveToDataModal from "components/MoveToDataModalContainer";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";
import { getAllConfigurationDetails } from "redux/actions/ConfigurationActions";
import { ThunkAppDispatch } from "redux/types/store";

interface Props {
  deploymentId: string;
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
  onLicenseCreated: any;
}
const { Text } = Typography;
const { Item } = Form;
const CreateNewLicenseModal = ({
  deploymentId,
  isOpen,
  setIsOpen,
  onLicenseCreated,
}: Props) => {
  const [form] = Form.useForm();
  const dispatch = useDispatch<ThunkAppDispatch>();
  const initialLicenseDetails = {
    client: "",
    product: "DATA_PLATFORM",
    baseUrl: "",
    expiresOn: "",
    displayBlockedFeatures: false,
    maximumUsers: 10000,
    maximumBuildsPerDay: 10000,
    maximumDatasets: 10000,
    maximumDashboards: 10000,
    maximumCharts: 10000,
    maximumRepositories: 10000,
    // create base modal here
  };

  const [newLicenseDetails, setNewLicenseDetails] = useState({
    ...initialLicenseDetails,
  });

  const [protocol, setProtocol] = useState("https");

  const handleProtocolChange = (value: any) => {
    setProtocol(value);
    setNewLicenseDetails({
      ...newLicenseDetails,
      baseUrl: `${value}://${newLicenseDetails.baseUrl.split("://")[1] || ""}`,
    });
  };

  const handleFqdnChange = (e: any) => {
    const fqdn = e.target.value;
    setNewLicenseDetails({
      ...newLicenseDetails,
      baseUrl: `${protocol}://${fqdn}`,
    });
  };

  const handleOk = async () => {
    console.log(newLicenseDetails);
    if (
      !(
        newLicenseDetails.client &&
        newLicenseDetails.product &&
        newLicenseDetails.baseUrl &&
        // newLicenseDetails.displayBlockedFeatures &&
        newLicenseDetails.maximumUsers &&
        newLicenseDetails.maximumBuildsPerDay &&
        newLicenseDetails.maximumDatasets &&
        newLicenseDetails.maximumDashboards &&
        newLicenseDetails.maximumCharts &&
        newLicenseDetails.maximumRepositories &&
        newLicenseDetails.expiresOn
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
        `/deployments/license/create/${deploymentId}`,
        JSON.stringify({
          ...newLicenseDetails,
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
    onLicenseCreated();
    dispatch(getAllConfigurationDetails());
  };
  const handleSwitchChange = (checked: any) => {
    const newDisplayBlockedFeatures = checked ? true : false;
    console.log(`Switch changed to: ${newDisplayBlockedFeatures}`);
    setNewLicenseDetails({
      ...newLicenseDetails,
      displayBlockedFeatures: newDisplayBlockedFeatures,
    });
  };
  return (
    <>
      <Form>
        <MoveToDataModal
          headingIcon={<KeyIcon />}
          heading={getLanguageLabel("productLicensing")}
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
                Create
              </MoveToDataButton>
            </>
          }
          information={
            <div style={{ width: "300px" }}>
              <div style={{ padding: "20px" }}>
                <div className="text-and-icon-align">
                  <LightBulbIcon />
                  <Text strong>License creation</Text>
                </div>
                <div
                  style={{
                    paddingTop: "10px",
                    paddingLeft: "20px",
                    fontSize: "0.7rem",
                  }}
                >
                  License creation in MoveToData is an exclusive feature reserved
                  for MoveToData Team users. When creating a license, you have the
                  option to choose from three distinct product types: data
                  platform, data hub, and data platform. Additionally, you can
                  tailor your license by selecting one of four size options:
                  ultimate, large, medium, or small, each coming with predefined
                  values. Furthermore, these predefined values are customizable,
                  allowing you to fine-tune your license to suit your specific
                  needs and requirements.
                </div>
              </div>
              <div style={{ padding: "20px" }}>
                <div className="text-and-icon-align">
                  <LibraryIcon />
                  <Text strong>{getLanguageLabel("learn")}</Text>
                </div>
                <div style={{ paddingTop: "10px", paddingLeft: "20px" }}>
                  <Link to="/learn/">License Guidelines</Link>
                  <br />
                  <Link to="/learn/">Best Practices</Link>
                  <br />
                  <Link to="/learn/">Data Transfer Security</Link>
                  <br />
                  <Link to="/learn/">Governance Policy and Guidelines</Link>
                </div>
              </div>
              <div style={{ padding: "20px" }}>
                <div className="text-and-icon-align">
                  <KeyIcon />
                  <Text strong>{getLanguageLabel("access")}</Text>
                </div>
                <div style={{ paddingTop: "10px", paddingLeft: "20px" }}>
                  License creation is only available for MoveToData Team
                </div>
              </div>
            </div>
          }
          footerExtraText="License creation is only available for MoveToData Team"
        >
          <Item name={"client"}>
            <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <label style={{ width:'25%' }}>Client:</label>
              <MoveToDataInput
                placeholder="Enter premise name"
                autofocus
                onChange={(e: any) =>
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    client: e.target.value,
                  })
                }
                value={newLicenseDetails.client}
                required
                style={{ width:'75%' }}
              />
            </div>
          </Item>

          <Item name={"product"}>
            <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <label style={{ width:'25%' }}>Product:</label>
              <Select
                defaultValue="DATA_PLATFORM"
                onChange={(value) =>
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    product: value,
                  })
                }
                value={newLicenseDetails.product}
                style={{ width:'75%' }}
              >
                <Select.Option value="DATA_PLATFORM">Data Platform</Select.Option>
                <Select.Option value="DATA_HUB">Data Hub</Select.Option>
                <Select.Option value="DATA_VIZ">Data Viz</Select.Option>
              </Select>
            </div>
          </Item>

          <Item name={"displayBlockedFeatures"}>
            <div style={{ display: "flex", justifyContent: "space-between", width: "100%" }}>
              <label >Display Blocked Features:</label>
              <Switch
                onChange={(checked: boolean) =>
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    displayBlockedFeatures: checked ? true : false,
                  })
                }
                checked={newLicenseDetails.displayBlockedFeatures}
              />
            </div>
          </Item>

          <Item name="expiresOn" required>
            <div style={{ display: "flex", justifyContent: "space-between", width: "100%" }}>
              <label style={{ width:'34%' }}>Expires On:</label>
              <MoveToDataDatePicker
                onChange={(date: any) => {
                  const timestamp = date ? date.valueOf() : null;
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    expiresOn: timestamp,
                  });
                }}
                value={
                  newLicenseDetails.expiresOn
                    ? new Date(newLicenseDetails.expiresOn)
                    : null
                }
              />
            </div>
          </Item>


          <Item name="baseUrl">
            <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <label style={{ width:'25%' }}>Base Url:</label>
              <div style={{ width:'75%', display:'flex', justifyContent:"space-between" }}>
                <Select value={protocol} onChange={handleProtocolChange} style={{width:'24%',height:'83%'}}>
                  <Select.Option value="http">http</Select.Option>
                  <Select.Option value="https">https</Select.Option>
                </Select>

                <MoveToDataInput
                  placeholder="Enter your server FQDN"
                  onChange={handleFqdnChange}
                  value={newLicenseDetails.baseUrl.split("://")[1] || ""}
                  style={{width:'74%', height:'100%'}}
                />
              </div>
            </div>
          </Item>

          <Divider />
          <Item name={"maximumUsers"}>
          <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <label style={{ width:'25%' }}>Users:</label>
              <MoveToDataInput
                defaultValue={100000}
                placeholder="Enter Count of max Users"
                autoFocus
                onChange={(e: any) =>
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    maximumUsers: e.target.value,
                  })
                }
                value={newLicenseDetails.maximumUsers}
                required
                style={{ width:'75%' }}
              />
            </div>
          </Item>

          <Item name={"maximumBuildsPerDay"}>
            <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <label style={{ width:'25%' }}>Builds per day:</label>
              <MoveToDataInput
                defaultValue={100000}
                placeholder="Enter Count of max builds per day"
                autofocus
                onChange={(e: any) =>
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    maximumBuildsPerDay: e.target.value,
                  })
                }
                value={newLicenseDetails.maximumBuildsPerDay}
                required
                style={{ width:'75%' }}
              />
            </div>
          </Item>

          <Item name={"maximumDatasets"}>
            <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <label style={{ width:'25%' }}>Datasets:</label>
              <MoveToDataInput
                defaultValue={100000}
                placeholder="Enter Count of max datasets"
                autofocus
                onChange={(e: any) =>
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    maximumDatasets: e.target.value,
                  })
                }
                value={newLicenseDetails.maximumDatasets}
                required
                style={{ width:'75%' }}
              />
            </div>
          </Item>

          <Item name={"maximumRepositories"}>
            <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <label style={{ width:'25%' }}>Repositories:</label>
              <MoveToDataInput
                defaultValue={100000}
                placeholder="Enter Count of max Repositories"
                autofocus
                onChange={(e: any) =>
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    maximumRepositories: e.target.value,
                  })
                }
                value={newLicenseDetails.maximumRepositories}
                required
                style={{ width:'75%' }}
              />
            </div>
          </Item>

          <Item name={"maximumCharts"}>
            <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <label style={{ width:'25%' }}>Charts:</label>
              <MoveToDataInput
                defaultValue={100000}
                placeholder="Enter Count of max Charts"
                autofocus
                onChange={(e: any) =>
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    maximumCharts: e.target.value,
                  })
                }
                value={newLicenseDetails.maximumCharts}
                required
                style={{ width:'75%' }}
              />
            </div>
          </Item>

          <Item name={"maximumDashboards"}>
            <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <label style={{ width:'25%' }}>Dashboards:</label>
              <MoveToDataInput
                defaultValue={100000}
                placeholder="Enter Count of max Dashboards"
                autofocus
                onChange={(e: any) =>
                  setNewLicenseDetails({
                    ...newLicenseDetails,
                    maximumDashboards: e.target.value,
                  })
                }
                value={newLicenseDetails.maximumDashboards}
                required
                style={{ width:'75%' }}
              />
            </div>
          </Item>
        </MoveToDataModal>
      </Form>
    </>
);
};

export default CreateNewLicenseModal;
