import { NotificationBell } from "Apps/Notifications/NotificationBell.view";
import { Resource } from "Apps/explorer/explorer";
import { getRecentlyViewed } from "Apps/explorer/explorer.api";
import { useNavigateHelper } from "Apps/explorer/explorer.hooks";
import { ResourceTypeEnum } from "Apps/explorer/explorer.utils";
import {
  Avatar,
  Col,
  Dropdown,
  MenuProps,
  Row,
  Space,
  Tooltip,
  Typography,
} from "antd";
import {
  DuplicateIcon,
  LogoutIcon,
  NotificationIcon,
  SearchFiledIcon,
  SettingsIcon
} from "assets/icons/boslerActionIcons";
import { CopyIcon } from "assets/icons/boslerEditorIcons";
import { DocumentationIcon } from "assets/icons/boslerFileIcons";
import { BoslerIcon, HelpIcon } from "assets/icons/boslerMiscellaneousIcons";
import axios from "axios";
import { getIsConnectAdmin } from "common/common.api";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerCommandPalette from "components/CommandPalette/CommandPalette.view";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import BoslerLoader from "components/boslerLoader";
import React, { useEffect, useState } from "react";
import { useHotkeys } from "react-hotkeys-hook";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { refreshTokenStatus } from "redux/actions/tokenActions";
import { logout, setIsConnectAdmin } from "redux/actions/userActions";
import { ThunkAppDispatch } from "redux/types/store";
import {
  copyToClipboard,
  getLanguageLabel,
  getUserDocsLanguage,
  isDefined,
  timeConverter,
} from "utils/utilities";
import HeaderSearch from "./HeaderSearch";

const { Text } = Typography;

const MainHeader = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const isEmbedded = searchParams.get("embedded");
  const dispatch = useDispatch<ThunkAppDispatch>();
  const navigate = useNavigate();
  const navigateResource = useNavigateHelper();
  const [isDebugInfoOpen, setIsDebugInfoOpen] = useState(false);
  const [debugInfo, setDebugInfo] = useState<any>(null);
  const [debugInfoText, setDebugInfoText] = useState<any>(null);

  const [isHeaderSearchModalOpen, setIsHeaderSearchModalOpen] = useState(false);

  const [charts, setCharts] = useState<Resource[]>([]);
  const [dashboards, setDashboards] = useState<Resource[]>([]);
  const [datasets, setDatasets] = useState<Resource[]>([]);
  const [transforms, setTransforms] = useState<Resource[]>([]);

  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  interface DebugInfo {
    lastUpdated?: string; // The "?" makes this property optional
    versions: Record<string, string>;
    debugInfo: string;
    userId: string;
    location: string;
  }

  const copyDebugInfo = async () => {
    const { data } = await axios.get(`/platform/config`);
    const debugInfo: DebugInfo = {
      versions: {}, // Provide a default empty object for versions
      debugInfo: "", // Provide default values for debugInfo, userId, and location
      userId: "",
      location: "",
    };

    let debugInfoText = "";

    if (data.lastUpdatedOn) {
      debugInfo.lastUpdated = timeConverter(data.lastUpdatedOn * 1000);
      debugInfoText +=
        `${getLanguageLabel("lastUpdated")}: ${timeConverter(
          data.lastUpdatedOn * 1000
        )}` + "\n";
      debugInfoText += "\n";
    }

    for (const property in data.versions) {
      debugInfoText += `${property}: ${data.versions[property]}` + "\n";
    }
    debugInfoText += "\n";

    debugInfoText += `debug Info: ${new Date().toUTCString()}` + "\n";
    debugInfoText += "\n";
    debugInfoText += `user Id: ${user.id}` + "\n";
    debugInfoText += `location: ${window.location.href}`;

    debugInfo.versions = data.versions;
    debugInfo.debugInfo = new Date().toUTCString();
    debugInfo.userId = user.id;
    debugInfo.location = window.location.href;

    copyToClipboard(debugInfoText);

    // Optionally, you can set the JSON string in a variable
    setDebugInfo(debugInfo);

    setDebugInfoText(debugInfoText);

    setIsDebugInfoOpen(true);
  };

  useEffect(() => {
    getIsConnectAdmin().then(({ data }) => {
      dispatch(setIsConnectAdmin(data));
    });
    getRecentlyViewed().then(({ data }) => {
      const recentCharts = data.filter(
        (ele: any) => ele.type == ResourceTypeEnum.CHART
      );
      setCharts(recentCharts);

      const recentDashboards = data.filter(
        (ele: any) => ele.type == ResourceTypeEnum.DASHBOARD
      );
      setDashboards(recentDashboards);

      const recentDatasets = data.filter(
        (ele: any) => ele.type == ResourceTypeEnum.DATASET
      );

      setDatasets(recentDatasets);

      const recentTransforms = data.filter(
        (ele: any) => ele.type == ResourceTypeEnum.REPOSITORY
      );
      setTransforms(recentTransforms);
    });
  }, []);

  const items: MenuProps["items"] = [
    {
      key: "1",
      label: (
        <a
          rel="noopener noreferrer"
          onClick={() => navigate("/portal/settings/profile")}
        >
          {user.givenName} {user.familyName}
        </a>
      ),
      icon: (
        <Avatar
          src={
            user.profileImage && user.profileImage != ""
              ? user.profileImage
              : null
          }
        >
          {user.name ? user.name.charAt(0).toUpperCase() : "B"}
        </Avatar>
      ),
      disabled: false,
    },
    {
      key: "2",
      label: (
        <Link to={`/portal/settings/notifications`}>
          {getLanguageLabel("notifications")}
        </Link>
      ),
      icon: <NotificationIcon size={14} />,
    },
    {
      key: "3",
      label: (
        <a
          rel="noopener noreferrer"
          onClick={() => navigate("/portal/settings/profile")}
        >
          {getLanguageLabel("settings")}
        </a>
      ),
      icon: <SettingsIcon size={14} />,
    },
    {
      key: "4",
      label: (
        <a
          rel="noopener noreferrer"
          onClick={() => {
            dispatch(logout());
            dispatch(refreshTokenStatus());
            navigate("/Auth/logout");
          }}
        >
          {getLanguageLabel("logout")}
        </a>
      ),
      icon: <LogoutIcon size={14} />,
    },
  ];

  useHotkeys("/", (event: any) => {
    event.preventDefault();
    setIsHeaderSearchModalOpen(true);
  });

  if (isEmbedded) return null;

  return (
    <>
      <BoslerModal
        footer={null}
        open={isHeaderSearchModalOpen}
        onCancel={() => setIsHeaderSearchModalOpen(false)}
        width={600}
      >
        <HeaderSearch setIsHeaderSearchModalOpen={setIsHeaderSearchModalOpen} />
      </BoslerModal>
      <div>
        <Row
          justify={"space-between"}
          align="middle"
          className="main-app-header"
        >
          <Col>
            <Row align={"middle"} gutter={16}>
              <Col>
                <div>
                  <Link
                    to="/"
                    style={{ display: "flex", alignItems: "center" }}
                  >
                    {loading ? (
                      <BoslerLoader />
                    ) : isDefined(config) && isDefined(config.logo) ? (
                      <img
                        style={{
                          maxWidth: "100%",
                          maxHeight: "100%",
                          height: "3.5vh",
                        }}
                        src={config.logo}
                      />
                    ) : (
                      <BoslerIcon size={32} />
                    )}
                  </Link>
                </div>
              </Col>
              <Col>
                <div className="company-name"></div>{" "}
                {/* Company Name is removed for now -- Rakesh */}
              </Col>
              {/* <Col>
                <Menu mode="horizontal" className="boslerHeaderMenu">
                  <Menu.Item
                    key="projects"
                    onClick={() => navigate(getApplicationLink("projects"))}
                    className="boslerHeaderMenuItem"
                  >
                    {getLanguageLabel("projects")}
                  </Menu.Item>
                  <Menu.SubMenu
                    key="chart"
                    title={
                      <Row align={"middle"} className="boslerHeaderMenuItem">
                        {getLanguageLabel("chart")}
                        &nbsp;
                        <SingleChevronDownIcon />
                      </Row>
                    }
                  >
                    {charts?.map((chart) => {
                      return (
                        <Menu.Item
                          key={chart.id}
                          className="boslerHeaderMenuItem"
                          onClick={() => navigateResource(chart.id)}
                        >
                          <Row align={"middle"}>
                            {getNodeLogo(chart.type, chart.subType)}
                            &nbsp;
                            {chart.name}
                          </Row>
                        </Menu.Item>
                      );
                    })}
                  </Menu.SubMenu>

                  <Menu.SubMenu
                    key="dashboard"
                    title={
                      <Row align={"middle"} className="boslerHeaderMenuItem">
                        {getLanguageLabel("dashboard")}
                        &nbsp;
                        <SingleChevronDownIcon />
                      </Row>
                    }
                  >
                    {dashboards?.map((dashboard) => {
                      return (
                        <Menu.Item
                          key={dashboard.id}
                          className="boslerHeaderMenuItem"
                          onClick={() => navigateResource(dashboard.id)}
                        >
                          <Row align={"middle"}>
                            {getNodeLogo(dashboard.type, dashboard.subType)}
                            &nbsp;
                            {dashboard.name}
                          </Row>
                        </Menu.Item>
                      );
                    })}
                  </Menu.SubMenu>
                  <Menu.SubMenu
                    key="transformation"
                    title={
                      <Row align={"middle"} className="boslerHeaderMenuItem">
                        {getLanguageLabel("transformation")}
                        &nbsp;
                        <SingleChevronDownIcon />
                      </Row>
                    }
                  >
                    {transforms?.map((transform) => {
                      return (
                        <Menu.Item
                          key={transform.id}
                          className="boslerHeaderMenuItem"
                          onClick={() => navigateResource(transform.id)}
                        >
                          <Row align={"middle"}>
                            {getNodeLogo(transform.type, transform.subType)}
                            &nbsp;
                            {transform.name}
                          </Row>
                        </Menu.Item>
                      );
                    })}
                  </Menu.SubMenu>
                  <Menu.SubMenu
                    key="dataset"
                    title={
                      <Row align={"middle"} className="boslerHeaderMenuItem">
                        {getLanguageLabel("dataset")}
                        &nbsp;
                        <SingleChevronDownIcon />
                      </Row>
                    }
                  >
                    {datasets?.map((dataset) => {
                      return (
                        <Menu.Item
                          key={dataset.id}
                          className="boslerHeaderMenuItem"
                          onClick={() => navigateResource(dataset.id)}
                        >
                          <Row align={"middle"}>
                            {getNodeLogo(dataset.type, dataset.subType)}
                            &nbsp;
                            {dataset.name}
                          </Row>
                        </Menu.Item>
                      );
                    })}
                  </Menu.SubMenu>

                  <Menu.Item
                    key="connect"
                    className="boslerHeaderMenuItem"
                    onClick={() => navigate(getApplicationLink("connect"))}
                  >
                    {getLanguageLabel("connect")}
                  </Menu.Item>
                  <Menu.Item
                    key="schedules"
                    onClick={() => navigate(getApplicationLink("schedules"))}
                    className="boslerHeaderMenuItem"
                  >
                    {getLanguageLabel("schedules")}
                  </Menu.Item>
                  <Menu.Item
                    key="builds"
                    onClick={() => navigate(getApplicationLink("builds"))}
                    className="boslerHeaderMenuItem"
                  >
                    {getLanguageLabel("builds")}
                  </Menu.Item>
                  <Menu.Item
                    key="settings"
                    onClick={() => navigate(getApplicationLink("settings"))}
                    className="boslerHeaderMenuItem"
                  >
                    {getLanguageLabel("settings")}
                  </Menu.Item>
                </Menu>
              </Col> */}
            </Row>
          </Col>

          <Col>
            <Row gutter={16} align="middle" justify={"end"}>
              <div
                onClick={() => {
                  setIsHeaderSearchModalOpen(true);
                }}
              >
                <Tooltip
                  placement="bottom"
                  title={getLanguageLabel("searchMsg")}
                >
                  <SearchFiledIcon />
                </Tooltip>
              </div>
              <Tooltip
                placement="bottom"
                title={getLanguageLabel("information")}
              >
                <Col onClick={() => copyDebugInfo()}>
                  <HelpIcon size={18} />
                </Col>
              </Tooltip>
              <Col>
                <BoslerCommandPalette />
              </Col>
              <Col>
                <Tooltip
                  placement="bottom"
                  title={getLanguageLabel("documentation")}
                  className="Text-and-icon-center"
                >
                  <a
                    href={`/learn/${getUserDocsLanguage()}`}
                    target="_blank"
                    rel="noreferrer"
                  >
                    {/* <a href={`/learn/`} target="_blank"> */}
                    <DocumentationIcon />
                  </a>
                </Tooltip>
              </Col>
              <Col>
                <Tooltip
                  placement="bottom"
                  title={getLanguageLabel("notification")}
                  className="Text-and-icon-center"
                >
                  <NotificationBell />
                </Tooltip>
              </Col>
              <Col>
                <Space wrap>
                  <Dropdown menu={{ items }}>
                    <a onClick={(e) => e.preventDefault()}>
                      <Space>
                        <Avatar
                          className="interactive cursor-ptr"
                          src={
                            user.profileImage && user.profileImage != ""
                              ? user.profileImage
                              : null
                          }
                        >
                          {user.name ? user.name.charAt(0).toUpperCase() : "B"}
                        </Avatar>
                      </Space>
                    </a>
                  </Dropdown>
                </Space>
              </Col>
            </Row>
          </Col>
        </Row>
      </div>
      <BoslerModal
        headingIcon={<DuplicateIcon />}
        heading={
          <Row justify={"space-between"} align="middle">
            <Col>{getLanguageLabel("debug")}</Col>
          </Row>
        }
        footerExtraText={getLanguageLabel("information")}
        open={isDebugInfoOpen}
        onCancel={() => setIsDebugInfoOpen(false)}
        width={600}
        extraActionHeading={
          <BoslerButton
            icon={<CopyIcon />}
            onClick={() => debugInfoText && copyToClipboard(debugInfoText)}
            minimal
            icononly
          ></BoslerButton>
        }
      >
        {debugInfo && debugInfo.lastUpdated && (
          <Row
            gutter={[16, 16]}
            justify={"space-between"}
            style={{
              margin: "0.84rem",
              // borderBottom: "1px solid var(--movetodata-border-color-default)",
            }}
          >
            <Col>
              <Text type="secondary">{getLanguageLabel("lastUpdated")}</Text>
            </Col>
            <Col>
              <Text strong>{debugInfo.lastUpdated}</Text>
            </Col>
          </Row>
        )}

        {debugInfo && (
          <>
            <div
              style={{
                background: "var(--movetodata-bkg-color-muted)",
                borderRadius: "2px",
                boxShadow:
                  "rgba(11, 14, 22, 0.02) 0px 0px 0px 1px, rgba(11, 14, 22, 0.04) 0px 4px 8px, rgba(11, 14, 22, 0.04) 0px 18px 46px 6px",

                margin: "0.84rem",
                border: "1px solid var(--movetodata-border-color-default)",
              }}
            >
              <Row
                gutter={[16, 16]}
                justify={"space-between"}
                style={{
                  margin: "0.84rem",
                  borderBottom: "1px solid var(--movetodata-border-color-default)",
                }}
              >
                <Col>
                  <Text type="secondary" strong>
                    Versions{" "}
                    {debugInfo.versions.platform && (
                      <>({debugInfo.versions.platform})</>
                    )}
                  </Text>
                </Col>
                <Col>
                  <BoslerButton
                    icon={<CopyIcon />}
                    onClick={() =>
                      debugInfo &&
                      copyToClipboard(
                        JSON.stringify(debugInfo.versions, null, 2)
                      )
                    }
                    icononly
                    minimal
                    trimicononlypadding
                  />
                </Col>
              </Row>
              {Object.keys(debugInfo.versions)
                .filter((key) => key !== "platform")
                .filter((key) => key !== "connect")
                .map((key) => (
                  <Row
                    gutter={[16, 16]}
                    justify={"space-between"}
                    style={{
                      marginRight: "1.84rem",
                      marginLeft: "1.84rem",
                    }}
                  >
                    <Col span={8} key={key}>
                      {key}
                    </Col>
                    <Col>
                      <Text strong>
                        {debugInfo.versions[key] === null
                          ? "-.-.-"
                          : debugInfo.versions[key]}
                      </Text>
                    </Col>
                  </Row>
                ))}
              <br />
            </div>
            <br />
            <Row
              gutter={[16, 16]}
              justify={"space-between"}
              style={{
                margin: "0.84rem",
                borderBottom: "1px solid var(--movetodata-border-color-default)",
              }}
            >
              <Col>
                <Text type="secondary">
                  {getLanguageLabel("userName")} {getLanguageLabel("id")}
                </Text>
              </Col>
              <Col>
                <span className="text-and-icon-center">
                  <Text strong>{debugInfo.userId}</Text>
                  <BoslerButton
                    icon={<CopyIcon />}
                    onClick={() =>
                      debugInfo && copyToClipboard(debugInfo.userId)
                    }
                    icononly
                    minimal
                    trimicononlypadding
                  />
                </span>
              </Col>
            </Row>

            <Row
              gutter={[16, 16]}
              justify={"space-between"}
              style={{
                margin: "0.84rem",
                borderBottom: "1px solid var(--movetodata-border-color-default)",
              }}
            >
              <Col>
                <Text type="secondary">{getLanguageLabel("url")}</Text>
              </Col>
              <Col>
                <span className="text-and-icon-center">
                  <Text style={{ fontSize: "0.5rem" }} strong>
                    {debugInfo.location}
                  </Text>
                  <BoslerButton
                    icon={<CopyIcon />}
                    onClick={() =>
                      debugInfo && copyToClipboard(debugInfo.location)
                    }
                    icononly
                    minimal
                    trimicononlypadding
                  />
                </span>
              </Col>
            </Row>
          </>
        )}
        <br />
      </BoslerModal>
    </>
  );
};

export default MainHeader;
