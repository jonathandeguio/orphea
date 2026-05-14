import BoslerTable from "Apps/Dataset/Table/BoslerTable";
import { CommentState } from "assets/Illustrations/EmptyState";
import { WarningIcon } from "assets/icons/boslerActionIcons";
import NoData from "components/CommonUI/NoData";
import BoslerLoader from "components/boslerLoader";
import React, { useEffect, useState } from "react";
import { getLanguageLabel, isDefined } from "utils/utilities";

interface TProps {
  data: any;
  loading: boolean;
}

interface PreviewData {
  data: any;
  status: string;
  rows: any;
}

const PreviewLink = ({ data, loading }: TProps) => {
  const [previewData, setPreviewData] = useState<PreviewData | undefined>();
  const [columns, setColumns] = useState([]);

  useEffect(() => {
    if (isDefined(data)) {
      setPreviewData(data);
      let newColumns: any = [];

      data?.cols?.map((col: any) => {
        newColumns.push({
          id: col.field,
          title: col.headerName,
          dataIndex: col.field,
          key: col.field,
          cell: (info: any) => info.getValue(),
          footer: (props: any) => props.column.id,
          accessorKey: col.field,
          header: col.headerName,
          size: 200,
          type: col.type,
        });
      });

      setColumns(newColumns);
    }
  }, [data]);

  return (
    <>
      <div
        style={{
          height: "100%",
          width: "100%",
        }}
      >
        {loading ? (
          <BoslerLoader />
        ) : isDefined(previewData) && previewData?.status == "SUCCESS" ? (
          <BoslerTable
            isTableFromBottomBar={true}
            offlineData={{
              rows:
                previewData?.rows && columns && columns.length > 0
                  ? previewData.rows
                  : [],
              cols: columns,
            }}
          />
        ) : previewData && previewData?.status == "FAILED" ? (
          <>
            <div className="BoslerHeader1">
              <WarningIcon color={"var(--bosler-intent-danger)"} />{" "}
              {previewData?.status} | There was an error running preview{" "}
            </div>
            <br />
            {previewData?.data}
          </>
        ) : (
          <NoData
            heading={getLanguageLabel("startPreviewToResults")}
            icon={<CommentState />}
          />
        )}
      </div>
    </>
  );
};

export default React.memo(PreviewLink);
