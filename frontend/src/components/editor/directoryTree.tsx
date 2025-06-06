import { TreeExplorer } from "Apps/explorer";
import { FileExplorerContextMenuHandlerType } from "Apps/explorer/FileExplorer";
import { Skeleton } from "antd";
import { useContextMenuState } from "common/components/ContextMenu";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams, useSearchParams } from "react-router-dom";
import {
  decodeFromBase64,
  isDefined,
  isEmpty,
  notEmpty,
  openNotification,
} from "utils/utilities";
import {
  createOrUpdateRepositoryEditorPane,
  updateRepositoryEditorActiveId,
  updateRepositoryPaneGitBlame,
} from "../../redux/repositoryEditorSlice";
import { RootState } from "../../redux/types/store";
import { EditorTreeContextMenu } from "./directoryTreeContextMenu";
import {
  getFileContentAPI,
  gitBlameApi,
  moveFileFractalAPI,
} from "./editor.api";
import { createReverseMap } from "Apps/explorer/explorer.utils";

const DirectoryTree = (props: $TSFixMe) => {
  const { id, branch, detached } = useParams();
  const [contextMenuNode, setContextMenuNode] = useState<any>(null);
  const dispatch = useDispatch();
  const contextMenuStore = useContextMenuState();

  const { editorPanes } = useSelector(
    (state: RootState) => state.repositoryEditor
  );

  const contextMenuHandler: FileExplorerContextMenuHandlerType = (
    x: number,
    y: number,
    node: any
  ) => {
    contextMenuStore.displayContextMenu(x, y);
    setContextMenuNode(node);
  };
  const onClick = (file: $TSFixMe) => {
    if (file.type === "FOLDER") {
      return;
    }

    if (isEmpty(editorPanes[file.path]) && notEmpty(id) && notEmpty(branch)) {
      getFileContentAPI(id, branch, file.path, isDefined(detached))
        .then(({ data }) => {
          dispatch(
            createOrUpdateRepositoryEditorPane({
              content: decodeFromBase64(data["fileContents.b64"]),
              fileName: file.name,
              gitBlame: [],
              id: file.path,
              path: file.path,
              type: file.subType,
              paneType: "EDITOR",
            })
          );
        })
        .then(() => {
          dispatch(updateRepositoryEditorActiveId(file.path));
        })
        .then(() => {
          gitBlameApi(id, branch, file.path).then(({ data }) => {
            dispatch(
              updateRepositoryPaneGitBlame({
                gitBlame: data,
                id: file.path,
              })
            );
          });
        });
    } else {
      dispatch(updateRepositoryEditorActiveId(file.path));
    }
  };

  return (
    <div
      className="show-file-icons show-folder-icons"
      style={{
        flexGrow: 1,
      }}
    >
      <div className="workspaceContainer">
        {isEmpty(props.treeData) ? (
          <div style={{ margin: "0 1rem" }}>
            <Skeleton active />
          </div>
        ) : (
          <TreeExplorer
            onContextMenu={contextMenuHandler}
            dynamicFetching={false}
            defaultActiveId={"0-2-1"}
            treeData={props.treeData}
            onClick={onClick}
            openOnSingleClick={true}
            fileStatus={createReverseMap(props?.fileStatus)}
            onDrop={(a, b) => {
              let destinationPath = b.path + `/${a.name}`;

              if (b.type === "FILE") {
                let splitB = b.path.split("/");
                splitB.pop();
                splitB.push(a.name);
                destinationPath = splitB.join("/");
              }

              if (notEmpty(id) && notEmpty(branch)) {
                moveFileFractalAPI(
                  id,
                  branch,
                  a.path,
                  destinationPath,
                  a.type.toLowerCase(),
                  "folder"
                )
                  .then(() => props.refreshTree(id, branch))
                  .catch(({ response }) => {
                    openNotification(
                      response.data.error,
                      response.data.description,
                      "error"
                    );
                  });
              }
            }}
          />
        )}
      </div>
      {notEmpty(id) && notEmpty(branch) && (
        <EditorTreeContextMenu
          repo={id}
          branch={branch}
          onClick={onClick}
          refreshTree={props.refreshTree}
          node={contextMenuNode}
          store={contextMenuStore}
        />
      )}
    </div>
  );
};

export default DirectoryTree;
