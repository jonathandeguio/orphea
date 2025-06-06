import {
  Avatar,
  Col,
  Collapse,
  Divider,
  message,
  Popover,
  Radio,
  Row,
  Select,
  Switch,
  Table,
  Typography,
  Upload,
} from "antd";
import axios from "axios";
const { Option } = Select;

import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";

// import { setTheme } from "../../app";
import React from "react";
import ReactCountryFlag from "react-country-flag";
import {
  getLanguageLabel,
  getUserLanguage,
  openNotification,
  setTheme,
} from "utils/utilities";
import { ThunkAppDispatch } from "redux/types/store";

import type { RcFile } from "antd/es/upload/interface";

import { TickIcon } from "assets/icons/boslerNavigationIcon";
import { updateUserDataAPI } from "components/CommandPalette/CommandPalette.api";
import { SaveIcon } from "assets/icons/boslerActionIcons";
import { EditIcon } from "assets/icons/boslerEditorIcons";
import { UploadIcon } from "assets/icons/boslerInterfaceIcons";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import BoslerLoader from "components/boslerLoader";
import { updateLanguage } from "redux/actions/languageActions";
import { updateUserDetails } from "redux/actions/userActions";
import BoslerInput from "components/InputComponent/BoslerInput";
import BoslerButton from "components/ButtonComponent/BoslerButton";
import MfaConfiguration, { openNotificationWithIcon } from "./mfaConfiguration";
import { useParams } from "react-router";
import BoslerModal from "components/BoslerModalContainer";

const layout = {
  labelCol: {
    span: 4,
  },
  wrapperCol: {
    span: 16,
  },
};

const { Title, Text } = Typography;
const { Panel } = Collapse;
/* eslint-disable no-template-curly-in-string */

// const validateMessages = {
//   required: "${label} is required!",
//   types: {
//     email: "${label} is not a valid email!",
//     number: "${label} is not a valid number!",
//   },
//   number: {
//     range: "${label} must be between ${min} and ${max}",
//   },
// };
/* eslint-enable no-template-curly-in-string */

const Profile = ({ user, self, showPreferences, loginHistory }: $TSFixMe) => {
  const [newUserData, setNewUserData] = useState({ ...user });
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [uploadLoading, setUploadLoading] = useState(false);

  const [updated, setUpdated] = useState<boolean>();

  const [open, setOpen] = useState(false);

  const hide = () => {
    setOpen(false);
  };
  const [isOnRecoveryCode, setIsOnRecoveryCode] = useState<boolean>(false);

  const { user: loggedinUser } = useSelector(
    (state) => (state as any).userDetails
  );
  const handleOpenChange = (newOpen: boolean) => {
    setOpen(newOpen);
  };
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );
  const [CancelModalOpen, setCancelModalOpen] = useState<boolean>(false);
  const { config } = useSelector((state) => (state as any).platformConfig);
  const handleMfaToggel = async () => {
    if (config.mfaEnforced === false || config.mfaEnforced === null) {
      if (loggedinUser.isMfaEnabled) {
        setCancelModalOpen(true);
      } else {
        setIsOpen(true);
      }
    } else if (platformAdmin) {
      if (loggedinUser.isMfaEnabled) {
        setCancelModalOpen(true);
      } else {
        setIsOpen(true);
      }
    } else {
      openNotificationWithIcon(
        "error",
        getLanguageLabel("MFADisableError"),
        getLanguageLabel("MFADisableErrorMessage") + " ."
      );
    }
  };
  const [loadingMfa, setLoadingMfa] = useState<boolean>(false);
  const urlParams = useParams();
  const handleResetMfa = () => {
    setLoadingMfa(true);
    setIsOpen(false);
    // Disable MFA
    axios
      .post(`/passport/mfa/disable/${user.username}`)
      .then(() => {
        dispatch(updateUserDetails({ ...user, isMfaEnabled: false }));
      })
      .catch((err) => {
        openNotificationWithIcon(
          "error",
          "MFA Disable Error",
          "Failed to disable MFA. Please try again."
        );
        dispatch(updateUserDetails({ ...user, isMfaEnabled: true }));
      })
      .finally(() => {
        setLoadingMfa(false);
        setCancelModalOpen(false);
      });
  };
  // const { language: userLanguage } = useSelector((state: RootState) => state.language);

  // const props = {
  //   name: "file",
  //   action: "https://www.mocky.io/v2/5cc8019d300000980a055e76",
  //   headers: {
  //     authorization: "authorization-text",
  //   },
  //   onChange(info: $TSFixMe) {
  //     if (info.file.status !== "uploading") {
  //     }
  //     if (info.file.status === "done") {
  //       setNewUserData({
  //         ...newUserData,
  //         profileImageUrl: info.file.response.url,
  //       });
  //       // message.success(`${info.file.name} file uploaded successfully`);
  //     } else if (info.file.status === "error") {
  //       message.error(`${info.file.name} file upload failed.`);
  //     }
  //   },
  // };

  const [last10Login, setLast10Login] = useState();

  useEffect(() => {
    getLoginActivity();
  }, []);

  const getLoginActivity = async () => {
    try {
      const data = axios
        .get(`/passport/users/${user?.id}/last10Login`)
        .then((data) => {
          setLast10Login(data.data);
        });
    } catch (error) {
      message.error("Something went wrong");
    }
  };

  const columns = [
    {
      title: "Agent",
      dataIndex: "agent",
      key: "agent",
    },
    {
      title: "Remote Address",
      dataIndex: "remoteAddr",
      key: "remoteAddr",
    },
    {
      title: "Last Login",
      dataIndex: "lastLoginAt",
      key: "lastLoginAt",
      render: (text: number) => new Date(text).toLocaleString(),
    },
    {
      title: "Logged Out",
      dataIndex: "lastLogoutAt",
      key: "lastLogoutAt",
      render: (text: number) =>
        text ? new Date(text).toLocaleString() : getLanguageLabel("noStatus"),
    },
  ];

  const handleUpdate = async (newUserData: any) => {
    updateUserDataAPI(newUserData).then(({ data }) => {
      setNewUserData(data);
      if (self) {
        setTheme(data);
        dispatch(updateLanguage(getUserLanguage(data)));
        dispatch(updateUserDetails(data));

        if (data) {
          setUpdated(true);
        }
      }
    });
  };

  const getBase64 = (img: RcFile, callback: (url: string) => void) => {
    const reader = new FileReader();
    reader.addEventListener("load", () => callback(reader.result as string));
    reader.readAsDataURL(img);
  };

  const beforeUpload = (file: RcFile) => {
    const isJpgOrPng = file.type === "image/jpeg" || file.type === "image/png";
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isJpgOrPng) {
      openNotification(
        "You can only upload JPG/PNG file!",
        "JPG/PNG are the only accepted formats",
        "error"
      );
    } else if (!isLt2M) {
      openNotification(
        "Image must smaller than 2MB!",
        "Please select a file smaller than 2MB",
        "error"
      );
    } else {
      getBase64(file as RcFile, (url) => {
        setUploadLoading(false);

        handleUpdate({ ...newUserData, profileImage: url });
      });
    }

    return false;
  };

  return !user ? (
    <BoslerLoader />
  ) : (
    <div className="settings-center-block">
      <Row gutter={[32, 24]}>
        <Col span={12}>
          <div>
            <form {...layout} name="nest-messages" onSubmit={handleUpdate}>
              <p>
                <Title level={3}>{getLanguageLabel("editProfile")}</Title>
                <Text type="secondary">
                  {getLanguageLabel("editProfileDescription")}
                </Text>
                <Divider />
              </p>
              <div className="BoslerHeader1">
                {getLanguageLabel("givenName")}
              </div>

              <BoslerInput
                placeholder={user.givenName}
                // @ts-expect-error TS(2322): Type 'string[]' is not assignable to type 'string'... Remove this comment to see the full error message
                name={["user", "fname"]}
                onChange={(e) => {
                  setNewUserData({
                    ...newUserData,
                    givenName: e.target.value,
                  });
                }}
                value={newUserData.givenName}
              />
              <div className="BoslerHeader1">
                {getLanguageLabel("familyName")}
              </div>

              <BoslerInput
                placeholder={user.familyName}
                // @ts-expect-error TS(2322): Type 'string[]' is not assignable to type 'string'... Remove this comment to see the full error message
                name={["user", "lname"]}
                onChange={(e) =>
                  setNewUserData({
                    ...newUserData,
                    familyName: e.target.value,
                  })
                }
                value={newUserData.familyName}
              />
              <div className="BoslerHeader1">{getLanguageLabel("email")}</div>
              <BoslerInput
                placeholder={user.email}
                // @ts-expect-error TS(2322): Type 'string[]' is not assignable to type 'string'... Remove this comment to see the full error message
                name={["user", "email"]}
                type="email"
                onChange={(e) =>
                  setNewUserData({ ...newUserData, email: e.target.value })
                }
                value={newUserData.email}
              />

              {/* 
              <div className="BoslerHeader1">{getLanguageLabel("designation")}</div>
                  <BoslerInput
                    // @ts-expect-error TS(2322): Type 'string[]' is not assignable to type 'string'... Remove this comment to see the full error message
                    name={["designation"]}
                    value={desig}
                    onChange={(e) => setDesig(e.target.value)}
                    type="name"
                  />
                 */}

              <div className="BoslerHeader1">
                {getLanguageLabel("location")}
              </div>
              <BoslerInput
                placeholder={user.location}
                // @ts-expect-error TS(2322): Type 'string[]' is not assignable to type 'string'... Remove this comment to see the full error message
                name={["location"]}
                onChange={(e) =>
                  setNewUserData({
                    ...newUserData,
                    location: e.target.value,
                  })
                }
                value={newUserData.location}
              />
              <div>
                {config.mfaEnabled &&
                (loggedinUser.id === urlParams.id || !urlParams.id) ? (
                  <Row style={{ padding: "10px 0" }} gutter={16} align="middle">
                    <Col span={12}>
                      <Text type="secondary">
                        {getLanguageLabel("multiFactorAuthentication")} (MFA)
                      </Text>
                    </Col>
                    <Col>
                      <Switch
                        loading={loadingMfa}
                        checked={user.isMfaEnabled}
                        onChange={handleMfaToggel} // Trigger MFA toggle and QR code generation
                        size={"small"}
                      />
                    </Col>
                  </Row>
                ) : (
                  ""
                )}
                <BoslerModal
                  heading={getLanguageLabel("disableMfa")}
                  closable={true}
                  open={CancelModalOpen}
                  onCancel={() => setCancelModalOpen(false)}
                  footer={
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "end",
                        gap: "20px",
                        padding: "10px",
                      }}
                    >
                      <BoslerButton
                        onClick={() => setCancelModalOpen(!CancelModalOpen)}
                      >
                        {getLanguageLabel("cancel")}
                      </BoslerButton>
                      <BoslerButton onClick={handleResetMfa} intent="dangerous">
                        {getLanguageLabel("yes")}
                      </BoslerButton>
                    </div>
                  }
                >
                  <Typography>
                    {getLanguageLabel("disableMfaMessage")}
                  </Typography>
                </BoslerModal>
                <BoslerModal
                  closable={true}
                  width={"600px"}
                  heading={getLanguageLabel("setUpMFA")}
                  open={isOpen}
                  onCancel={() => {
                    if (isOnRecoveryCode) {
                      dispatch(
                        updateUserDetails({
                          ...loggedinUser,
                          isMfaEnabled: true,
                        })
                      );
                    }
                    setIsOpen(false);
                  }}
                >
                  <MfaConfiguration
                    setIsOnRecoveryCode={setIsOnRecoveryCode}
                    setIsOpen={setIsOpen}
                  />
                </BoslerModal>
              </div>

              {/* <Link to={`mfaConfiguration`}>
                  <div
                    className={
                      lastSegment == "mfaConfiguration"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                    <CodeCellIcon /> {"mfaConfiguration"}
                    </div>
                  </div>
                </Link> */}

              <BoslerButton
                icon={updated ? <TickIcon /> : <SaveIcon />}
                intent={updated ? "success" : "action"}
                onClick={() => handleUpdate(newUserData)}
                style={{
                  marginTop: "10px",
                  marginBottom: "10px",
                }}
                textTransform="none"
              >
                {getLanguageLabel("update")}
              </BoslerButton>

              {showPreferences ? (
                <>
                  <Divider orientation="left">Preferences</Divider>
                  <div className="theme-pref">
                    <div className="BoslerHeader1">
                      {getLanguageLabel("languagePreference")}
                    </div>

                    <Select
                      defaultValue={user.preferences.language}
                      style={{ width: "100%" }}
                      onChange={(selected) => {
                        handleUpdate({
                          ...newUserData,
                          language: selected,
                        });
                      }}
                    >
                      <Option value="auto">
                        {getLanguageLabel("auto")}{" "}
                        <Text type="secondary">(Automatic)</Text>
                        <br />
                        <Text type="secondary">
                          {getLanguageLabel("autoLanguageSubtitle")}
                        </Text>
                      </Option>
                      <Option value="fr">
                        <ReactCountryFlag countryCode="FR" svg />{" "}
                        {getLanguageLabel("french")}{" "}
                        <Text type="secondary">(French)</Text>
                      </Option>
                      <Option value="en">
                        <ReactCountryFlag countryCode="GB" svg />{" "}
                        {getLanguageLabel("english")}{" "}
                        <Text type="secondary">(English)</Text>
                      </Option>
                      <Option value="de">
                        <ReactCountryFlag countryCode="DE" svg />{" "}
                        {getLanguageLabel("german")}{" "}
                        <Text type="secondary">(German)</Text>
                      </Option>
                      <Option value="nl">
                        <ReactCountryFlag countryCode="NL" svg />{" "}
                        {getLanguageLabel("dutch")}{" "}
                        <Text type="secondary">(Dutch)</Text>
                      </Option>
                      <Option value="hi">
                        <ReactCountryFlag countryCode="IN" svg />{" "}
                        {getLanguageLabel("hindi")}{" "}
                        <Text type="secondary">(Hindi)</Text>
                      </Option>
                    </Select>
                  </div>

                  <div className="theme-pref">
                    <Text type="secondary">
                      {getLanguageLabel("themePreference")}
                    </Text>{" "}
                    &nbsp;&nbsp;
                    <br />
                    <Radio.Group
                      name="mode"
                      onChange={(e) => {
                        handleUpdate({
                          ...newUserData,
                          mode: e.target.value,
                        });
                      }}
                      defaultValue={user.preferences.mode}
                      size="small"
                    >
                      <Radio.Button
                        name="mode"
                        value="auto"
                        style={{ background: "var(--background-color)" }}
                      >
                        {getLanguageLabel("auto")}
                      </Radio.Button>
                      <Radio.Button
                        name="mode"
                        value="dark"
                        style={{ background: "var(--background-color)" }}
                      >
                        {getLanguageLabel("dark")}
                      </Radio.Button>
                      <Radio.Button
                        name="mode"
                        value="light"
                        style={{ background: "var(--background-color)" }}
                      >
                        {getLanguageLabel("light")}
                      </Radio.Button>
                    </Radio.Group>
                  </div>
                </>
              ) : (
                ""
              )}

              <br />
            </form>
          </div>
        </Col>
        <Col span={6}>
          <div>
            <div style={{ position: "relative" }}>
              <Avatar src={user.profileImage} size={200}>
                {user.name ? user.name.charAt(0).toUpperCase() : "B"}
              </Avatar>

              <div style={{ position: "absolute", top: "75%" }}>
                <Popover
                  trigger="click"
                  placement="bottom"
                  open={open}
                  onOpenChange={handleOpenChange}
                  overlayInnerStyle={{
                    paddingLeft: 0,
                    paddingRight: 0,
                    paddingTop: "0.5rem",
                    paddingBottom: "0.5rem",
                  }}
                  content={
                    <>
                      <Row className="popoverContent" onClick={hide}>
                        <Upload
                          accept=".jpeg, .jpg, .png"
                          name="avatar"
                          showUploadList={false}
                          beforeUpload={beforeUpload}
                        >
                          <div
                            style={{ padding: "0.5rem" }}
                            className="text-and-icon-center"
                          >
                            <UploadIcon />
                            Upload a photo...
                          </div>
                        </Upload>
                      </Row>

                      <Row
                        className="popoverContent"
                        onClick={() => {
                          handleUpdate({ ...newUserData, profileImage: null });
                          hide();
                        }}
                      >
                        <span
                          style={{ padding: "0.5rem" }}
                          className="text-and-icon-center"
                        >
                          <TrashIcon color="var(--bosler-intent-danger)" />
                          Remove photo
                        </span>
                      </Row>
                    </>
                  }
                >
                  <BoslerButton
                    size="small"
                    icon={<EditIcon />}
                    intent="primary"
                  >
                    {getLanguageLabel("edit")}
                  </BoslerButton>
                </Popover>
              </div>
              <Divider />
            </div>

            {/* <Upload {...props} maxCount={1}>
                <Button
                  icon={<UploadIcon />}
                  
                >
                  {getLanguageLabel("uploadNew")}
                </Button>
              </Upload> */}
          </div>
        </Col>
      </Row>
      <Row>
        <Col>
          {user.ssoAttributes && (
            <Collapse>
              <Panel header={`SSO Attributes`} key="1">
                {Object.keys(user.ssoAttributes).map((key: any) => {
                  return (
                    <Row gutter={16}>
                      <Col>
                        <Text type="secondary">{key}:</Text>
                      </Col>
                      <Col>
                        <Text>
                          {user.ssoAttributes[key]
                            ? user.ssoAttributes[key]
                            : "--"}
                        </Text>
                      </Col>
                    </Row>
                  );
                })}
              </Panel>
            </Collapse>
          )}
        </Col>
      </Row>
      {loginHistory && (
        <Row>
          <Col>
            {last10Login && (last10Login as any)?.length > 0 && (
              <>
                <Title level={3}>{getLanguageLabel("loginActivity")}</Title>
                <Table
                  dataSource={last10Login}
                  columns={columns}
                  pagination={false}
                  // scroll={{ y: "60vh" }}
                />
              </>
            )}
          </Col>
        </Row>
      )}

      <br />
    </div>
  );
};

export default Profile;
