import { Col, Row, Typography } from "antd";
import React from "react";
const {Text } = Typography;
import styles from "../CreateTags.module.scss";
import { getLanguageLabel } from "utils/utilities";

const TagsTableHeader : React.FC =() => {
  return (
    <Row>
      <Col style={{width:"25%"}}>
        <Text className={styles.tagTableHeaderFont}>{getLanguageLabel("name")}</Text>
      </Col>
      <Col style={{width:"40%"}}>
        <Text className={styles.tagTableHeaderFont}>{getLanguageLabel("description")}</Text>
      </Col>
      <Col style={{width:"25%"}}>
        <Text className={styles.tagTableHeaderFont}>{getLanguageLabel("color")}</Text>
      </Col>
      <Col style={{width:"7%",textAlign:"right"}}>
        <Text className={styles.tagTableHeaderFont}>{getLanguageLabel("actions")}</Text>
      </Col>
    </Row>
  );
};

export default TagsTableHeader;