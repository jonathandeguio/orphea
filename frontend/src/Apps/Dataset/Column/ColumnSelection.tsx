import { IconForColumnType } from "Apps/Kepler/chart/charts.utils";
import { Tag } from "antd";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import { AddIcon, CrossIcon, SyncIcon } from "assets/icons/boslerActionIcons";
import { ChangeLogIcon } from "assets/icons/boslerInterfaceIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import NoData from "components/CommonUI/NoData";
import React, { useEffect, useState } from "react";
import { Column } from "react-table";
import { getLanguageLabel, isDefined } from "utils/utilities";
import styles from "./ColumnSelection.module.scss";
import LocalColumnStats from "./LocalColumnStats";
import { BoslerTypography } from "components/CommonUI/BoslerTypography";
import { Opacity } from "tsparticles-engine";
interface IProps {
  columns: any;
  tableInstance: any;
}

interface IColumnElementProps {
  tableInstance: any;
  column: any;
  showVisibleOnes: boolean;
  data: any;
}

const ColumnElement = ({
  tableInstance,
  column,
  showVisibleOnes,
  data,
}: IColumnElementProps) => {
  const [showBtn, setShowBtn] = useState<boolean>(false);
  const [showStats, setShowStats] = useState<boolean>(false);
  const isVisible = tableInstance.getColumn(String(column.id))?.getIsVisible();
  const handleColumnChange = (e: any, column: any) => {
    e.preventDefault();
    e.stopPropagation();
    setShowBtn(false);
    setShowStats(false);
    tableInstance.getColumn(String(column.id))?.toggleVisibility();
  };

  if (showVisibleOnes && !isVisible) {
    return null;
  } else if (!showVisibleOnes && isVisible) {
    return null;
  }

  return (
    <div className={styles.columnSelection_container}>
      <div
        key={column.id}
        className={styles.columnSelection_element}
        style={
          isVisible && showBtn
            ? {
                backgroundColor: "var(--movetodata-bkg-color-muted)",
              }
            : {}
        }
        onMouseEnter={() => {
          setShowBtn(true);
        }}
        onMouseLeave={() => {
          setShowBtn(false);
        }}
        onClick={() => {
          if (isVisible) setShowStats(!showStats);
        }}
      >
        <div className={styles.columnSelection_text}>
          <IconForColumnType type={column.type} />
          <BoslerTypography tooltip ellipsis>
            {column.id}
          </BoslerTypography>
        </div>
        <BoslerButton
          style={{ opacity: showBtn ? 1 : 0 }}
          intent={isVisible ? "dangerous" : "primary"}
          onClick={(e: any) => handleColumnChange(e, column)}
          size="small"
          borderless
          icononly
          icon={isVisible ? <CrossIcon /> : <AddIcon />}
        >
          {isVisible ? getLanguageLabel("remove") : getLanguageLabel("add")}
        </BoslerButton>
      </div>
      {showStats && <LocalColumnStats column={column} data={data} />}
    </div>
  );
};

const ColumnSelection = ({ columns, tableInstance }: IProps) => {
  const [searchText, setSearchText] = useState<string>("");
  const [filteredColumns, setFilteredColumns] = useState(columns);
  const handleVisibilityReset = () => {
    tableInstance.resetColumnVisibility();
  };

  const handleColumnsSwap = () => {
    filteredColumns.map((column: any) => {
      tableInstance.getColumn(String(column.id))?.toggleVisibility();
    });
  };

  useEffect(() => {
    if (isDefined(columns)) {
      const newFilteredColumns = columns.filter(
        (column: Column) =>
          column.id &&
          column.id.toLowerCase().includes(searchText.toLowerCase())
      );
      setFilteredColumns(newFilteredColumns);
    }
  }, [searchText]);

  useEffect(() => {
    setFilteredColumns(columns);
  }, [columns]);

  console.log("COLUMNS : ", columns);
  console.log("FILTERED COLS : ", filteredColumns);

  return (
    <div className={styles.container}>
      <div className={styles.columnSelection_search}>
        <BoslerInput
          placeholder={getLanguageLabel("searchColumns")}
          onChange={(e) => setSearchText(e.target.value)}
        />
        <BoslerButton
          icon={<SyncIcon />}
          icononly
          onClick={handleVisibilityReset}
          minimal
          size="small"
        >
          {getLanguageLabel("reset")}
        </BoslerButton>
        <BoslerButton
          icon={<ChangeLogIcon />}
          icononly
          onClick={handleColumnsSwap}
          minimal
          size="small"
        >
          Exchange selected and unselected columns
        </BoslerButton>
      </div>
      <div className="BoslerHeader1 --flex-gap5">
        {getLanguageLabel("columns")} {getLanguageLabel("selected")}{" "}
        <Tag color="green">
          {
            tableInstance
              .getAllLeafColumns()
              .filter((column: any) => column.getIsVisible()).length
          }{" "}
          / {columns.length}
        </Tag>
      </div>
      {filteredColumns.map((column: any) => {
        return (
          <ColumnElement
            column={column}
            tableInstance={tableInstance}
            showVisibleOnes
            data={tableInstance.getRowModel().rows}
          />
        );
      })}
      <div className="BoslerHeader1">{getLanguageLabel("columns")}</div>
      {tableInstance.getIsAllColumnsVisible() && (
        <div>
          <NoData icon={<SearchEmptyState />} />
        </div>
      )}
      {filteredColumns.map((column: any) => {
        return (
          <ColumnElement
            column={column}
            tableInstance={tableInstance}
            showVisibleOnes={false}
            data={tableInstance.getRowModel().rows}
          />
        );
      })}
    </div>
  );
};

export default ColumnSelection;
