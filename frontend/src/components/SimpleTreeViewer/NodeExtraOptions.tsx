import { TDatabaseTreePages } from "Apps/Connect/Connect.types";
import LinkModal from "Apps/Connect/Links/LinkModal.view";
import {
  ResourceSubTypeEnum,
  ResourceTypeEnum,
} from "Apps/explorer/explorer.utils";
import { Tooltip } from "antd";
import { EyeIcon, LinkIcon } from "assets/icons/boslerActionIcons";
import { DatabaseIcon } from "assets/icons/boslerDataIcons";
import { DownloadIcon } from "assets/icons/boslerInterfaceIcons";
import { PopOutIcon } from "assets/icons/boslerNavigationIcon";
import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useParams } from "react-router";
import { getLanguageLabel } from "utils/utilities";
import {
  updatePreviewQuery,
  updateSourceQuery,
} from "../../redux/actions/sourceActions";
import { ThunkAppDispatch } from "../../redux/types/store";
interface IProps {
  node: any;
  active: boolean;
  page: TDatabaseTreePages;
  parent: any;
}

export const getPreviewStatement = (
  tableName: string,
  database: ResourceTypeEnum,
  schema?: string
) => {
  if (database == ResourceTypeEnum.POSTGRESSOURCE) {
    return "SELECT * from " + tableName + " limit 100;";
  } else if (database == ResourceTypeEnum.MYSQLSERVERSOURCE) {
    return "SELECT TOP 100 * from " + schema + "." + tableName + ";";
  } else if (database == ResourceTypeEnum.MYSQLSOURCE) {
    return "SELECT * from " + schema + "." + tableName + " limit 100;";
  } else if (database == ResourceTypeEnum.ORACLE21SOURCE) {
    return (
      "SELECT * FROM ( SELECT * FROM " + tableName + ") WHERE ROWNUM <= 100;"
    );
  } else if (database == ResourceTypeEnum.SNOWFLAKESOURCE) {
    return "SELECT * from " + schema + "." + tableName + " limit 100;";
  } else {
    throw new Error("Not a valid source");
  }
};

const NodeExtraOptions = ({ node, active, page, parent }: IProps) => {
  const { id } = useParams();
  const dispatch = useDispatch<ThunkAppDispatch>();
  const regex = new RegExp("SOURCE");

  const [isNewLinkModalOpen, setIsNewLinkModalOpen] = useState(false);
  console.log("id", id, isNewLinkModalOpen);

  if (page === "SHAREPOINT_CONNECTOR") {
    return (
      <>
        {[
          "application/vnd.ms-excel",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "text/csv",
        ].includes(node.type) && (
          <div
            className="text-and-icon-center"
            onClick={() => {
              console.log("HADNLE ONCLICK", setIsNewLinkModalOpen);
              setIsNewLinkModalOpen(true);
            }}
          >
            <LinkIcon />
          </div>
        )}
        {node["@microsoft.graph.downloadUrl"] && (
          <div
            className="text-and-icon-center"
            onClick={() => {
              window.open(node["@microsoft.graph.downloadUrl"], "_blank");
            }}
          >
            <DownloadIcon size={14} />
          </div>
        )}
        <div
          className="text-and-icon-center"
          onClick={() => {
            window.open(node.webUrl, "_blank");
          }}
        >
          <PopOutIcon size={14} />
        </div>
        {id && isNewLinkModalOpen && (
          <LinkModal
            isVisible={isNewLinkModalOpen}
            setIsVisible={setIsNewLinkModalOpen}
            defaultSource={id}
            defaultFileId={node["id"]}
            defaultFileType={ResourceSubTypeEnum.XLS}
            defaultLinkName={"Data-Link-" + node.name}
          />
        )}
      </>
    );
  } else if (
    regex.test(node.type) &&
    node.subType == ResourceSubTypeEnum.TABLE_CHART &&
    page == "LINK"
  ) {
    return (
      <div
        className="--flex-row-center"
        style={{
          width: "fit-content",
          height: "fit-content",
        }}
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
        }}
      >
        <Tooltip title={getLanguageLabel("previewTable")}>
          <div
            onClick={() => {
              // Handle Preview
              dispatch(
                updatePreviewQuery(
                  node.id,
                  getPreviewStatement(node.name, node.type, parent?.name)
                )
              );
            }}
          >
            <EyeIcon size={14} />
          </div>
        </Tooltip>
        <Tooltip title={getLanguageLabel("queryTable")}>
          <div
            onClick={() => {
              dispatch(
                updateSourceQuery(
                  node.id,
                  "--" +
                    getPreviewStatement(node.name, node.type, parent?.name),
                  true
                )
              );
            }}
          >
            <DatabaseIcon size={14} />
          </div>
        </Tooltip>
      </div>
    );
  } else if (
    regex.test(node.type) &&
    node.subType == ResourceSubTypeEnum.TABLE_CHART &&
    page == "SOURCE"
  ) {
    return (
      <div
        className="--flex-row-center"
        style={{
          width: "fit-content",
          height: "fit-content",
        }}
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
        }}
      >
        <Tooltip title={getLanguageLabel("previewTable")}>
          <div
            onClick={() => {
              // Handle Preview
              dispatch(
                updatePreviewQuery(
                  node.id,
                  getPreviewStatement(node.name, node.type, parent?.name)
                )
              );
            }}
          >
            <EyeIcon size={14} />
          </div>
        </Tooltip>
        <Tooltip title={getLanguageLabel("createDataLink")}>
          <div
            onClick={() => {
              setIsNewLinkModalOpen(true);
            }}
          >
            <LinkIcon size={14} />
          </div>
        </Tooltip>
        {id && isNewLinkModalOpen && (
          <LinkModal
            isVisible={isNewLinkModalOpen}
            setIsVisible={setIsNewLinkModalOpen}
            defaultSource={id}
            defaultQuery={getPreviewStatement(
              node.name,
              node.type,
              parent?.name
            )}
            defaultLinkName={"Data-Link-" + node.name}
          />
        )}
      </div>
    );
  }

  return null;
};

export default NodeExtraOptions;
