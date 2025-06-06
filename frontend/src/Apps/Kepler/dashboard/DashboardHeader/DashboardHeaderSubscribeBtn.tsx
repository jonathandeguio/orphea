import { Popover } from "antd";
import { AddUserIcon } from "assets/icons/boslerInterfaceIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerLoader from "components/boslerLoader";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel } from "utils/utilities";
import { openSubscribeMenuDashboard } from "../../../../redux/actions/dashboardActions";
import { EDIT_MODE } from "../../../../redux/constants/resourcePermissionConstants";
import { ThunkAppDispatch } from "../../../../redux/types/store";
interface TProps {
  id: string;
}
const DashboardHeaderSubscribeBtn = ({ id }: TProps) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const resourcePermission = useSelector(
    (state) => (state as $TSFixMe).resourcePermission[id]
  );

  if (!resourcePermission) return <BoslerLoader size="tiny" />;
  return resourcePermission.mode == EDIT_MODE ? (
    <Popover content="Click here to subscribe to the dashboard and receive email updates.">
      <BoslerButton
        icon={<AddUserIcon size={18} />}
        onClick={() => {
          dispatch(openSubscribeMenuDashboard());
        }}
        disabled={resourcePermission.mode != EDIT_MODE}
        icononly
        trimicononlypadding
        minimal
      >
        {getLanguageLabel("subscribe")}
      </BoslerButton>
    </Popover>
  ) : (
    <></>
  );
};

export default DashboardHeaderSubscribeBtn;
