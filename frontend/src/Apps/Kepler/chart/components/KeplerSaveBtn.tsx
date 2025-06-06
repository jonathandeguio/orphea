import { EditIcon } from "assets/icons/boslerEditorIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import { VIEWER_MODE } from "../../../../redux/constants/resourcePermissionConstants";
import { SaveIcon } from "assets/icons/boslerActionIcons";
import { useDispatch, useSelector } from "react-redux";
import { RootState, ThunkAppDispatch } from "redux/types/store";
import { initChartSave } from "redux/actions/keplerActions";

interface TProps {
  showDialog: any;
  items: any[];
  saveChart: any;
  changeResourceMode: any;
}
const KeplerSaveBtn = ({
  showDialog,
  items,
  saveChart,
  changeResourceMode,
}: TProps) => {
  const chartLoading = useSelector(
    (state: RootState) => state.kepler.chartStatus
  );
  const handleSave = () => {
    saveChart();
  };

  return (
    <>
      <BoslerButton
        intent={showDialog ? "none" : "action"}
        icon={<EditIcon />}
        menuItems={items}
        disabled={showDialog}
        textTransform="capitalize"
        onClick={() => {
          changeResourceMode(VIEWER_MODE);
        }}
      >
        {getLanguageLabel("editing")}
      </BoslerButton>
      <BoslerButton
        icon={<SaveIcon />}
        onClick={() => {
          handleSave();
        }}
        loading={chartLoading === "LOADING"}
        intent={showDialog ? "action" : "none"}
        disabled={!showDialog}
      >
        {getLanguageLabel("save")}
      </BoslerButton>
    </>
  );
};

export default KeplerSaveBtn;
