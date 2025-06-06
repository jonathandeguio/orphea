import { updateDatasetHistorySourceAndCountAPI } from "Apps/Dataset/Dataset.api";
import { TDatasetMapping } from "Apps/Dataset/Dataset.contants";
import { Col, Popover, Switch, Typography } from "antd";
import {
  CrossIcon,
  HistoryIcon,
  SettingsIcon,
} from "assets/icons/boslerActionIcons";
import { CalendarIcon } from "assets/icons/boslerInterfaceIcons";
import { PlatformPagesEnum } from "common/enums";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import BoslerLoader from "components/boslerLoader";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel, openNotification } from "utils/utilities";
import {
  getDatasetMapping,
  updateCurrentTransactionMapping,
} from "../../redux/actions/datasetActions";
import { resourceModeUpdate } from "../../redux/actions/resourcePermissionActions";
import { isPlatformAdmin } from "../../redux/actions/userActions";
import {
  EDITOR_PERMISSION,
  EDIT_MODE,
  OWNER_PERMISSION,
  VIEWER_MODE,
} from "../../redux/constants/resourcePermissionConstants";
import { ThunkAppDispatch } from "../../redux/types/store";
import DatasetHistoryCalender from "./Calender/DatasetHistoryCalender";
import { HistoryStoreEnum } from "./DatasetHistory.contants";
import styles from "./DatasetHistory.module.scss";
import { TIntent } from "./DatasetHistory.types";
import { getIntent } from "./DatasetHistory.utils";
const { Text } = Typography;
interface TProps {
  datasetId: string;
  branch: string;
  datasetMapping: TDatasetMapping;
}

const DatasetHistoryHeaderAction = ({
  datasetId,
  branch,
  datasetMapping,
}: TProps) => {
  const resourcePermission = useSelector(
    (state) => (state as $TSFixMe).resourcePermission[datasetId]
  );
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );

  const [openModal, setOpenModal] = useState(false);
  const [openCalendarPopover, setOpenCalendarPopover] = useState(false);
  const [currentHistoryCountSource, setCurrentHistoryCountSource] = useState(
    datasetMapping.historyStoreType === HistoryStoreEnum.DATASET
  );
  const [currentCount, setCurrentCount] = useState(
    datasetMapping.datasetHistory
  );
  const { config } = useSelector((state) => (state as any).platformConfig);
  const [result, setResult] = useState<TIntent>("default");

  const dispatch = useDispatch<ThunkAppDispatch>();

  const [loading, setLoading] = useState(false);

  const handleHistorySetting = () => {
    setLoading(true);
    updateDatasetHistorySourceAndCountAPI(
      datasetId,
      branch,
      currentHistoryCountSource
        ? HistoryStoreEnum.DATASET
        : HistoryStoreEnum.PLATFORM,
      currentCount
    )
      .then(() => {
        dispatch(getDatasetMapping(datasetId, branch));
        setResult("success");
      })
      .catch((error) => {
        openNotification("Failed to update", "", "error");
        setResult("fail");
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const handleClose = () => {
    dispatch(
      updateCurrentTransactionMapping(
        datasetId,
        branch,
        datasetMapping.originalCurrentTransaction
      )
    );
    if (
      resourcePermission.permission == EDITOR_PERMISSION ||
      resourcePermission.permission == OWNER_PERMISSION
    ) {
      dispatch(resourceModeUpdate(EDIT_MODE, datasetId as string));
    } else {
      dispatch(resourceModeUpdate(VIEWER_MODE, datasetId as string));
    }
  };

  const handleClickChange = (open: boolean) => {
    setOpenCalendarPopover(open);
  };

  useEffect(() => {
    dispatch(isPlatformAdmin());
  }, []);

  if (!resourcePermission) {
    return <BoslerLoader />;
  }

  return (
    <div className={styles.headerAction}>
      <Popover
        content={
          <DatasetHistoryCalender
            datasetMapping={datasetMapping}
            setOpenCalendarPopover={setOpenCalendarPopover}
            page={PlatformPagesEnum.DATASET}
          />
        }
        open={openCalendarPopover}
        trigger="click"
        onOpenChange={handleClickChange}
      >
        <BoslerButton minimal icon={<CalendarIcon />} icononly />
      </Popover>

      <BoslerButton
        onClick={() => {
          setOpenModal(true);
        }}
        minimal
        icon={<SettingsIcon />}
        icononly
      />

      <BoslerButton
        onClick={() => {
          handleClose();
        }}
        intent="dangerous"
        icon={<CrossIcon />}
        icononly
        minimal
      />
      <BoslerModal
        headingIcon={<HistoryIcon />}
        heading={<Col>{getLanguageLabel("retention")}</Col>}
        open={openModal}
        footerButtonArea={
          <BoslerButton
            onClick={handleHistorySetting}
            intent={getIntent(result)}
            loading={loading}
            textTransform="none"
          >
            {getLanguageLabel("update")}
          </BoslerButton>
        }
        footerExtraText={
          getLanguageLabel("onlyPlatformAdminCanDefineCopiesToRetain")
        }
        extraActionHeading={
          <>
            <Text type="secondary">{getLanguageLabel("platformLevelRetention")}</Text> :{" "}
            {config.datasetHistory}
          </>
        }
        onCancel={() => setOpenModal(false)}
        width={600}
      >
        <br />
        <Switch
          disabled={!platformAdmin}
          size={"small"}
          checked={currentHistoryCountSource}
          onChange={() => {
            setResult("default");
            setCurrentHistoryCountSource(!currentHistoryCountSource);
          }}
        />{" "}
        Dataset Retention
        {currentHistoryCountSource && (
          <BoslerInput
            type="number"
            value={currentCount}
            onChange={(e: any) => {
              setResult("default");
              setCurrentCount(e.target.value);
            }}
          />
        )}
        <br />
        <br />
        <Text type="secondary">
          {getLanguageLabel("datasetRetentionPriority")}{" "}
          {datasetMapping.historyStoreType}
        </Text>
      </BoslerModal>
    </div>
  );
};

export default DatasetHistoryHeaderAction;
