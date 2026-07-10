import {
  Card,
  Col,
  Divider,
  Row,
  Switch,
  Table,
  Tabs,
  TabsProps,
  Typography
} from "antd";
import axios from "axios";
import MoveToDataLoader from "components/movetodataLoader";
import UserPopOver from "components/UserPopover/userpopover";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { getLanguageLabel, isDefined, timeConverter } from "utils/utilities";
import { updatePlatformConfig } from "redux/actions/platformSettingsActions";
import { ThunkAppDispatch } from "redux/types/store";
import { getUploadLogsAPI } from "./PlatformConfig.api";

const { Text, Title } = Typography;
export const UploadSettings = () => {
  const [uploadLogs, setUploadLogs] = useState();
  const [uploadDatasetMap, setUploadDatasetMap] = useState(new Map());
  const [uploadedByUserMap, setUploadedByUserMap] = useState();

  const dispatch = useDispatch<ThunkAppDispatch>();

  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  const uploadColumns = [
    {
      title: getLanguageLabel("datasetName"),
      dataIndex: "datasetId",
      key: "datasetId",
      width: "30%",
      render: (text: any, record: any) => (
        <>
          <Link to={(uploadDatasetMap as any).get(text)?.link}>
            <span className="pop-over-item">
              {(uploadDatasetMap as any).get(text)?.name}
            </span>
          </Link>
        </>
      ),
    },
    {
      title: `${getLanguageLabel("uploaded")} ${getLanguageLabel("by")}`,
      dataIndex: "uploadedBy",
      key: "uploadedBy",
      render: (text: any, record: any) => (
        <>
          <UserPopOver record={(uploadedByUserMap as any)[text]}>
            <Link to={`/portal/settings/user/${text}`}>
              <span className="pop-over-item">
                {(uploadedByUserMap as any)[text]?.name}
              </span>
            </Link>
          </UserPopOver>
        </>
      ),
    },

    {
      title: `${getLanguageLabel("uploaded")} ${getLanguageLabel("at")}`,
      dataIndex: "uploadedAt",
      key: "uploadedAt",
      render: (text: any, record: any) => <>{timeConverter(text)}</>,
    },
  ];

  const uploadItems: TabsProps["items"] = [
    {
      key: "1",
      label: getLanguageLabel("settings"),
      children: (
        <>
          <br />
          <Row gutter={16}>
            <Col span={6}>
              <Text type="secondary">Allow Uploads:</Text>
            </Col>

            <Col>
              <Switch
                checkedChildren="Yes"
                unCheckedChildren="No"
                loading={loading}
                defaultChecked={isDefined(config) ? config.upload : false}
                onChange={(checked) => {
                  dispatch(
                    updatePlatformConfig({ ...config, upload: checked })
                  );
                }}
              />
            </Col>
          </Row>
          <br />
        </>
      ),
    },
    {
      key: "2",
      label: `Log`,
      children: (
        <>
          <Row>
            <Col span={24}>
              <Card className="card">
                <Title level={3}>Last Uploads</Title>
                <Text type="secondary">
                  This is the list of uploads on the platform.
                </Text>
                <Divider />

                <Table
                  loading={!uploadLogs}
                  dataSource={uploadLogs}
                  columns={uploadColumns}
                  pagination={{ pageSize: 5, hideOnSinglePage: true }}
                  className="interactive"
                />
              </Card>
            </Col>
          </Row>
        </>
      ),
    },
  ];
  return (
    <div className="settings-center-block">
      {loading ? (
        <MoveToDataLoader />
      ) : (
        <>
          <Row>
            <Col>
              <Title level={3}>Upload Settings</Title>
              <Text type="secondary">
                This space is designated for platform administrators and
                developers for upload Settings.
              </Text>
            </Col>
          </Row>
          <Divider />
          <Tabs
            defaultActiveKey="1"
            items={uploadItems}
            onChange={(key) => {
              if (key == "2" && !uploadLogs)
                getUploadLogsAPI().then(async ({ data }: any) => {
                  const user_list: any[] = [];
                  for (let i = 0; i < data.length; i++) {
                    user_list.push(data[i].uploadedBy);
                  }

                  const uniqueUSERIDList: string[] = user_list.filter(
                    (item, index) => user_list.indexOf(item) === index
                  );

                  const { data: user_details_dict } = await axios.post(
                    `/passport/users/byIds`,
                    uniqueUSERIDList
                  );

                  const datasetMap = new Map();
                  await Promise.all(
                    data.map(async (ele: any) => {
                      try {
                        const { data: datasetDetails } = await axios.get(
                          `/kitab/${ele.datasetId}`
                        );
                        const temp = {
                          name: datasetDetails.name,
                          link: `/portal/kitab/dataset/${datasetDetails.id}/master`,
                        };
                        datasetMap.set(ele.datasetId, temp);
                      } catch (error) {
                        datasetMap.set(ele.datasetId, {
                          name: "unknown",
                          link: `/portal/settings/platform`,
                        });
                      }
                    })
                  ).then(() => {
                    setUploadDatasetMap(datasetMap);
                    setUploadedByUserMap(user_details_dict);

                    setUploadLogs(data);
                  });
                });
            }}
          />
        </>
      )}
    </div>
  );
};
