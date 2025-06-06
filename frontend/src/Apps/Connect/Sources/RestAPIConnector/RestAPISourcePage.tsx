import { Typography } from "antd";
import NoData from "components/CommonUI/NoData";
import React from "react";
import { ISourceConfig } from "../Source";
import styles from "./RestAPIConnector.module.scss";
import { IRestDomain } from "./RestAPIConnector.types";
const { Text } = Typography;
interface IProps {
  source: ISourceConfig;
}

const RestAPISourcePage = ({ source }: IProps) => {
  if (!source.domains) {
    return <NoData />;
  }

  return (
    <div>
      <Text type="secondary" strong>
        {"Domains Available"}
      </Text>
      <div className={styles.domain_list + " --mt10"}>
        {source.domains.map((domain: IRestDomain) => (
          <div className={styles.domain_item}>
            <div className={styles.chip}>{domain.protocol}</div>
            <div>{domain.domain}</div>
            <div className={styles.chip}>{domain.authType}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default RestAPISourcePage;
