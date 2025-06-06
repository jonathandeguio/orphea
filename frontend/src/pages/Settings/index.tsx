// import { Outlet, Link } from "react-router-dom";
import {
  DataCellsIcon,
  FromCacheIcon,
  ProjectIcon,
} from "assets/icons/boslerDataIcons";
import { CodeCellIcon, StylesIcon } from "assets/icons/boslerEditorIcons";
import { PythonIcon } from "assets/icons/boslerExternalIcons";
import { EmailIcon } from "assets/icons/boslerFileIcons";
import { OpenIcon } from "assets/icons/boslerNavigationIcon";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { BTHInternal } from "components/CommonUI/BoslerTypography";
import { getDefaultFavicon } from "components/boslerLoader/FavIconLoader";
import DebugInfoModal from "layouts/Sidebar/DebugInfoModal";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Outlet, useLocation } from "react-router";
import { Link } from "react-router-dom";
import { getLanguageLabel, isDefined } from "utils/utilities";
import {
  ClearCacheRunIcon,
  MoreMenuIcon,
  NotificationIcon,
  PreferencesIcon,
  SparklesIcon,
} from "../../assets/icons/boslerActionIcons";
import {
  ChangeLogIcon,
  ComponentIcon,
  DownloadIcon,
  GroupsIcon,
  KeyIcon,
  UploadIcon,
  UserIcon,
} from "../../assets/icons/boslerInterfaceIcons";
import {
  BugIcon,
  PulseIcon,
  TagsIcon,
  TimeZoneIcon,
} from "../../assets/icons/boslerMiscellaneousIcons";
import {
  isGroupAdmin,
  isPlatformAdmin,
  isUserAdmin,
} from "../../redux/actions/userActions";
import { RootState, ThunkAppDispatch } from "../../redux/types/store";
import "./SettingsSidebar.scss";

const Setting = () => {
  const [currentWindowSize, setCurrentWindowSize] = useState<number>(
    window.innerWidth
  );
  const dispatch = useDispatch<ThunkAppDispatch>();

  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  useEffect(() => {
    document.title = getLanguageLabel("settings");
    let favicon = document.querySelector('link[rel="icon"]') as any;
    favicon.href = getDefaultFavicon();

    return () => {
      document.title =
        isDefined(config) && isDefined(config.platformName)
          ? config.platformName
          : "Bosler";
    };
  }, []);

  const location = useLocation();
  const pathSegments = location.pathname.split("/");

  const [isDebugInfoOpen, setIsDebugInfoOpen] = useState(false);

  const secondToLastSegment = pathSegments[pathSegments.length - 2];
  const lastSegment = pathSegments[pathSegments.length - 1];
  const { user } = useSelector((state: RootState) => state.userDetails);
  const [platformAdmin, setPlatformAdmin] = useState<boolean>(false);

  const [userAdmin, setuserAdmin] = useState<boolean>(false);
  const notSSOUser = () => {
    return user.provider == "local";
  };

  useEffect(() => {
    dispatch(isPlatformAdmin()).then((data: boolean) => setPlatformAdmin(data));
    dispatch(isUserAdmin()).then((data: boolean) => setuserAdmin(data));
    dispatch(isGroupAdmin());
  }, []);

  useEffect(() => {
    const handleResize = () => {
      setCurrentWindowSize(window.innerWidth);
    };
    window.addEventListener("resize", handleResize);
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  return (
    <div className="settingpage-layout">
      <div className="settingpage-layout-sidebar">
        <div className="settingpage-layout-sidebar-menuContainer">
          {platformAdmin && secondToLastSegment == "platform" ? (
            <>
              {" "}
              <div className="settingpage-layout-sidebar-menu">
                <Link
                  to={`/portal/settings/profile`}
                  className="text-and-icon-center back-icon-settings-page"
                >
                  <div className="text-and-icon-center">
                    <OpenIcon size={24} />
                    <span className="--hide-on-small-screen">
                      {getLanguageLabel("settings")}
                    </span>
                  </div>
                </Link>
                <div className="settingpage-layout-sidebar-menu-head"></div>
              </div>
              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  <BTHInternal>
                    <span className="--hide-on-small-screen">
                      {getLanguageLabel("product")}
                    </span>
                  </BTHInternal>
                </div>
                <Link to={`platform/license`}>
                  <div
                    className={
                      lastSegment == "license"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <KeyIcon />
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("license")}
                      </span>
                    </div>
                  </div>
                </Link>
              </div>
              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  <BTHInternal>
                    <span className="--hide-on-small-screen">
                      {getLanguageLabel("dataset")}
                    </span>
                  </BTHInternal>
                </div>

                <Link to={`platform/cache`}>
                  <div
                    className={
                      lastSegment == "cache"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <FromCacheIcon />
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("caching")}
                      </span>
                    </div>
                  </div>
                </Link>

                <Link to={`platform/history`}>
                  <div
                    className={
                      lastSegment == "history"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <ComponentIcon />
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("history")}
                      </span>
                    </div>
                  </div>
                </Link>
                <Link to={`platform/upload`}>
                  <div
                    className={
                      lastSegment == "upload"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <UploadIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("upload")}
                      </span>
                    </div>
                  </div>
                </Link>
                <Link to={`platform/download`}>
                  <div
                    className={
                      lastSegment == "download"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <DownloadIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("download")}
                      </span>
                    </div>
                  </div>
                </Link>
              </div>
              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  <BTHInternal>
                    <span className="--hide-on-small-screen">
                      {getLanguageLabel("personalization")}
                    </span>
                  </BTHInternal>
                </div>

                <Link to={`platform/timezone`}>
                  <div
                    className={
                      lastSegment == "timezone"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <TimeZoneIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("timezone")}
                      </span>
                    </div>
                  </div>
                </Link>
                <Link to={`platform/theme`}>
                  <div
                    className={
                      lastSegment == "theme"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <StylesIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("theme")}
                      </span>
                    </div>
                  </div>
                </Link>
              </div>
              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  <BTHInternal>
                    <span className="--hide-on-small-screen">
                      {getLanguageLabel("configuration")}
                    </span>
                  </BTHInternal>
                </div>

                <Link to={`platform/smtp`}>
                  <div
                    className={
                      lastSegment == "smtp"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <EmailIcon />{" "}
                      <span className="--hide-on-small-screen">SMTP</span>
                    </div>
                  </div>
                </Link>
                <Link to={`platform/httpProxy`}>
                  <div
                    className={
                      lastSegment == "httpProxy"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <ChangeLogIcon />
                      <span className="--hide-on-small-screen">HTTP Proxy</span>
                    </div>
                  </div>
                </Link>
                <Link to={`platform/artifactory`}>
                  <div
                    className={
                      lastSegment == "artifactory"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <PythonIcon />
                      <span className="--hide-on-small-screen">
                        Python Artifactory
                      </span>
                    </div>
                  </div>
                </Link>
                {/* <Link to={`platform/branch`}>
                  <div
                    className={
                      lastSegment == "branch"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <GitNewBranchIcon />{" "}
                      <span className="--hide-on-small-screen">Branch</span>
                    </div>
                  </div>
                </Link> */}
                {/* <Link to={`platform/backingFs`}>
                  <div
                    className={
                      lastSegment == "backingFs"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <HistoricalRunsIcon />
                      <span className="--hide-on-small-screen">BackingFs</span>
                    </div>
                  </div>
                </Link> */}
                <Link to={`platform/git`}>
                  <div
                    className={
                      lastSegment == "git"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <CodeCellIcon />{" "}
                      <span className="--hide-on-small-screen">Git</span>
                    </div>
                  </div>
                </Link>
                <Link to={`platform/spark`}>
                  <div
                    className={
                      lastSegment == "spark"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <SparklesIcon />{" "}
                      <span className="--hide-on-small-screen">Spark</span>
                    </div>
                  </div>
                </Link>
                <Link to={`platform/datamart`}>
                  <div
                    className={
                      lastSegment == "datamart"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <DataCellsIcon />{" "}
                      <span className="--hide-on-small-screen">Datamart</span>
                    </div>
                  </div>
                </Link>
              </div>
              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  <BTHInternal>
                    <span className="--hide-on-small-screen">
                      {getLanguageLabel("access")}
                    </span>
                  </BTHInternal>
                </div>

                <Link to={`platform/bulkUserCreation`}>
                  <div
                    className={
                      lastSegment == "bulkUserCreation"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <GroupsIcon />{" "}
                      <span className="--hide-on-small-screen">
                        Bulk User Creation
                      </span>
                    </div>
                  </div>
                </Link>
              </div>
              <Link to={`platform/mfa`}>
                <div
                  className={
                    lastSegment == "mfa"
                      ? "settingpage-layout-sidebar-menu-subHeadSelected"
                      : "settingpage-layout-sidebar-menu-subHead"
                  }
                >
                  <div className="text-and-icon-center">
                    <ClearCacheRunIcon /> Multi Factor Authentication
                  </div>
                </div>
              </Link>
              {/* {
                // check if is platform Admin and is development
                isDev && (
                  <div className="settingpage-layout-sidebar-menu">
                    <div className="settingpage-layout-sidebar-menu-head">
                      {getLanguageLabel("developer")}{" "}
                      {getLanguageLabel("settings")}
                    </div>
                    <Link to={`platform/developerSettings`}>
                      <div
                        className={
                          lastSegment == "developerSettings"
                            ? "settingpage-layout-sidebar-menu-subHeadSelected"
                            : "settingpage-layout-sidebar-menu-subHead"
                        }
                      >
                        <div className="text-and-icon-center">
                          <VariablesIcon /> {getLanguageLabel("developer")}{" "}
                          {getLanguageLabel("space")}
                        </div>
                      </div>
                    </Link>
                  </div>
                )
              } */}
            </>
          ) : (
            <>
              <div className="settingpage-layout-sidebar-menu"></div>
              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  <BTHInternal>
                    <span className="--hide-on-small-screen">
                      {getLanguageLabel("account")}
                    </span>
                  </BTHInternal>
                </div>

                <Link to={`profile`}>
                  <div
                    className={
                      lastSegment == "profile"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <UserIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("profile")}
                      </span>
                    </div>
                  </div>
                </Link>
                <Link to={`preferences`}>
                  <div
                    className={
                      lastSegment == "preferences"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <PreferencesIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("preferences")}
                      </span>
                    </div>
                  </div>
                </Link>
                <Link to={`notifications`}>
                  <div
                    className={
                      lastSegment == "notifications"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <NotificationIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("notifications")}
                      </span>
                    </div>
                  </div>
                </Link>

                <Link to={`tokens`}>
                  <div
                    className={
                      lastSegment == "tokens"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <KeyIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("tokens")}
                      </span>
                    </div>
                  </div>
                </Link>
                <Link to={`userProjects`}>
                  <div
                    className={
                      lastSegment == "userProjects"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <ProjectIcon />
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("yourProjects")}
                      </span>
                    </div>
                  </div>
                </Link>
                <Link to={`userGroups`}>
                  <div
                    className={
                      lastSegment == "userGroups"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <GroupsIcon />
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("yourGroups")}
                      </span>
                    </div>
                  </div>
                </Link>
                <Link to={`loginActivity`}>
                  <div
                    className={
                      lastSegment == "loginActivity"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <PulseIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("loginActivity")}
                      </span>
                    </div>
                  </div>
                </Link>

                {notSSOUser() && (
                  <Link to={`changePassword`}>
                    <div
                      className={
                        lastSegment == "changePassword"
                          ? "settingpage-layout-sidebar-menu-subHeadSelected"
                          : "settingpage-layout-sidebar-menu-subHead"
                      }
                    >
                      <div className="text-and-icon-center">
                        <MoreMenuIcon />{" "}
                        <span className="--hide-on-small-screen">
                          {getLanguageLabel("changePassword")}
                        </span>
                      </div>
                    </div>
                  </Link>
                )}
                {config.mfaEnabled &&
                (platformAdmin === true || userAdmin === true) ? (
                  <Link to={`mfaReset`}>
                    <div
                      className={
                        lastSegment == "mfaReset"
                          ? "settingpage-layout-sidebar-menu-subHeadSelected"
                          : "settingpage-layout-sidebar-menu-subHead"
                      }
                    >
                      <div className="text-and-icon-center">
                        <ClearCacheRunIcon />{" "}
                        <span className="--hide-on-small-screen">
                          {getLanguageLabel("authentication")}
                        </span>
                      </div>
                    </div>
                  </Link>
                ) : (
                  ""
                )}
              </div>

              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  <BTHInternal>
                    <span className="--hide-on-small-screen">
                      {getLanguageLabel("access")}
                    </span>
                  </BTHInternal>
                </div>

                <Link to={`users`}>
                  <div
                    className={
                      lastSegment == "users"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <UserIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("users")}
                      </span>
                    </div>
                  </div>
                </Link>
                <Link to={`groups`}>
                  <div
                    className={
                      lastSegment == "groups"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                      <GroupsIcon />{" "}
                      <span className="--hide-on-small-screen">
                        {getLanguageLabel("groups")}
                      </span>
                    </div>
                  </div>
                </Link>
                {/* {
                  // check if is platform Admin
                  platformAdmin && (
                    <Link to={`sso`}>
                      <div
                        className={
                          lastSegment == "sso"
                            ? "settingpage-layout-sidebar-menu-subHeadSelected"
                            : "settingpage-layout-sidebar-menu-subHead"
                        }
                      >
                        <div className="text-and-icon-center">
                          <LockIcon />{" "}
                          <span className="--hide-on-small-screen">SSO</span>
                        </div>
                      </div>
                    </Link>
                  )
                } */}
              </div>
              {
                // check if is platform Admin
                platformAdmin && (
                  <div className="settingpage-layout-sidebar-menu">
                    <div className="settingpage-layout-sidebar-menu-head">
                      <BTHInternal>
                        <span className="--hide-on-small-screen">
                          {getLanguageLabel("environments")}
                        </span>
                      </BTHInternal>
                    </div>
                    <Link to={`tags`}>
                      <div
                        className={
                          lastSegment == "tags"
                            ? "settingpage-layout-sidebar-menu-subHeadSelected"
                            : "settingpage-layout-sidebar-menu-subHead"
                        }
                      >
                        <div className="text-and-icon-center">
                          <TagsIcon />{" "}
                          <span className="--hide-on-small-screen">
                            {getLanguageLabel("resource") +
                              " " +
                              getLanguageLabel("tags")}
                          </span>
                        </div>
                      </div>
                    </Link>

                    <Link to={`platform/license`}>
                      <div
                        className={
                          lastSegment == "platform"
                            ? "settingpage-layout-sidebar-menu-subHeadSelected"
                            : "settingpage-layout-sidebar-menu-subHead"
                        }
                      >
                        <div className="text-and-icon-center">
                          <ComponentIcon />{" "}
                          <span className="--hide-on-small-screen">
                            {"Platform" + " " + getLanguageLabel("settings")}
                          </span>
                        </div>
                      </div>
                    </Link>
                  </div>
                )
              }
            </>
          )}
        </div>
        <BoslerButton
          onClick={() => setIsDebugInfoOpen(true)}
          minimal
          icon={<BugIcon />}
          textTransform={"capitalize"}
        >
          {getLanguageLabel("information")}
        </BoslerButton>
        {isDebugInfoOpen && (
          <DebugInfoModal
            isOpen={isDebugInfoOpen}
            setIsOpen={setIsDebugInfoOpen}
          />
        )}
      </div>

      <div className="settingpage-layout-outlet">
        <Outlet />
      </div>
    </div>
  );
};

export default Setting;
