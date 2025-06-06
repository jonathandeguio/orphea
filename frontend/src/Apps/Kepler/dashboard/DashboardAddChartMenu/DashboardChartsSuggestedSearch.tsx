import { DatePicker, Form, MenuProps, Popover } from "antd";
import { CheckboxChangeEvent } from "antd/es/checkbox";
import { Checkbox } from "antd/lib";
import { Resource } from "Apps/explorer/explorer";
import { getNodeIcon } from "Apps/explorer/explorer.utils";
import { DragHandleVerticalIcon } from "assets/icons/boslerActionIcons";
import { CalendarIcon, UserIcon } from "assets/icons/boslerInterfaceIcons";
import { ArrowDownIcon, ArrowUpIcon } from "assets/icons/boslerNavigationIcon";
import {
  SearchEmptyState,
  WarningState,
} from "assets/Illustrations/EmptyState";
import UserInfo from "common/components/UserInfo";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerLoader from "components/boslerLoader";
import { BoslerTypography } from "components/CommonUI/BoslerTypography";
import NoData from "components/CommonUI/NoData";
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useParams } from "react-router";
import { RootState } from "redux/types/store";
import { getLanguageLabel, getTimeDisplay } from "utils/utilities";
import { getSuggestedChartsAPI } from "../Dashboard.api";
import {
  ASCENDING,
  DESCENDING,
  chartsFilters,
} from "./DashboardAddChartMenuChartsTab.constants";
import {
  ISuggestedChartsFilters,
  SortOrder,
  SortOrderBy,
} from "./DashboardAddChartMenuChartsTab.types";

interface IProps {
  isLoading: boolean;
  isError: string | false;
  charts: Resource[] | undefined;
}

const SuggestedChartsList = ({ charts, isLoading, isError }: IProps) => {
  if (isLoading) {
    return <BoslerLoader />;
  }

  if (isError) {
    return (
      <NoData heading={"Error"} subHeading={isError} icon={<WarningState />} />
    );
  }

  if (!charts) {
    return (
      <NoData
        heading={"No data"}
        subHeading={"No result found the process"}
        icon={<SearchEmptyState />}
      />
    );
  }

  return (
    <div className="chart-list">
      {charts?.map((chart) => (
        <div
          key={chart.id}
          className="chart-card draggable"
          draggable
          unselectable="on"
          onDragStart={(e) => {
            // if (e.target === e.currentTarget) {
            e.dataTransfer.setData("text/plain", "layoutElement-chart");
            e.dataTransfer.setData("chart", chart.id);
            // }
          }}
        >
          <DragHandleVerticalIcon />
          <div>
            {getNodeIcon(chart.type as string, chart.subType as string)}
          </div>
          <div className="chart-card-content">
            <BoslerTypography variant="H1">{chart.name}</BoslerTypography>
            <div>
              <BoslerTypography variant="para">
                {getLanguageLabel("createdBy")}
              </BoslerTypography>{" "}
              <UserInfo userId={chart.createdBy} size="12px" />
            </div>
            <BoslerTypography variant="para">
              {getLanguageLabel("updatedAt")} {getTimeDisplay(chart.updatedAt)}
            </BoslerTypography>
          </div>
        </div>
      ))}
    </div>
  );
};

const DashboardChartsSuggestedSearch = () => {
  const { allusers } = useSelector((state: RootState) => state.allUserDetails);
  const { user } = useSelector((state: RootState) => state.userDetails);
  const { id } = useParams<{ id: string }>();

  const [sortOrder, setSortOrder] = useState<SortOrder>(DESCENDING);
  const [sortOrderBy, setSortOrderBy] = useState<SortOrderBy>("updatedAt");
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isError, setIsError] = useState<string | false>(false);
  const [filters, setFilters] = useState<ISuggestedChartsFilters>({
    ...chartsFilters,
    createdBy: [user.id],
  });
  const [charts, setCharts] = useState<Array<Resource>>();
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFilters({ ...filters, searchText: e.target.value });
  };

  const handleSortOrderChange = () => {
    if (sortOrder === ASCENDING) {
      setSortOrder(DESCENDING);
    } else {
      setSortOrder(ASCENDING);
    }
  };

  const handleDatefilterChage = (e: string | null, key: string) => {
    setFilters((prev) => ({
      ...prev,
      [key]: e || null,
    }));
  };

  const handleChecked = (e: CheckboxChangeEvent) => {
    e.stopPropagation();
    const userId = e.target.value;
    const updatedCreatedBy = filters?.createdBy?.includes(userId)
      ? filters.createdBy?.filter((id) => id !== userId)
      : [...filters.createdBy!, userId];

    setFilters({ ...filters, createdBy: updatedCreatedBy });
  };

  const items: MenuProps["items"] = allusers?.map((usr: any) => ({
    label: (
      <Checkbox
        onChange={handleChecked}
        checked={filters.createdBy?.includes(usr.id)}
        value={usr.id}
      >
        {usr?.name}
      </Checkbox>
    ),
    key: usr?.id,
  }));

  useEffect(() => {
    setIsLoading(true);
    setIsError(false);
    getSuggestedChartsAPI(id!, filters, 0, 20, sortOrderBy, sortOrder)
      .then((res) => {
        setCharts(res.data.content);
      })
      .catch((error) => {
        setIsError(error.message);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [filters, sortOrder]);

  return (
    <div className="filter-section">
      <Form>
        <div className="filter-container">
          <div onClick={() => handleSortOrderChange()}>
            <BoslerButton
              icononly
              minimal={true}
              icon={
                sortOrder === ASCENDING ? <ArrowUpIcon /> : <ArrowDownIcon />
              }
            />
          </div>
          <div className="chart_search_input">
            <BoslerInput
              type="text"
              placeholder={getLanguageLabel("search") + " " + " charts..."}
              value={filters.searchText!}
              onChange={handleSearchChange}
              className="search-input"
            />
          </div>
          <div>
            <BoslerButton
              minimal={true}
              menuItems={items}
              icononly
              icon={<UserIcon />}
            />
          </div>
          <div>
            <Popover
              className="date-time-filter-popover"
              content={
                <>
                  <div>
                    <div>
                      <label>
                        {" "}
                        {getLanguageLabel("createdAt") +
                          " " +
                          getLanguageLabel("from")}{" "}
                        :
                      </label>
                      <DatePicker
                        type="date"
                        onChange={(date) =>
                          handleDatefilterChage(
                            date?.toString(),
                            "createdAtFrom"
                          )
                        }
                      />
                    </div>
                    <div>
                      <label>
                        {getLanguageLabel("createdAt") +
                          " " +
                          getLanguageLabel("to")}{" "}
                        :
                      </label>
                      <DatePicker
                        onChange={(date) =>
                          handleDatefilterChage(date?.toString(), "createdAtTo")
                        }
                      />
                    </div>
                  </div>
                  <div>
                    <div>
                      <label>
                        {getLanguageLabel("updatedAt") +
                          " " +
                          getLanguageLabel("from")}{" "}
                        :
                      </label>
                      <DatePicker
                        onChange={(date) =>
                          handleDatefilterChage(
                            date?.toString(),
                            "updatedAtFrom"
                          )
                        }
                      />
                    </div>
                    <div>
                      <label>
                        {getLanguageLabel("updatedAt") +
                          " " +
                          getLanguageLabel("to")}{" "}
                        :
                      </label>
                      <DatePicker
                        onChange={(date) =>
                          handleDatefilterChage(date?.toString(), "updatedAtTo")
                        }
                      />
                    </div>
                  </div>
                </>
              }
              title={
                getLanguageLabel("date") +
                " " +
                "-" +
                " " +
                getLanguageLabel("time") +
                " " +
                getLanguageLabel("filters")
              }
              trigger={"click"}
              placement="right"
            >
              <BoslerButton minimal={true} icononly icon={<CalendarIcon />} />
            </Popover>
          </div>
        </div>
      </Form>
      <SuggestedChartsList
        charts={charts}
        isLoading={isLoading}
        isError={isError}
      />
    </div>
  );
};

export default DashboardChartsSuggestedSearch;
