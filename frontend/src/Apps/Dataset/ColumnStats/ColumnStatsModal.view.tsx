import { Col, Menu, Row } from "antd";
import { SearchIcon } from "assets/icons/boslerActionIcons";
import { FilterIcon } from "assets/icons/boslerTableIcons";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerLoader from "components/boslerLoader";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  capitalizeFirstLetter,
  getLanguageLabel,
  getSocketClient,
} from "utils/utilities";
import {
  LoadColumnStatsPane,
  openColumnStatsPane,
} from "../../../redux/actions/datasetActions";
import { COLUMN_STATS_PANE_OPEN_SUCCESS } from "../../../redux/constants/datasetConstants";
import { ThunkAppDispatch } from "../../../redux/types/store";
import { getColumnTypeIcon } from "../Table/BoslerTable.utils";
const ColumnStatsModal = ({
  id,
  branch,
  transactionId,
  isVisible,
  setIsVisible,
}: {
  id: string;
  branch: string;
  transactionId: string;
  isVisible: boolean;
  setIsVisible: any;
}) => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const { data, loading: loadingTable } = useSelector(
    (state) => (state as $TSFixMe).datasetTable
  );

  const [searchText, setSearchText] = useState("");
  const [filteredColumns, setFilteredColumns] = useState([]);

  useEffect(() => {
    const newFilteredColumns = loadingTable
      ? []
      : data?.cols.filter((column: any) =>
          column.id.toLowerCase().includes(searchText.toLowerCase())
        );
    setFilteredColumns(newFilteredColumns);
  }, [loadingTable, searchText]);

  return (
    <BoslerModal
      headingIcon={<FilterIcon />}
      heading={
        <Row justify={"space-between"} align="middle">
          <Col>{getLanguageLabel("columnStats")}</Col>
        </Row>
      }
      footerExtraText={getLanguageLabel("columnStatsMsg")}
      extraActionHeading={
        <BoslerInput
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          placeholder={getLanguageLabel("searchColumns")}
          suffix={<SearchIcon />}
          autofocus
        />
      }
      open={isVisible}
      onCancel={() => setIsVisible(false)}
      width={600}
    >
      <Menu
        mode="inline"
        onSelect={() => setIsVisible(false)}
        style={{
          background: "transparent",
          border: "none",
          marginTop: "10px",
          maxHeight: "60vh",
          overflowY: "auto",
        }}
      >
        {!loadingTable ? (
          filteredColumns?.map((column: any) => {
            return (
              <Menu.Item
                onClick={() => {
                  dispatch(
                    openColumnStatsPane(column.id, id, branch, transactionId)
                  ).then((result: any) => {
                    if (typeof result == "string") {
                      const client = getSocketClient();
                      client.activate();
                      client.onConnect = () => {
                        client.subscribe(
                          `/topic/sparkResults/${result}`,
                          function (mail) {
                            if (JSON.parse(mail.body).message === "success") {
                              dispatch(LoadColumnStatsPane(result));
                            }
                          }
                        );
                      };
                    } else if (typeof result == "object")
                      dispatch({
                        type: COLUMN_STATS_PANE_OPEN_SUCCESS,
                        payload: {
                          counts: result.counts,
                          lengths: result.lengths,
                          distribution: result.distribution,
                        },
                      });
                  });
                }}
              >
                <Row justify={"space-between"} align="middle">
                  <Col>
                    <Row align="middle">
                      {getColumnTypeIcon(column.type)}
                      {column.id}
                    </Row>
                  </Col>
                  <Col className="text-and-icon-center key-binding">
                    {capitalizeFirstLetter(column.type)}
                  </Col>
                </Row>
              </Menu.Item>
            );
          })
        ) : (
          <BoslerLoader />
        )}
      </Menu>
    </BoslerModal>
  );
};

export default ColumnStatsModal;
