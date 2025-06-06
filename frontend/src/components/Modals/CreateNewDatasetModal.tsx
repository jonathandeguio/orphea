import { Form, Typography } from "antd";
import React from "react";

import { useDispatch } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

import { ResourceSubTypeEnum } from "Apps/explorer/explorer.utils";
import { TableIcon } from "assets/icons/boslerTableIcons";
import axios from "axios";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import { ErrorResponse } from "global";
import { InfoIcon } from "../../assets/icons/boslerMiscellaneousIcons";
import { TickIcon } from "../../assets/icons/boslerNavigationIcon";
import { createDataset } from "../../redux/actions/datasetActions";
import { listFolderDetails } from "../../redux/actions/projectActions";
import { addNewResource } from "../../redux/fileIndexSlice";
import { ThunkAppDispatch } from "../../redux/types/store";
import BoslerInput from "../BoslerComponents/InputComponent/BoslerInput";

const { Text, Title } = Typography;

export default ({ id, isVisible, setIsVisible }: any) => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const [form] = Form.useForm();

  const onCreateNew = (child: any) => {
    dispatch(addNewResource(child));
  };

  const handleOk = (name: string, description: string) => {
    if (name !== "") {
      dispatch(
        createDataset({
          name: name,
          description: description,
          parent: id,
          source: ResourceSubTypeEnum.NONE,
        })
      )
        .then((data: $TSFixMe) => {
          setIsVisible(false);
          onCreateNew(data);

          if (data.id) {
            dispatch(listFolderDetails(id));
          } else {
            openNotification(
              "Can't create dataset",
              "Something went wrong",
              "error"
            );
          }
        })
        .catch((err: $TSFixMe) => {
          if (axios.isAxiosError(err) && isDefined(err.response)) {
            const data = err?.response?.data as ErrorResponse;
            const error = data.error;
            const description = data.description;

            openNotification(error, description, "error");
          }
        });
    }
  };

  return (
    <Form
      form={form}
      initialValues={{ groups: true, folders: true }}
      onKeyPress={(e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          form.submit();
        }
      }}
      onFinish={(values) => {
        handleOk(values.name, values.description);
      }}
    >
      <BoslerModal
        heading={getLanguageLabel("dataset")}
        headingIcon={<TableIcon />}
        open={isVisible}
        onCancel={() => setIsVisible(false)}
        footerExtraText={getLanguageLabel("accessMessage")}
        footerButtonArea={
          <BoslerButton
            intent="primary"
            onClick={() => form.submit()}
            icon={<TickIcon />}
          >
            {getLanguageLabel("create")}
          </BoslerButton>
        }
        information={
          <div style={{ padding: "15px", width: "200px" }}>
            <div className="text-and-icon-align">
              <InfoIcon />
              <Text strong>Info</Text>
            </div>
            <div style={{ paddingTop: "2px", paddingLeft: "20px" }}>
              <Text style={{ fontSize: "0.8rem" }}>
                {getLanguageLabel("datasetDescription")}
              </Text>
            </div>
          </div>
        }
      >
        <div className="BoslerHeader1">{getLanguageLabel("name")}</div>
        <Form.Item
          name="name"
          rules={[
            {
              required: true,
            },
          ]}
        >
          <BoslerInput
            autofocus
            placeholder={getLanguageLabel("dataset")}
            style={{ width: "20vw", minWidth: "300px" }}
          />
        </Form.Item>
        <div className="BoslerHeader1">{getLanguageLabel("description")}</div>
        <Form.Item name="description">
          <BoslerInput
            placeholder={getLanguageLabel("descriptionOpt")}
            style={{ width: "20vw", minWidth: "300px" }}
          />
        </Form.Item>
      </BoslerModal>
    </Form>
  );
};
