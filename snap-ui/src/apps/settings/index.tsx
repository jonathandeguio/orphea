// import { Outlet, Link } from "react-router-dom";
import { EmailIcon } from "assets/icons/boslerFileIcons";
import { OpenIcon } from "assets/icons/boslerNavigationIcon";
import { getDefaultFavicon } from "components/boslerLoader/FavIconLoader";
import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Outlet, useLocation } from "react-router";
import { Link } from "react-router-dom";
import { getLanguageLabel, isDefined } from "utils/utilities";
import {
  ClearCacheRunIcon,
  HistoricalRunsIcon,
  LockIcon,
  MoreMenuIcon,
  NotificationIcon,
  PreferencesIcon,
  SparklesIcon,
} from "assets/icons/boslerActionIcons";
import {
  ChangeLogIcon,
  ComponentIcon,
  GroupsIcon,
  KeyIcon,
  UserIcon,
} from "assets/icons/boslerInterfaceIcons";
import { PulseIcon } from "assets/icons/boslerMiscellaneousIcons";
import {
  isGroupAdmin,
  isPlatformAdmin,
  isUserAdmin,
} from "redux/actions/userActions";
import { ThunkAppDispatch } from "redux/types/store";
import { CodeCellIcon } from "assets/icons/boslerEditorIcons";

const Setting = () => {
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

  const secondToLastSegment = pathSegments[pathSegments.length - 2];
  const lastSegment = pathSegments[pathSegments.length - 1];
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );

  const notSSOUser = () => {
    return user.provider == "local";
  };

  useEffect(() => {
    dispatch(isPlatformAdmin());
    dispatch(isUserAdmin());
    dispatch(isGroupAdmin());
  }, []);

  return (
    <div className="settingpage-layout">
      <div className="settingpage-layout-sidebar">
        <div className="settingpage-layout-sidebar-menuContainer">
          {secondToLastSegment == "platform" ? (
            <>
              {" "}
              <div className="settingpage-layout-sidebar-menu">
                <Link
                  to={`/portal/settings/profile`}
                  className="text-and-icon-center"
                >
                  <div className="text-and-icon-center">
                    <OpenIcon size={24} />
                    {getLanguageLabel("settings")}
                  </div>
                </Link>
                <div className="settingpage-layout-sidebar-menu-head"></div>
              </div>
              <div className="settingpage-layout-sidebar-menu"></div>
              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  {getLanguageLabel("personalization")}
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
                      <HistoricalRunsIcon /> {getLanguageLabel("timezone")}
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
                      <SparklesIcon /> {getLanguageLabel("theme")}
                    </div>
                  </div>
                </Link>
              </div>
              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  {getLanguageLabel("configuration")}
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
                      <EmailIcon /> SMTP
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
                      <ChangeLogIcon /> HTTP Proxy
                    </div>
                  </div>
                </Link>
              </div>
              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  {getLanguageLabel("access")}
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
                      <GroupsIcon /> Bulk User Creation
                    </div>
                  </div>
                </Link>

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

              </div>
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
                  {getLanguageLabel("account")}
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
                      <UserIcon /> {getLanguageLabel("profile")}
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
                      <PreferencesIcon /> {getLanguageLabel("preferences")}
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
                      <NotificationIcon /> {getLanguageLabel("notifications")}
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
                      <KeyIcon /> {getLanguageLabel("tokens")}
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
                      <PulseIcon /> {getLanguageLabel("loginActivity")}
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
                        <MoreMenuIcon /> {getLanguageLabel("changePassword")}
                      </div>
                    </div>
                  </Link>
                )}
                {platformAdmin && <Link to={`mfaConfig`}>
                  <div
                    className={
                      lastSegment == "mfaConfig"
                        ? "settingpage-layout-sidebar-menu-subHeadSelected"
                        : "settingpage-layout-sidebar-menu-subHead"
                    }
                  >
                    <div className="text-and-icon-center">
                    <ClearCacheRunIcon /> {"Mfa Settings"}
                    </div>
                  </div>
                </Link>}
                
              </div>

              <div className="settingpage-layout-sidebar-menu">
                <div className="settingpage-layout-sidebar-menu-head">
                  {getLanguageLabel("access")}
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
                      <UserIcon /> {getLanguageLabel("users")}
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
                      <GroupsIcon /> {getLanguageLabel("groups")}
                    </div>
                  </div>
                </Link>
                {
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
                          <LockIcon /> SSO
                        </div>
                      </div>
                    </Link>
                  )
                }
              </div>
              {
                // check if is platform Admin
                platformAdmin && (
                  <div className="settingpage-layout-sidebar-menu">
                    <div className="settingpage-layout-sidebar-menu-head">
                      {getLanguageLabel("environments")}
                    </div>

                    <Link to={`platform/cache`}>
                      <div
                        className={
                          lastSegment == "platform"
                            ? "settingpage-layout-sidebar-menu-subHeadSelected"
                            : "settingpage-layout-sidebar-menu-subHead"
                        }
                      >
                        <div className="text-and-icon-center">
                          <ComponentIcon /> Platform{" "}
                          {getLanguageLabel("settings")}
                        </div>
                      </div>
                    </Link>
                  </div>
                )
              }
            </>
          )}
        </div>
      </div>
      <div className="settingpage-layout-outlet">
        <Outlet />
      </div>
    </div>
  );
};

export default Setting;
