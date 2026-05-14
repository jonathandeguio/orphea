import { Divider } from "antd";

import { APIIcon, HomeIcon } from "assets/icons/orpheaInterfaceIcons";
import OrpheaCommandPalette from "components/CommandPalette/CommandPalette.view";
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate, useSearchParams } from "react-router-dom";
import { getLanguageLabel } from "utils/utilities";
import { RootState } from "redux/types/store";
import SBElement from "./SBElement";
import SBElementAvatar from "./SBElementAvatar";
import SBElementLogo from "./SBElementLogo";
import SBElementSearch from "./SBElementSearch";
import styles from "./Sidebar.module.scss";
import { LayoutViewEnum } from "./Sidebar.utils";
import { SyncIcon } from "assets/icons/orpheaActionIcons";

const Sidebar = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [sidebarAttributes, setSidebarAttributes] = useState({
    iconSize: 20,
    showText: false,
    width: "30px",
  });

  const { user } = useSelector((state: RootState) => state.userDetails);
  const [selectedSideBarTab, setSelectedSideBarTab] = useState<any>("None");
  const isEmbedded = searchParams.get("embedded");

  useEffect(() => {
    if (user.preferences.layoutView == LayoutViewEnum.COMFORTABLE) {
      setSidebarAttributes({
        iconSize: 22,
        showText: true,
        width: "46px",
      });
    } else if (user.preferences.layoutView == LayoutViewEnum.COMPACT) {
      setSidebarAttributes({
        iconSize: 18,
        showText: false,
        width: "27px",
      });
    }
  }, [user.preferences]);

  if (isEmbedded) return <></>;

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
                color={selectedSideBarTab == "home" ? "#9ca3b4" : "#9ca3b4"}
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={getLanguageLabel("home")}
            text="home"
            showText={sidebarAttributes.showText}
            onClick={() => {
              navigate("/");
              setSelectedSideBarTab("home");
            }}
            selected={selectedSideBarTab == "home"}
          />

          <SBElementSearch
            iconSize={sidebarAttributes.iconSize}
            showText={sidebarAttributes.showText}
          />
          <SBElement
            icon={
              <APIIcon
                color={selectedSideBarTab == "triggers" ? "#ffffff" : "#9ca3b4"}
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={"Build Triggers"}
            text="triggers"
            showText={sidebarAttributes.showText}
            onClick={() => {
              navigate("/portal/triggers");
              setSelectedSideBarTab("triggers");
            }}
            selected={selectedSideBarTab == "triggers"}
          />
          <SBElement
            icon={
              <SyncIcon
                color={
                  selectedSideBarTab == "deployments" ? "#ffffff" : "#9ca3b4"
                }
                size={sidebarAttributes.iconSize}
              />
            }
            tooltip={"Build Deployments"}
            text="deployments"
            showText={sidebarAttributes.showText}
            onClick={() => {
              navigate("/portal/deployments");
              setSelectedSideBarTab("deployments");
            }}
            selected={selectedSideBarTab == "deployments"}
          />

          <Divider
            style={{
              marginTop: "10px",
              marginBottom: "10px",
            }}
          />
          <OrpheaCommandPalette
            iconSize={sidebarAttributes.iconSize}
            showText={sidebarAttributes.showText}
          />
        </div>

        <div className={styles.bottom}>
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
