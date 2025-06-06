import { NotificationBell } from "Apps/Notifications/NotificationBell.view";
import { Divider } from "antd";
import {
  BuildIcon,
  HistoryIcon,
  RefreshIcon,
} from "assets/icons/boslerActionIcons";
import { BooleanIcon } from "assets/icons/boslerDataIcons";
import { FolderIcon } from "assets/icons/boslerFileIcons";
import {
  HomeIcon,
  ScheduledRunIcon,
  UserIcon,
} from "assets/icons/boslerInterfaceIcons";
import { StarIcon } from "assets/icons/boslerMiscellaneousIcons";
import { getIsConnectAdmin } from "common/common.api";
import BoslerCommandPalette from "components/CommandPalette/CommandPalette.view";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useSearchParams } from "react-router-dom";
import { getLanguageLabel, isLicenseKeyUsedValid } from "utils/utilities";
import { setIsConnectAdmin } from "../../redux/actions/userActions";
import { RootState } from "../../redux/types/store";
import SBElement from "./SBElement";
import SBElementAvatar from "./SBElementAvatar";
import SBElementLogo from "./SBElementLogo";
import SBElementSearch from "./SBElementSearch";
import styles from "./Sidebar.module.scss";
import { LayoutViewEnum } from "./Sidebar.utils";

const Sidebar = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { info } = useSelector((state) => (state as any).license);
  const [sidebarAttributes, setSidebarAttributes] = useState({
    iconSize: 20,
    showText: false,
    width: "30px",
  });

  const { user } = useSelector((state: RootState) => state.userDetails);
  const [selectedSideBarTab, setSelectedSideBarTab] = useState<any>("None");
  const isEmbedded = searchParams.get("embedded");
  const dispatch = useDispatch();

  useEffect(() => {
    getIsConnectAdmin().then(({ data }) => {
      dispatch(setIsConnectAdmin(data));
    });
  }, []);

  useEffect(() => {
    if (user.preferences.layoutView == LayoutViewEnum.COMFORTABLE) {
      setSidebarAttributes({
        iconSize: 22,
        showText: true,
        width: "46px",
      });
    } else if (user.preferences.layoutView == LayoutViewEnum.COMPACT) {
      setSidebarAttributes({
        iconSize: 20,
        showText: false,
        width: "30px",
      });
    }
  }, [user.preferences]);

  if (isEmbedded || !isLicenseKeyUsedValid(info)) return <></>;

  return (
    <div className={styles.wrapper}>
      <div
        className={styles.container}
        style={{ width: sidebarAttributes.width }}
      >
        <div className={styles.top}>
          <div className={styles.logo}>
            <SBElementLogo
              iconSize={sidebarAttributes.iconSize}
              showText={sidebarAttributes.showText}
            />
          </div>
          <Divider
            style={{
              marginTop: "10px",
              marginBottom: "10px",
            }}
          />

          <SBElement
            icon={
              <HomeIcon
                color={
                  selectedSideBarTab == "home"
                    ? "var(--icon-color-selected)"
                    : "white"
                }
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={getLanguageLabel("home")}
            text="home"
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("home");
            }}
            navigateLink="/"
            selected={selectedSideBarTab == "home"}
          />

          <SBElementSearch
            iconSize={sidebarAttributes.iconSize}
            showText={sidebarAttributes.showText}
          />

          <SBElement
            icon={
              <FolderIcon
                color={
                  selectedSideBarTab == "projects"
                    ? "var(--icon-color-selected)"
                    : "white"
                }
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={getLanguageLabel("projects")}
            text="projects"
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("projects");
            }}
            navigateLink="/portal/projects"
            selected={selectedSideBarTab == "projects"}
          />

          <SBElement
            icon={
              <BuildIcon
                color={
                  selectedSideBarTab == "builds"
                    ? "var(--icon-color-selected)"
                    : "white"
                }
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={getLanguageLabel("builds")}
            text="builds"
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("builds");
            }}
            navigateLink="/portal/builds"
            selected={selectedSideBarTab == "builds"}
          />

          <SBElement
            icon={
              <ScheduledRunIcon
                color={
                  selectedSideBarTab == "schedules"
                    ? "var(--icon-color-selected)"
                    : "white"
                }
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={getLanguageLabel("schedules")}
            text="schedules"
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("schedules");
            }}
            navigateLink="/portal/schedules"
            selected={selectedSideBarTab == "schedules"}
          />

          <Divider
            style={{
              marginTop: "10px",
              marginBottom: "10px",
            }}
          />

          <SBElement
            icon={
              <StarIcon
                color={"#ffc940"}
                stroke={"#ffc940"}
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={getLanguageLabel("favourites")}
            text="favs"
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("favourites");
            }}
            navigateLink="/portal/favourites"
            selected={selectedSideBarTab == "favourites"}
          />

          <SBElement
            icon={
              <HistoryIcon
                color={
                  selectedSideBarTab == "recentlyViewed"
                    ? "var(--icon-color-selected)"
                    : "white"
                }
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={getLanguageLabel("recentlyViewed")}
            text="recentlyViewed"
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("recentlyViewed");
            }}
            navigateLink="/portal/recentlyViewed"
            selected={selectedSideBarTab == "recentlyViewed"}
          />

          <SBElement
            icon={
              <UserIcon
                color={
                  selectedSideBarTab == "createdByYou"
                    ? "var(--icon-color-selected)"
                    : "white"
                }
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={getLanguageLabel("createdByYou")}
            text="createdByYou"
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("createdByYou");
            }}
            navigateLink="/portal/createdByYou"
            selected={selectedSideBarTab == "createdByYou"}
          />

          <SBElement
            icon={
              <RefreshIcon
                color={
                  selectedSideBarTab == "updatedByYou"
                    ? "var(--icon-color-selected)"
                    : "white"
                }
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={getLanguageLabel("updatedByYou")}
            text="updatedByYou"
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("updatedByYou");
            }}
            navigateLink="/portal/updatedByYou"
            selected={selectedSideBarTab == "updatedByYou"}
          />

          <Divider
            style={{
              marginTop: "10px",
              marginBottom: "10px",
            }}
          />
          <BoslerCommandPalette
            iconSize={sidebarAttributes.iconSize}
            showText={sidebarAttributes.showText}
          />

          
          <SBElement
            icon={
              <BooleanIcon
                color={
                  selectedSideBarTab == "accessManager"
                    ? "var(--icon-color-selected)"
                    : "white"
                }
                size={sidebarAttributes.iconSize + 2}
              />
            }
            tooltip={getLanguageLabel("accessManager")}
            text={"accessManager"}
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("accessManager");
            }}
            navigateLink="/portal/access_manager"
            selected={selectedSideBarTab == "accessManager"}
          />
          {/* <SBElement
            icon={
              <LightBulbIcon
                color={
                  selectedSideBarTab == "learn"
                    ? "var(--icon-color-selected)"
                    : "white"
                }
                size={sidebarAttributes.iconSize + 2}
              />
            }
            tooltip={getLanguageLabel("learn")}
            text={"learn"}
            showText={sidebarAttributes.showText}
            onClick={() => {
              setSelectedSideBarTab("learn");
            }}
            navigateLink="/portal/learn"
            selected={selectedSideBarTab == "learn"}
          /> */}
        </div>

        <div className={styles.bottom}>
          <NotificationBell
            iconSize={sidebarAttributes.iconSize}
            showText={sidebarAttributes.showText}
          />
          <SBElementAvatar
            iconSize={sidebarAttributes.iconSize}
            showText={sidebarAttributes.showText}
          />
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
