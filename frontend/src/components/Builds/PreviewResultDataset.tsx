import BoslerTable from "Apps/Dataset/Table/BoslerTable";
import {
  SearchEmptyState,
  WarningState,
} from "assets/Illustrations/EmptyState";
import NoData from "components/CommonUI/NoData";
import BoslerLoader from "components/boslerLoader";
import React from "react";

interface IProps {
  data: any;
  isLoading?: boolean;
  error?: string | undefined;
}

const PreviewResultDataset = ({
  data,
  isLoading = false,
  error = undefined,
}: IProps) => {
  let new_columns: any = [];
  if (data) {
    data?.schema?.map((column: any) => {
      let new_column = {
        accessorKey: "",
        header: "",
        id: "",
        cell: (info: any) => info.getValue(),
        footer: (props: any) => props.column.id,
        size: 200,
        type: "",
      };
      new_column.accessorKey = column.headerName;
      new_column.id = column.headerName;
      new_column.header = column.headerName;
      new_column.type = column.type;

      new_columns.push(new_column);
    });
  }

  if (isLoading) {
    return <BoslerLoader />;
  }

  if (error) {
    return (
      <NoData heading={"Error"} subHeading={error} icon={<WarningState />} />
    );
  }

  if (!data) {
    return (
      <NoData
        heading={"No data"}
        subHeading={"No result found the process"}
        icon={<SearchEmptyState />}
      />
    );
  }
  return (
    <>
      {/* <Alert message={getLanguageLabel("limitedRowsDisplay")} type="warning" /> */}

      <BoslerTable
        isTableFromBottomBar={true}
        offlineData={{ rows: data?.data ?? [], cols: new_columns }}
      />
    </>
  );
};

export default React.memo(PreviewResultDataset);
