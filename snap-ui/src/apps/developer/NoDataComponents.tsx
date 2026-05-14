import { Typography } from "antd";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import { SyncIcon } from "assets/icons/orpheaActionIcons";
import React from "react";
import NoData from "components/NoData";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";

const { Title } = Typography;
const NoDataComponents = () => {
  return (
    <>
      <Title>Empty State</Title>
      <NoData
        heading="No chart present"
        subHeading="Add charts from the section on right."
        icon={<SearchEmptyState size={"90px"} />}
        actionArea={
          <OrpheaButton icon={<SyncIcon spin={false} />} borderless>
            Reload
          </OrpheaButton>
        }
      />
    </>
  );
};

export default NoDataComponents;
