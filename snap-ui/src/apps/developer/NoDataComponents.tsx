import { Typography } from "antd";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import { SyncIcon } from "assets/icons/movetodataActionIcons";
import React from "react";
import NoData from "components/NoData";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";

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
          <MoveToDataButton icon={<SyncIcon spin={false} />} borderless>
            Reload
          </MoveToDataButton>
        }
      />
    </>
  );
};

export default NoDataComponents;
