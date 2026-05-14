import { FileExplorerContextMenuHandlerType } from "Apps/explorer/FileExplorer";
import { ResourceType } from "Apps/explorer/explorer.utils";
import { Input } from "antd";
import { useOutsideClickHandler } from "hooks/useOutsideClickHandler";
import React, { useRef, useState } from "react";
import { makeDebounceFunction, notEmpty } from "utils/utilities";
import { TreeNode } from "./TreeNode";
const { Search } = Input;
interface Props {
  treeData: any;
  type?: ResourceType[];
  defaultActiveId?: string;
  onContextMenu?: FileExplorerContextMenuHandlerType;
  onClick?: (node: any) => void;
  onDoubleClick?: (node: any) => void;
  dynamicFetching: boolean;
  openOnSingleClick: boolean;
  hidden?: string[];
  onDrop?: (a: any, b: any) => void;
  page: "LINK" | "SOURCE";
  originId?: string;
}

export const SimpleTreeViewer: React.FC<Props> = ({
  defaultActiveId,
  treeData,
  type,
  onClick,
  onContextMenu,
  onDoubleClick,
  dynamicFetching,
  openOnSingleClick,
  hidden,
  onDrop,
  page,
  originId,
}) => {
  const [closeAllNodesTrigger, setCloseAllNodesTrigger] = useState(false);
  const [searchValue, setSearchValue] = useState<String>("");
  const [filteredTreeData, setFilteredTreeData] = useState(treeData);
  const treeRef = useRef<HTMLDivElement | null>();
  const [activeId, setActiveId] = useState<string | undefined>(undefined);
  const onSingleNodeClickHandler = (node: any) => {
    setActiveId(node.id);
    onClick?.(node);
  };
  const onDoubleNodeClickHandler = (node: any) => {
    setActiveId(node.id);
    onDoubleClick?.(node);
  };
  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;

    const traverse = (node: any, value: string) => {
      node.children.map((item: any) => {
        traverse(item, value);

        if (item.name.indexOf(value) > -1) {
          node.isOpen = true;
          item.hidden = false;
        } else {
          if (!notEmpty(item.children)) {
            item.hidden = true;
          } else {
            if (item.isOpen) {
              item.hidden = false;
              node.isOpen = true;
            } else {
              item.hidden = true;
            }
          }
        }
      });
    };
    const nodeData = JSON.parse(JSON.stringify(treeData));
    if (value && value != "") {
      traverse(nodeData, value);
    } else {
      closeAllNodes();
    }
    setFilteredTreeData(nodeData);
    setSearchValue(value);
  };

  const closeAllNodes = () => {
    setCloseAllNodesTrigger(true);
    // Reset the trigger after closing to allow future closes
    setTimeout(() => setCloseAllNodesTrigger(false), 0);
  };

  useOutsideClickHandler(() => setActiveId(undefined), [treeRef]);

  return (
    <div style={{ height: "100%" }} ref={(ref) => (treeRef.current = ref)}>
      <Search
        className="--mb5"
        placeholder="Search"
        onChange={makeDebounceFunction((e: any) => onChange(e), 500)}
      />
      <TreeNode
        isExpanded={true}
        hidden={hidden}
        key={filteredTreeData.id}
        node={filteredTreeData}
        type={type}
        index={-1}
        activeId={activeId ? activeId : defaultActiveId}
        openOnSingleClick={openOnSingleClick}
        onDoubleClick={onDoubleNodeClickHandler}
        onClick={onSingleNodeClickHandler}
        onContextMenu={onContextMenu}
        dynamicFetching={dynamicFetching}
        onDrop={onDrop}
        closeAllNodesTrigger={closeAllNodesTrigger}
        page={page}
        originId={originId}
      />
    </div>
  );
};
