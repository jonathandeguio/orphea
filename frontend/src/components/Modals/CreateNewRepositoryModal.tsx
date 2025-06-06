import { Col, Row, Typography } from "antd";
import React, { useState } from "react";

import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

import { DisableIcon } from "assets/icons/boslerActionIcons";
import { CodeCellIcon } from "assets/icons/boslerEditorIcons";
import axios from "axios";
import { PREPARING } from "components/Builds/Builds.constants";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import { createRepositoryAPI } from "components/editor/editor.api";
import { FRACTAL_USE_CASES } from "components/editor/editor.constants";
import { ErrorResponse } from "global";
import { LicenseIncapableModal } from "pages/Settings/PlatformConfig/License/LicenseIncapableModal";
import { useNavigate } from "react-router";
import { BooleanIcon } from "../../assets/icons/boslerDataIcons";
import {
  PySparkIcon,
  SparkSQLIcon,
} from "../../assets/icons/boslerExternalIcons";
import { InfoIcon } from "../../assets/icons/boslerMiscellaneousIcons";
import { TickIcon } from "../../assets/icons/boslerNavigationIcon";
import { addNewResource } from "../../redux/fileIndexSlice";
import { ThunkAppDispatch } from "../../redux/types/store";
import BoslerInput from "../BoslerComponents/InputComponent/BoslerInput";

const { Text, Title } = Typography;

const RepoCard = ({
  templateLanguage,
  setTemplateLanguage,
  name,
  description,
  disabled,
}: any) => {
  return (
    <Col span={7}>
      <div
        className={
          disabled
            ? templateLanguage == name
              ? " selected-card"
              : " unselected-card"
            : "create-repo-cards".concat(
                templateLanguage == name ? " selected-card" : " unselected-card"
              )
        }
        onClick={(e) => setTemplateLanguage(name)}
        style={{
          cursor: disabled ? "not-allowed" : "pointer",
          padding: "10px",
          minHeight: "140px",
          maxHeight: "140px",
        }}
      >
        <Row>
          <Col span={20}>
            <div
              className="text-and-icon-center"
              // style={{ cursor: "not-allowed" }}
            >
              {name == "PYTHON" && <PySparkIcon />}{" "}
              {name == "SQL" && <SparkSQLIcon />}
              {name}
            </div>
          </Col>
          <Col span={4}>{name == "R" ? <DisableIcon /> : <BooleanIcon />}</Col>

          <Col>
            <Text type="secondary">
              <div
                style={{
                  maxWidth: "150px",
                  wordWrap: "break-word",
                }}
                className="icon-text"
              >
                {description}
              </div>
            </Text>
          </Col>
        </Row>
      </div>
    </Col>
  );
};

export default ({ id, isVisible, setIsVisible }: any) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const navigate = useNavigate();

  const [createLoading, setCreateLoading] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [templateLanguage, setTemplateLanguage] = useState("PYTHON");

  const { info } = useSelector((state) => (state as any).license);

  const onCreateNew = (child: any) => {
    dispatch(addNewResource(child));
  };

  const handleOk = () => {
    if (name !== "") {
      setCreateLoading(true);
      createRepositoryAPI(templateLanguage, {
        name: name,
        description: description,
        parent: id,
        type: "REPOSITORY",
      })
        .then(({ data }) => {
          onCreateNew(data);
          navigate(`/portal/kitab/repository/${data.id}/master`);
        })
        .catch((err) => {
          if (axios.isAxiosError(err) && isDefined(err.response)) {
            const data = err?.response?.data as ErrorResponse;
            const error = data.error;
            const description = data.description;

            openNotification(error, description, "error");
          }
        })
        .finally(() => {
          setIsVisible(false);
          setCreateLoading(false);
        });
    }
  };

  if (!FRACTAL_USE_CASES.includes(info.product))
    return (
      <LicenseIncapableModal
        type={"FRACTAL"}
        isOpen={isVisible}
        setIsOpen={setIsVisible}
      />
    );

  return (
    <BoslerModal
      open={isVisible}
      onOk={handleOk}
      onCancel={() => setIsVisible(false)}
      headingIcon={<CodeCellIcon />}
      heading={getLanguageLabel("repository")}
      footerExtraText={getLanguageLabel("accessMessage")}
      footerButtonArea={
        <BoslerButton
          intent={createLoading ? "none" : "primary"}
          onClick={handleOk}
          loading={createLoading}
          disabled={createLoading}
          icon={<TickIcon />}
        >
          {createLoading
            ? getLanguageLabel(PREPARING)
            : getLanguageLabel("create")}
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
              {getLanguageLabel("codeRepositoryMessage")}
            </Text>
          </div>
        </div>
      }
    >
      <div className="BoslerHeader1">{getLanguageLabel("name")}</div>
      <BoslerInput
        bordered
        autofocus
        onChange={(e) => {
          setName(e.target.value);
        }}
        value={name}
        type="name"
        required
        placeholder={getLanguageLabel("repository")}
      />
      <div className="BoslerHeader1">{getLanguageLabel("description")}</div>
      <BoslerInput
        onChange={(e) => setDescription(e.target.value)}
        placeholder={getLanguageLabel("descriptionOpt")}
      />

      <Title level={5}>{getLanguageLabel("language")}</Title>

      <Row>
        <RepoCard
          templateLanguage={templateLanguage}
          setTemplateLanguage={setTemplateLanguage}
          name={"PYTHON"}
          description={getLanguageLabel("PythonRepositoryCreationSubtitle")}
        />
        <Col span={1}></Col>
        <RepoCard
          templateLanguage={templateLanguage}
          setTemplateLanguage={setTemplateLanguage}
          name={"SQL"}
          description={getLanguageLabel("SQLRepositoryCreationSubtitle")}
        />
        <Col span={1}></Col>
        <RepoCard
          templateLanguage={templateLanguage}
          setTemplateLanguage={setTemplateLanguage}
          name={"R"}
          description={getLanguageLabel("notAvailable")}
          disabled={true}
        />
      </Row>
    </BoslerModal>
  );
};
