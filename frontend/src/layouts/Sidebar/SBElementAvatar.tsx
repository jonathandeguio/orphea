import { Avatar, Dropdown, MenuProps } from "antd";
import { LogoutIcon, SettingsIcon } from "assets/icons/boslerActionIcons";
import { DocumentationIcon } from "assets/icons/boslerFileIcons";
import BoslerAvatar from "components/BoslerComponents/BoslerAvatar/BoslerAvatar";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import { getLanguageLabel } from "utils/utilities";
import { refreshTokenStatus } from "../../redux/actions/tokenActions";
import { logout } from "../../redux/actions/userActions";
import { ThunkAppDispatch } from "../../redux/types/store";
import SBElement from "./SBElement";

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
        <Link
          to={`/learn/fr/`}
          onClick={(e) => {
            e.preventDefault(); // Prevent default behavior
            window.open(
              `/learn/fr/`,
              "_blank",
              "noopener,noreferrer"
            ); // Open in new tab
          }}
        >
          {getLanguageLabel("documentation")}
        </Link>
      ),
      icon: <DocumentationIcon size={14} />,
    },
    // {
    //   key: "3",
    //   label: <div>{getLanguageLabel("information")}</div>,
    //   onClick: () => setIsDebugInfoOpen(true),
    //   icon: <HelpIcon size={14} />,
    // },
    {
      key: "4",
      label: (
        <Link rel="noopener noreferrer" to="/portal/settings/profile">
          {getLanguageLabel("settings")}
        </Link>
      ),
      icon: <SettingsIcon size={14} />,
    },

    {
      key: "6",
      label: (
        <Link
          rel="noopener noreferrer"
          to="/Auth/logout"
          onClick={() => {
            dispatch(logout());
            dispatch(refreshTokenStatus());
          }}
        >
          {getLanguageLabel("logout")}
        </Link>
      ),
      icon: <LogoutIcon size={14} />,
    },
  ];
  return (
    <>
      <SBElement
        icon={
          <Dropdown
            menu={{ items: items }}
            placement="topRight"
            trigger={["hover"]}
          >
            <BoslerAvatar
              userId={user.id}
              className="cursor-ptr"
              size={iconSize && iconSize > 18 ? "default" : "small"}
              disableUserPopover
            />
          </Dropdown>
        }
      />
    </>
  );
};

export default SBElementAvatar;
