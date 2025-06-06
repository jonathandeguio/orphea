import { Col, Row, Tooltip, Typography } from "antd";
import { ArrowLeftIcon } from "assets/icons/boslerNavigationIcon";
import { CopyCellIcon } from "assets/icons/boslerTableIcons";
import Avatars from "components/Avatars/Avatars";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React from "react";
import { useNavigate } from "react-router";
import { copyToClipboard, getLanguageLabel } from "utils/utilities";
import styles from "../AccessRequest.module.scss";

const { Title } = Typography;

interface IProps {
  id: string;
}

export const AccessRequestHeader = ({ id }: IProps) => {
  const navigate = useNavigate();
  return (
    <div className={styles.header}>
      <Title style={{ margin: "0px" }} level={3}>
        <div onClick={() => navigate(-1)} style={{ cursor: "pointer" }}>
          <div className="text-and-icon-center">
            <ArrowLeftIcon size={30} />{" "}
            {getLanguageLabel("accessManager").toUpperCase()}
          </div>
        </div>
      </Title>

      <Row gutter={16}>
        <Col>
          <Avatars link={`/topic/${id}`} />
        </Col>
        <Col>
          <Tooltip title={getLanguageLabel("clickToCopyIntoClipboard")}>
            <BoslerButton
              onClick={() => copyToClipboard(window.location.href)}
              icon={<CopyCellIcon />}
            >
              {getLanguageLabel("copy")} URL
            </BoslerButton>
          </Tooltip>
        </Col>
      </Row>
    </div>
  );
};
