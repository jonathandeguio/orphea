import { Avatar, Dropdown, MenuProps } from "antd";
import { LogoutIcon, SettingsIcon } from "assets/icons/movetodataActionIcons";
import { DocumentationIcon } from "assets/icons/movetodataFileIcons";
import { HelpIcon } from "assets/icons/movetodataMiscellaneousIcons";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { getLanguageLabel, getUserDocsLanguage } from "utils/utilities";
import { refreshTokenStatus } from "redux/actions/tokenActions";
import { logout } from "redux/actions/userActions";
import { ThunkAppDispatch } from "redux/types/store";
import DebugInfoModal from "./DebugInfoModal";
import SBElement from "./SBElement";
import { UserIcon } from "assets/icons/movetodataInterfaceIcons";

interface TProps {
  iconSize?: number;
  showText?: any;
  selected?: boolean;
  onClick?: any;
  ref?: any;
}

const SBElementAvatar = ({
  iconSize,
  showText,
  selected,
  onClick,
  ref,
}: TProps) => {
  const navigate = useNavigate();
  const dispatch = useDispatch<ThunkAppDispatch>();
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  const [isDebugInfoOpen, setIsDebugInfoOpen] = useState(false);

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
        <a
          href={`/learn/${getUserDocsLanguage()}`}
          target="_blank"
          rel="noreferrer"
        >
          {getLanguageLabel("documentation")}
        </a>
      ),
      icon: <DocumentationIcon size={14} />,
    },
    {
      key: "3",
      label: <div>{getLanguageLabel("information")}</div>,
      onClick: () => setIsDebugInfoOpen(true),
      icon: <HelpIcon size={14} />,
    },
    {
      key: "4",
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
      key: "6",
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
  console.log(user.profileImage);
  return (
    <>
      <SBElement
        icon={
          <Dropdown
            menu={{ items: items }}
            placement="topRight"
            trigger={["hover"]}
          >
            <Avatar
              className="cursor-ptr"
              src={
                user.profileImage && user.profileImage != ""
                  ? user.profileImage
                  : null
              }
              size={iconSize && iconSize > 18 ? "default" : "small"}
            >
              {user.name ? user.name.charAt(0).toUpperCase() : "B"}
            </Avatar>
          </Dropdown>
        }
      />

      {isDebugInfoOpen && (
        <DebugInfoModal
          isOpen={isDebugInfoOpen}
          setIsOpen={setIsDebugInfoOpen}
        />
      )}
    </>
  );
};

export default SBElementAvatar;
