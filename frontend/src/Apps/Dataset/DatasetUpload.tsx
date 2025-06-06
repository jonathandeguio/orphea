import { ResourceSubTypeEnum } from "Apps/explorer/explorer.utils";
import { Card, Col, Divider, Progress, Radio, Row, Typography } from "antd";
import { CrossIcon, PublishIcon } from "assets/icons/boslerActionIcons";
import { DocsIcon } from "assets/icons/boslerFileIcons";
import { UploadIcon } from "assets/icons/boslerInterfaceIcons";
import { TableIcon } from "assets/icons/boslerTableIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import CsvPreprocessing from "components/CsvPreprocessing";
import { getInitialValues } from "components/CsvPreprocessing/CsvPreprocessing.constants";
import { ICsvPreprocessing } from "components/CsvPreprocessing/CsvPreprocessing.types";
import BoslerLoader from "components/boslerLoader";
import React, { useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router";
import { autoFormatter } from "utils/AutoFormatter";
import {
  blobFileType,
  getLanguageLabel,
  openNotification,
} from "utils/utilities";
import * as XLSX from "xlsx";
import { importDataset } from "../../redux/actions/datasetActions";
import { ThunkAppDispatch } from "../../redux/types/store";
import { IDatasetDetails } from "./DatasetDetail";
import DatasetHeader from "./DatasetHeader.view";
const { Title, Text } = Typography;
interface TProps {
  id: string;
  branch: string;
  datasetDetails?: IDatasetDetails;
}
const DatasetUpload = ({ id, branch, datasetDetails }: TProps) => {
  const navigate = useNavigate();
  const { config } = useSelector((state) => (state as any).platformConfig);

  const [selectedFile, setSelectedFile] = useState([]);
  const [isSelected, setIsSelected] = useState(false);
  const [uploading, setUploading] = useState(0);
  const [readingFile, setReadingFile] = useState(false);
  const [uploadClicked, setUploadClicked] = useState(false);
  const [sheetNames, setSheetNames] = useState([]);
  const [selectedSheet, setSelectedSheet] = useState("");
  const [csvProcessingData, setCsvProcessingData] = useState<ICsvPreprocessing>(
    getInitialValues()
  );

  const isFileReadyToUpload = isSelected && !readingFile;

  const showSheetSelector = isSelected && sheetNames.length > 0 && !readingFile;

  const dispatch = useDispatch<ThunkAppDispatch>();
  const acceptedFileTypes = {
    "text/csv": [".csv"],
    "application/vnd.ms-excel": [
      ".xls",
      ".xlt",
      ".xla",
      ".xlam",
      ".xll",
      ".xlw",
    ],
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": [
      ".xlsx",
      ".xlsm",
      ".xlsb",
      ".xltx",
      ".xltm",
    ],
    "text/tab-separated-values": [".tsv"],
  };

  const onDrop = useCallback((acceptedFiles: any) => {
    if (acceptedFiles.length == 0) {
      openNotification("File rejected", "", "error");
      return;
    }
    const file = acceptedFiles[0];
    const reader = new FileReader();
    setReadingFile(true);

    reader.onload = (e) => {
      // Do whatever you want with the file contents
      const data = e.target?.result;
      if (blobFileType(file.name, ResourceSubTypeEnum.XLS)) {
        const workbook = XLSX.read(data, { type: "binary" });

        const sheetNames = workbook.SheetNames;
        setSheetNames(sheetNames as any);
        setSelectedSheet(sheetNames[0] as string);
      } else {
        setSheetNames([]);
        setSelectedSheet("");
      }
      setReadingFile(false);
    };
    setIsSelected(true);
    setSelectedFile(file);

    reader.readAsArrayBuffer(file);
  }, []);

  const { getRootProps, getInputProps, isFocused, isDragAccept, isDragReject } =
    useDropzone({
      onDrop,
      accept: acceptedFileTypes,
    });

  const uploadAPI = async () => {
    setUploadClicked(true);
    // message.loading(`Please wait uploading.`);
    navigate(-1);

    dispatch(
      importDataset(
        selectedFile,
        id,
        branch,
        setUploading,
        selectedSheet,
        JSON.stringify(csvProcessingData)
      )
    ).then((data: $TSFixMe) => {
      //   dispatch(checkTransaction(id!, branch!)).finally(() => {
      setIsSelected(false);
      setUploadClicked(false);
      setSelectedFile([]);
      setSelectedSheet("");
      //   });
    });
  };

  if (!config.upload)
    return (
      <div className="dataset-upload-out">
        <Row align={"middle"} justify="center" style={{ padding: "1.5rem 0" }}>
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
      </div>
    );

  return (
    <div className="dataset-splitpane">
      {datasetDetails && (
        <DatasetHeader
          id={id}
          branch={branch}
          datasetDetails={datasetDetails}
        />
      )}

      <div className="dataset-upload-out">
        {!isSelected ? (
          <>
            <div className="dataset-upload-out-head">
              {getLanguageLabel("noDataFoundPleaseUploadAFile")}
            </div>
            <div
              {...getRootProps({ className: "dropzone" })}
              className="dataset-upload"
              style={{ cursor: "pointer" }}
            >
              <input
                {...(getInputProps() as any)}
                className="dataset-upload-btn"
              />
              {isDragReject && <p>Files will be rejected</p>}
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
              {/* </p> */}
              <br />
              By uploading files, you agree to our Terms and Conditions and
              Privacy Policy.
            </div>
          </>
        ) : (
          <div className="dataset-upload">
            <div>
              <>
                <Card
                  title={
                    <>
                      <div className="text-and-icon-center">
                        <DocsIcon /> {getLanguageLabel("files")}{" "}
                      </div>
                    </>
                  }
                  bordered={false}
                  // className="scaled-interactive card"
                  style={{ textAlign: "left" }}
                >
                  <Row gutter={16} style={{ textAlign: "left" }}>
                    <Col className="gutter-row" span={6}>
                      {" "}
                      {getLanguageLabel("fileName")}
                    </Col>
                    <Col className="gutter-row" span={6}>
                      {(selectedFile as $TSFixMe).name}
                    </Col>
                  </Row>
                  <Row gutter={16} style={{ textAlign: "left" }}>
                    <Col className="gutter-row" span={6}>
                      {" "}
                      {getLanguageLabel("fileType")}
                    </Col>
                    <Col className="gutter-row" span={16}>
                      {(selectedFile as $TSFixMe).type}
                    </Col>
                  </Row>
                  <Row gutter={16} style={{ textAlign: "left" }}>
                    <Col className="gutter-row" span={6}>
                      {" "}
                      {getLanguageLabel("size")}
                    </Col>
                    <Col className="gutter-row" span={6}>
                      {autoFormatter((selectedFile as $TSFixMe).size, "bytes")}
                    </Col>
                  </Row>
                </Card>
                <br />

                {showSheetSelector && (
                  <>
                    <Card
                      title={
                        <>
                          <TableIcon /> {getLanguageLabel("selectSheet")}{" "}
                        </>
                      }
                      bordered={false}
                      // className="scaled-interactive card"
                      style={{ textAlign: "left" }}
                    >
                      <Radio.Group
                        name="Sheet"
                        onChange={(e) => {
                          setSelectedSheet(e.target.value);
                        }}
                        value={selectedSheet}
                        size="small"
                      >
                        {sheetNames.map((sheet) => {
                          return (
                            <Radio name="sheet" value={sheet}>
                              {sheet}
                            </Radio>
                          );
                        })}
                      </Radio.Group>
                    </Card>
                  </>
                )}
                {selectedFile &&
                  !Array.isArray(selectedFile) &&
                  (selectedFile as File).type == "text/csv" && (
                    <CsvPreprocessing
                      onValuesChange={(
                        values: ICsvPreprocessing,
                        allValues: ICsvPreprocessing
                      ) => {
                        setCsvProcessingData(allValues);
                      }}
                    />
                  )}

                {readingFile && (
                  <Card bordered={false}>
                    <Row justify={"center"} align={"middle"} gutter={[16, 16]}>
                      <Col>
                        <BoslerLoader />
                      </Col>
                      <Col>Your file is getting ready...</Col>
                    </Row>
                  </Card>
                )}
                <br />
              </>

              {uploadClicked && (
                <Card bordered={false}>
                  {uploading < 100 ? (
                    <>
                      <Title>
                        <div className="text-and-icon-center">
                          <PublishIcon size={40} />
                          {getLanguageLabel("uploadWait")}
                        </div>
                        <Divider />
                        <Progress percent={uploading} />
                        <Text type="secondary">
                          {getLanguageLabel("uploadWaitDetail")}
                        </Text>
                      </Title>
                    </>
                  ) : (
                    <>
                      <Title>
                        <div className="text-and-icon-center">
                          <BoslerLoader size="small" />
                          &nbsp;{" "}
                          <Text type="secondary">
                            {getLanguageLabel("processing")}
                          </Text>
                        </div>
                      </Title>
                      <Text type="secondary">
                        {getLanguageLabel("creatingDatasetDetail")}
                      </Text>
                    </>
                  )}
                </Card>
              )}
            </div>
          </div>
        )}

        {isFileReadyToUpload && (
          <>
            <br />
            <Row justify={"center"} align={"middle"} gutter={[16, 16]}>
              <Col>
                <BoslerButton
                  disabled={uploadClicked}
                  size={"large"}
                  icon={<CrossIcon />}
                  onClick={() => {
                    setIsSelected(false);
                  }}
                  intent="dangerous"
                >
                  {getLanguageLabel("cancel")}
                </BoslerButton>
              </Col>
              <Col>
                <BoslerButton
                  disabled={uploadClicked}
                  icon={<UploadIcon />}
                  size={"large"}
                  onClick={uploadAPI}
                  intent="primary"
                >
                  {getLanguageLabel("upload")}
                </BoslerButton>
              </Col>
            </Row>
          </>
        )}
      </div>
    </div>
  );
};

export default DatasetUpload;
