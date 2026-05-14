import { DatasetColumn } from "Apps/Kepler/kepler";
import { Divider, Form, Select } from "antd";
import React, { useCallback, useEffect } from "react";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { isDefined } from "utils/utilities";
import { IconForColumnType } from "../charts.utils";
interface Props {
  name: any;
  rules?: any;
  listName?: any;
  onSelect?: any;
  filter?: any;
  dropdownListPrefix?: any;
  customStyle?: any;
  field?: any;
}

// Dont remove datasetId, used on filters component to read value | Can be improved later
export const ColumnSelectLabel = ({
  name,
  type,
  datasetId,
}: {
  name: string;
  type: string;
  datasetId?: string;
}) => {
  return (
    <div
      style={{
        display: "flex",
        gap: "0.4rem",
        flexDirection: "row",
        alignItems: "center",
        height: "100%",
        width: "100%",
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          width: "100%",
        }}
      >
        <span style={{ fontSize: "small" }}>{name}</span>
        <span style={{ display: "flex", marginLeft: "auto" }}>
          <div className="text-and-icon-center">
            <div className="data-type-label">{type}</div>
            <IconForColumnType type={type} style={{ color: "#08c" }} />
          </div>
        </span>
      </div>
    </div>
  );
};

const ColumnSelect: React.FC<Props> = (props) => {
  let columns = useSelector((state: RootState) => state.kepler.columns);
  let dataForm = useSelector((state: RootState) => state.kepler.dataForm);
  let columnsMap: { [key: string]: DatasetColumn } = {};

  columns.forEach(
    (column: DatasetColumn) => (columnsMap[column.headerName] = column)
  );

  let selectedColumn: any = null;

  if (isDefined(props.listName)) {
    if (Array.isArray(props.name)) {
      selectedColumn = dataForm.getFieldValue([props.listName, ...props.name]);
    } else {
      selectedColumn = dataForm.getFieldValue([props.listName, props.name]);
    }
  } else {
    selectedColumn = dataForm.getFieldValue(props.name);
  }

  useEffect(() => {
    props.onSelect?.(
      isDefined(selectedColumn) ? columnsMap[selectedColumn] : undefined
    );
  }, [selectedColumn]);

  if (isDefined(props.filter)) {
    columns = columns.filter((column: DatasetColumn) => props.filter(column));
  }

  const dropdownRenderer = useCallback(
    (menu: any) => {
      return (
        <>
          {isDefined(props.dropdownListPrefix) ? (
            <>
              <>{props.dropdownListPrefix}</>
              <Divider style={{ margin: "3px 0" }} />
            </>
          ) : (
            <></>
          )}

          {menu}
        </>
      );
    },
    [props.dropdownListPrefix]
  );

  const { Option } = Select;
  return (
    <Form.Item
      name={props.name}
      rules={props.rules}
      style={{ width: "100%", minWidth: "0", ...props.customStyle }}
    >
      <Select
        popupMatchSelectWidth={false}
        placeholder={`Choose a column`}
        className=""
        showSearch
        allowClear
        optionFilterProp="value"
        onChange={(value, option) => props.onSelect?.(columnsMap[value])}
        dropdownRender={dropdownRenderer}
      >
        {columns.map((column: any) => (
          <Option value={column.headerName}>
            <ColumnSelectLabel name={column.headerName} type={column.type} />
          </Option>
        ))}
      </Select>
    </Form.Item>
  );
};

export default ColumnSelect;
