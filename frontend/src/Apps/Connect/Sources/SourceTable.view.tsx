import { Col, Dropdown, Row, Table, Tooltip, Typography } from "antd";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import DeleteModal from "components/Modals/DeleteModal";

import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import {
  getLanguageLabel,
  getSourceIcon,
  getTimeDisplay,
  timeConverter,
} from "utils/utilities";
import {
  MoreMenuIcon,
  SearchIcon,
} from "../../../assets/icons/boslerActionIcons";
import { EditIcon } from "../../../assets/icons/boslerEditorIcons";
import { TrashIcon } from "../../../assets/icons/boslerMiscellaneousIcons";
import GlobalSearch from "../../../helpers/GlobalSearch";
import {
  deleteSource,
  listSources,
} from "../../../redux/actions/sourceActions";
import { ThunkAppDispatch } from "../../../redux/types/store";

import { getConnectLink } from "../Connect.utils";

const { Title } = Typography;

const SourceTable2 = ({ tableList, loading }: any) => {
  const { id } = useParams();

  const dispatch = useDispatch<ThunkAppDispatch>();
  const navigate = useNavigate();

  const [deleteServiceDetails, setDeleteServiceDetails] = useState({
    modalView: false,
    id: null,
    name: "",
    disabled: true,
  });
  const [FilteredData, setFilteredData] = useState();

  const deleteSourceHandler = (resourceId: string) => {
    dispatch(deleteSource(resourceId)).then(() => {
      setDeleteServiceDetails({
        ...deleteServiceDetails,
        modalView: false,
        disabled: true,
      });
      dispatch(listSources());
    });
  };

  const columns = [
    {
      title: getLanguageLabel("name"),
      dataIndex: "name",
      key: "name",
      width: "40%",
      sorter: (a: $TSFixMe, b: $TSFixMe) => a.name.localeCompare(b.name),
      render: (text: $TSFixMe, record: $TSFixMe) => (
        <Title
          onClick={() => navigate(getConnectLink(record.id, "source"))}
          level={5}
          style={{ color: "#5C7080", cursor: "pointer" }}
        >
          <div className="text-and-icon-center">
            {getSourceIcon(record.type, record.dbmsType)}
            {"  "} {text}
          </div>
        </Title>
      ),
    },
    {
      title: getLanguageLabel("description"),
      dataIndex: "description",
      key: "description",
    },

    // {
    //   title: getLanguageLabel("numberOfLinks"),
    //   dataIndex: "links",
    //   key: "links",
    //   render: (text: any, record: any) => {
    //     return <></>;
    //   },
    // },
    {
      title: getLanguageLabel("lastUpdated"),
      dataIndex: "updatedAt",
      key: "updatedAt",
      width: "30%",
      render: (text: $TSFixMe, record: $TSFixMe) => {
        return (
          <Row justify="space-between">
            <Col>
              {record.updatedAt ? (
                <Tooltip title={timeConverter(record.updatedAt)}>
                  {getTimeDisplay(record.updatedAt)}
                </Tooltip>
              ) : (
                getLanguageLabel("noStatus")
              )}
            </Col>
            <Col>
              <Dropdown
                menu={{
                  items: [
                    {
                      label: (
                        <>
                          <div
                            onClick={() =>
                              navigate(getConnectLink(record.id, "source"))
                            }
                            className="text-and-icon-center"
                            style={{
                              width: "100%",
                            }}
                          >
                            <EditIcon />
                            {getLanguageLabel("edit")}
                          </div>
                        </>
                      ),

                      key: 0,
                    },
                    {
                      label: (
                        <>
                          <div
                            onClick={() =>
                              setDeleteServiceDetails({
                                ...deleteServiceDetails,
                                modalView: true,
                                name: record.name,
                                id: record.id,
                              })
                            }
                            className="text-and-icon-center"
                            style={{
                              color: "var(--bosler-intent-danger)",
                            }}
                          >
                            <TrashIcon color={"var(--bosler-intent-danger)"} />
                            {getLanguageLabel("delete")}
                          </div>
                        </>
                      ),

                      key: 1,
                    },
                  ],
                }}
                trigger={["click"]}
              >
                <div
                  onClick={(e) => e.preventDefault()}
                  style={{ cursor: "pointer" }}
                >
                  <MoreMenuIcon />
                </div>
              </Dropdown>
            </Col>
          </Row>
        );
      },
    },
  ];

  return (
    <>
      <>
        <BoslerInput
          placeholder={getLanguageLabel("searchSources")}
          allowClear
          onChange={(e) => {
            setFilteredData(GlobalSearch(e.target.value, tableList, columns));
          }}
          suffix={<SearchIcon />}
        />

        <Table
          loading={loading}
          style={{ width: "100%", margin: "auto" }}
          columns={columns}
          dataSource={FilteredData !== undefined ? FilteredData : tableList}
          pagination={false}
        />
      </>

      <DeleteModal
        deleteServiceDetails={deleteServiceDetails}
        setDeleteServiceDetails={setDeleteServiceDetails}
        handleDelete={deleteSourceHandler}
      />
    </>
  );
};

export default SourceTable2;
