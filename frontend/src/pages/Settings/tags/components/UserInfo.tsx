import { Col, Divider, Popover, Row, Typography } from "antd";
import React from "react";
import { getLanguageLabel, getTimeDisplay, timeConverter } from "utils/utilities";
const { Title} = Typography;
import styles from "../CreateTags.module.scss";
import { InfoIcon } from "assets/icons/boslerMiscellaneousIcons";

interface IUserInfoProps {
  categoryDetails: any;
}
const UserInfo: React.FC<IUserInfoProps> = ({ categoryDetails }) => {
  return (
    <Popover
      placement="topRight"
      content={
        <div className={`${styles.infoContainer} --width100`}>
            <Row style={{ fontSize: "16px"}}>Info</Row>
            <Divider className={styles.tableDivider}/>
          <Row className={styles.detailRow}>
            <Col span={12}>
              <span className={styles.categoryLabels}>
                {getLanguageLabel("createdBy")}
              </span>
            </Col>
            <Col span={12}>
              <span>{categoryDetails?.createdBy}</span>
            </Col>
          </Row>
          <br />
          <Row className={styles.detailRow}>
            <Col span={12}>
              <span className={styles.categoryLabels}>
                {getLanguageLabel("createdAt")}
              </span>
            </Col>
            <Col span={12}>
              <span>
                {categoryDetails?.createdAt
                  ? timeConverter(Number(categoryDetails.createdAt))
                  : "No Date Available"}
              </span>
            </Col>
          </Row>
          <br />
          <Row className={styles.detailRow}>
            <Col span={12}>
              <span className={styles.categoryLabels}>
                {getLanguageLabel("updatedBy")}
              </span>
            </Col>
            <Col span={12}>
              <span>{categoryDetails?.updatedBy}</span>
            </Col>
          </Row>
          <br />
          <Row className={styles.detailRow}>
            <Col span={12}>
              <span className={styles.categoryLabels}>
                {getLanguageLabel("updatedAt")}
              </span>
            </Col>
            <Col span={12}>
              <span>{categoryDetails?.updatedAt}</span>
            </Col>
          </Row>
        </div>
      }
    >
      <InfoIcon />
    </Popover>
  );
};

export default UserInfo;
