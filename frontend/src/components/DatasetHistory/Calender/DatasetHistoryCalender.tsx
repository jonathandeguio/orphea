import { getDatasetMappingTransactionsAPI } from "Apps/Dataset/Dataset.api";
import { TDatasetMapping } from "Apps/Dataset/Dataset.contants";
import { Calendar, Divider } from "antd";
import { PlatformPagesEnum } from "common/enums";
import type { Dayjs } from "dayjs";
import dayjs from "dayjs";
import "dayjs/locale/zh-cn";
import dayLocaleData from "dayjs/plugin/localeData";
import React, { Dispatch, SetStateAction } from "react";
import { useDispatch } from "react-redux";
import { convertDateToLocalDate } from "utils/utilities";
import { updateTransactionsDatasetMapping } from "../../../redux/actions/datasetActions";
import { ThunkAppDispatch } from "../../../redux/types/store";
import styles from "../DatasetHistory.module.scss";
import DatasetHistoryCalenderTransactions from "./DatasetHistoryCalenderTransactions";

dayjs.extend(dayLocaleData);

interface TProps {
  datasetMapping: TDatasetMapping;
  setOpenCalendarPopover: Dispatch<SetStateAction<boolean>>;
  page: PlatformPagesEnum;
}

const DatasetHistoryCalender = ({
  datasetMapping,
  setOpenCalendarPopover,
  page,
}: TProps) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const getDisabledDates = (value: Dayjs) => {
    if (
      datasetMapping.validDates.some(
        (dateArr) =>
          JSON.stringify(dateArr) ===
          JSON.stringify(convertDateToLocalDate(value))
      )
    ) {
      return false;
    }
    return true;
  };
  const SHOW_TRANSACTIONS = page == PlatformPagesEnum.KEPLER;

  return (
    <div className={styles.calendarWrapper}>
      <div className={styles.calendar}>
        <Calendar
          fullscreen={false}
          disabledDate={getDisabledDates as any}
          onSelect={(date, { source }) => {
            if (source === "date") {
              getDatasetMappingTransactionsAPI(
                datasetMapping.datasetId,
                datasetMapping.branch,
                date.format("YYYY-MM-DD")
              ).then(({ data }) => {
                dispatch(
                  updateTransactionsDatasetMapping(
                    datasetMapping.datasetId,
                    datasetMapping.branch,
                    data
                  )
                );

                if (page == PlatformPagesEnum.DATASET) {
                  setOpenCalendarPopover(false);
                }
              });
            }
          }}
        />
      </div>
      {SHOW_TRANSACTIONS && (
        <>
          <Divider
            type="vertical"
            style={{
              height: "300px",
            }}
          />
          <DatasetHistoryCalenderTransactions datasetMapping={datasetMapping} />
        </>
      )}
    </div>
  );
};

export default DatasetHistoryCalender;
