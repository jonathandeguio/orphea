import {
  Card,
  Col,
  Form,
  notification,
  Progress,
  Radio,
  Row,
  Tooltip,
  Typography,
} from "antd";
import React, { useCallback, useState } from "react";

import { useDispatch, useSelector } from "react-redux";
import {
  blobFileType,
  formatDuration,
  getLanguageLabel,
  isDefined,
  openNotification,
} from "utils/utilities";
import * as XLSX from "xlsx";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

import { ResourceSubTypeEnum } from "Apps/explorer/explorer.utils";
import { EditIcon } from "assets/icons/boslerEditorIcons";
import { DocsIcon, FolderIcon } from "assets/icons/boslerFileIcons";
import { HelpIcon } from "assets/icons/boslerMiscellaneousIcons";
import { TickIcon } from "assets/icons/boslerNavigationIcon";
import { TableIcon } from "assets/icons/boslerTableIcons";
import axios from "axios";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerLoader from "components/boslerLoader";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import { useDropzone } from "react-dropzone";
import { useNavigate } from "react-router";
import { autoFormatter } from "utils/AutoFormatter";
import { CrossIcon } from "../../assets/icons/boslerActionIcons";
import {
  ChangeLogIcon,
  UploadIcon,
} from "../../assets/icons/boslerInterfaceIcons";
import { listFolderDetails } from "../../redux/actions/projectActions";
import { addNewResource } from "../../redux/fileIndexSlice";
import { ThunkAppDispatch } from "../../redux/types/store";

const { Text, Title } = Typography;

export default ({ id, isVisible, setIsVisible }: any) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const navigate = useNavigate();
  const [uploadedFile, setUploadedFile] = useState<Blob>();
  const [uploadLoading, setUploadLoading] = useState(false);

  const [uploading, setUploading] = useState(0);
  const [sheetNames, setSheetNames] = useState([]);

  const [form] = Form.useForm();

  const { config } = useSelector((state) => (state as any).platformConfig);

  const onCreateNew = (child: any) => {
    dispatch(addNewResource(child));
  };

  const onDrop = useCallback((acceptedFiles: any) => {
    const file = acceptedFiles[0];
    const reader = new FileReader();

    reader.onload = (e) => {
      // Do whatever you want with the file contents
      const data = e.target?.result;
      if (blobFileType(file.name, ResourceSubTypeEnum.XLS)) {
        const workbook = XLSX.read(data, { type: "binary", bookSheets: true });
        const sheetNames = workbook.SheetNames;
        setSheetNames(sheetNames as any);
        form.setFieldValue("selectedSheet", sheetNames[0] as string);
      } else {
        setSheetNames([]);
        form.setFieldValue("selectedSheet", sheetNames[0] as string);
      }
    };
    form.setFieldsValue({
      fileName: file.name.split(".")[0],
      selectedSheet: "first",
    });

    setUploadedFile(file);

    reader.readAsArrayBuffer(file);
  }, []);

  const { getRootProps, getInputProps } = useDropzone({
    onDrop,
    // accept: { "*": [] },
  });

  const handleOk = async () => {
    console.log(form.getFieldsValue());
    if (
      (form.getFieldValue("selectedSheet") != null ||
        form.getFieldValue("selectedSheet") != undefined) &&
      form.getFieldValue("selectedSheet").length == 0
    ) {
      openNotification("Upload failed", "Select atleast one sheet", "error");
      return;
    }
    const uploadNotificationKey = "upload" + Date.now();
    try {
      setUploadLoading(true);
      const formData = new FormData();

      formData.append("file", uploadedFile as Blob);

      const params = new URLSearchParams({
        sheetName: form.getFieldValue("selectedSheet"),
        fileName: form.getFieldValue("fileName"),
        description:
          form.getFieldValue("description") != ""
            ? form.getFieldValue("description")
            : getLanguageLabel("uploaded"),
      }).toString();

      const isDataset =
        blobFileType((uploadedFile as File).name, ResourceSubTypeEnum.XLS) ||
        blobFileType((uploadedFile as File).name, ResourceSubTypeEnum.CSV) ||
        blobFileType((uploadedFile as File).name, ResourceSubTypeEnum.PARQUET);

      const url = `/kitab/folder/uploadFile/${id}?` + params;

      const { data } = await axios.post(url, formData, {
        onUploadProgress: function (AxiosProgressEvent: any) {
          const percentCompleted = Math.round(
            (AxiosProgressEvent.loaded / AxiosProgressEvent.total) * 100
          );

          const estimatedTime = formatDuration(
            Math.round(AxiosProgressEvent.estimated) * 1000
          );

          setUploading(percentCompleted);
          notification.destroy(uploadNotificationKey);
          notification.info({
            key: uploadNotificationKey,
            message: (
              <>
                {percentCompleted == 100 && isDataset ? (
                  <>
                    <Row justify={"space-between"}>
                      <Col>{getLanguageLabel("fileUploadCompleted")}</Col>
                    </Row>
                    <Row justify={"space-between"}>
                      <Col span={4}>
                        <div className="text-and-icon-center">
                          <BoslerLoader size="small" />
                        </div>
                      </Col>
                      <Col span={20}>
                        <div className="text-and-icon-center">
                          {getLanguageLabel("processing")}
                        </div>
                      </Col>
                    </Row>
                  </>
                ) : (
                  <>
                    <Row>
                      <Col span={17}>
                        {getLanguageLabel("fileUploadInProgress")}
                      </Col>
                      <Col span={7}>{estimatedTime}</Col>
                    </Row>
                  </>
                )}
              </>
            ),
            icon: (
              <>
                {percentCompleted == 100 ? (
                  <div className="success-tick-circle text-and-icon-center">
                    <TickIcon color="#ffffff" />
                  </div>
                ) : (
                  <BoslerLoader size="small" />
                )}
              </>
            ),
            closeIcon: <>{percentCompleted == 100 && <CrossIcon />}</>,
            description: (
              <>
                <Row>
                  <Col span={22}>
                    <div className="text-and-icon-center">
                      {isDataset ? (
                        <>
                          <TableIcon />
                          {form.getFieldValue("fileName")}
                        </>
                      ) : (
                        <>
                          <DocsIcon />
                          {form.getFieldValue("fileName")}
                        </>
                      )}
                    </div>
                  </Col>
                  <Col span={2}>
                    <Progress
                      type="circle"
                      size={30}
                      percent={percentCompleted}
                    />
                  </Col>
                </Row>
              </>
            ),
            duration: 0,
            placement: "bottomRight",
          });
        },
        headers: {
          "Content-type": "multipart/form-data",
        },
      });

      notification.destroy(uploadNotificationKey);
      notification.info({
        key: uploadNotificationKey,
        message: (
          <>
            <Row justify={"space-between"}>
              <Col>{getLanguageLabel("fileUploadCompleted")}</Col>
              <Col>
                <a href={`/portal/kitab/folder/${id}`}>
                  <BoslerButton minimal icon={<FolderIcon />} />
                </a>
              </Col>
            </Row>
          </>
        ),
        icon: (
          <>
            <div className="success-tick-circle text-and-icon-center">
              <TickIcon color="#ffffff" />
            </div>
          </>
        ),
        closeIcon: <>{<CrossIcon />}</>,
        description: (
          <>
            <Row>
              <Col span={22}>
                <div className="text-and-icon-center">
                  {isDataset ? <TableIcon /> : <DocsIcon />}
                  <a
                    href={
                      isDataset
                        ? `/portal/kitab/dataset/${data.id}/master`
                        : `/portal/blob/${data.id}`
                    }
                  >
                    <Tooltip title={data.name}>
                      <div className="clipText10">{data.name}</div>
                    </Tooltip>
                  </a>
                </div>
              </Col>
              <Col span={2}>
                <Progress type="circle" size={30} percent={100} />
              </Col>
            </Row>
          </>
        ),
        duration: 0,
        placement: "bottomRight",
      });

      onCreateNew(data);

      setUploadLoading(false);

      return data;
    } catch (error: any) {
      notification.destroy(uploadNotificationKey);

      if (error.response) {
        openNotification(
          error.response.data,
          error.response.description,
          "error"
        );
      } else {
        openNotification("Request Failed", "Unable to upload file", "error");
      }
    }
  };

  const buttonArea = () => {
    if (uploadedFile) {
      return (
        <>
          <BoslerButton
            icon={<ChangeLogIcon />}
            intent="dangerous"
            key="submit"
            onClick={() => {
              setUploadedFile(undefined);
            }}
            disabled={uploadLoading}
          >
            {getLanguageLabel("replace")}
          </BoslerButton>
          <BoslerButton
            icon={<UploadIcon />}
            intent={uploadLoading ? "primary" : "success"}
            key="submit"
            onClick={() => {
              handleOk().then(() => {
                dispatch(listFolderDetails(id));
                setUploadedFile(undefined);
              });
              setIsVisible(false);
            }}
          >
            {getLanguageLabel("upload")}
          </BoslerButton>
        </>
      );
    }

    return <></>;
  };
  const FOOTER_TEXT = uploadLoading
    ? getLanguageLabel("creatingDatasetDetail")
    : "By uploading files, you agree to our Terms and Conditions and Privacy Policy.";
  const BUTTON_AREA = buttonArea();

  return (
    <BoslerModal
      headingIcon={<UploadIcon />}
      heading={getLanguageLabel("upload") + getLanguageLabel("newFile")}
      open={isVisible}
      width={750}
      onCancel={() => setIsVisible(false)}
      footerButtonArea={BUTTON_AREA}
      footerExtraText={FOOTER_TEXT}
    >
      {config.upload ? (
        <div>
          {isDefined(uploadedFile) ? (
            <>
              <Card title="File Details">
                <Form form={form}>
                  <Row gutter={16}>
                    <Col span={8}>Name</Col>
                    <Col span={16}>
                      <Form.Item name="fileName">
                        <BoslerInput
                          placeholder={getLanguageLabel("fileName")}
                          suffix={<EditIcon />}
                        />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Row>
                    <Col span={8}>
                      <Text strong>Desc</Text>
                    </Col>
                    <Col span={16}>
                      <Form.Item name="description">
                        <BoslerInput
                          value={getLanguageLabel("uploaded")}
                          placeholder={getLanguageLabel("uploaded")}
                          disabled={uploadLoading}
                          suffix={<EditIcon />}
                        />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Row>
                    <Col span={8}>Type</Col>
                    <Col span={16}>
                      <Form.Item>{(uploadedFile as File).type}</Form.Item>
                    </Col>
                  </Row>
                  <Row>
                    <Col span={8}> Size</Col>
                    <Col span={16}>
                      <Form.Item>
                        {autoFormatter(uploadedFile.size, "bytes")}
                      </Form.Item>
                    </Col>
                  </Row>
                  {sheetNames.length >= 1 && (
                    <Row>
                      <Col span={8}>
                        <Text strong>Sheet</Text>
                      </Col>
                      <Col span={16}>
                        <Form.Item name="selectedSheet">
                          <Radio.Group size="small">
                            {sheetNames.map((sheet) => {
                              return (
                                <Radio name="sheet" value={sheet}>
                                  {sheet}
                                </Radio>
                              );
                            })}
                          </Radio.Group>
                        </Form.Item>
                      </Col>
                    </Row>
                  )}
                </Form>
              </Card>
            </>
          ) : (
            <div
              {...getRootProps({
                className: "dropzone",
              })}
              className="dataset-upload"
              style={{ cursor: "pointer" }}
            >
              <input
                {...(getInputProps() as any)}
                className="dataset-upload-btn"
              />
              {/* <Card
            title={
              <>
                <span
                  style={{
                    background: "#ff6600",
                    color: "white",
                    fontWeight: "",
                  }}
                >
                  <div className="text-and-icon-center">
                    <WarningIcon color={"white"} size={30} />
                    {getLanguageLabel("dataUploadSecurityWarning")}
                  </div>
                </span>
              </>
            }
            bordered={false}
            // className="scaled card"
            style={{
              textAlign: "left",
              // width: "72vw",
              background: "#ff6600",
              color: "white",
            }}
          >
            {getLanguageLabel("dataUploadSecurityWarningText")}
          </Card> */}
              {/* <br /> */}

              <Row
                style={{
                  height: "40vh",
                  alignContent: "center",
                  border: "1px dashed var(--bosler-border-color-default)",
                  alignItems: "center",
                  display: "flex",
                  flexDirection: "column",
                  justifyContent: "center",
                  position: "relative",
                  textAlign: "center",
                  borderRadius: "3px",
                  margin: "7px",
                }}
              >
                <Title level={5}>
                  {getLanguageLabel("dragFile")}{" "}
                  {getLanguageLabel("or").toLowerCase()}{" "}
                  <span
                    style={{
                      color: "var(--bosler-font-color-default)",
                    }}
                    className="link"
                  >
                    {getLanguageLabel("browseFiles")}{" "}
                  </span>
                </Title>
              </Row>
              <Text style={{ fontSize: "0.6rem" }} type="secondary">
                <div className="text-and-icon-center">
                  {getLanguageLabel("info")} <HelpIcon size={12} />
                </div>{" "}
                : {getLanguageLabel("fileUploadOptions")}
              </Text>
            </div>
          )}
        </div>
      ) : (
        <>
          <Row
            align={"middle"}
            justify="center"
            style={{ padding: "1.5rem 0" }}
          >
            <div className="lock" />
          </Row>

          <Row align={"middle"} justify="center">
            <Text strong style={{ fontSize: "1.5rem" }}>
              Upload Disabled
            </Text>
          </Row>
          <Row align={"middle"} justify="center">
            {" "}
            <Text style={{ fontSize: "1rem" }}>
              Please Contact the Platform Admin to enable the Uploads on Bosler.
            </Text>
          </Row>
        </>
      )}
    </BoslerModal>
  );
};
