import "@blocknote/core/style.css";
import LexicalEditor from "Apps/LexicalEditor";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import React from "react";

interface Props {
  dashboardId: string;
  layout: any;
  element: any;
  editable: boolean;
  removeElement: any;
  tabId: string;
  updateTabElement: (elementId: string, data: string) => void;
}

const EditorElement = ({
  dashboardId,
  layout,
  element,
  editable,
  removeElement,
  tabId,
  updateTabElement,
}: Props) => {
  const defaultData = element && element.data ? element.data : undefined;

  const handleChange = (editorState: any) => {
    updateTabElement(element.id, JSON.stringify(editorState));
  };

  return (
    <div
      className={
        editable ? "editorElement editableElementBorder" : "editorElement"
      }
      style={{
        cursor: editable ? "move" : "pointer",
        color: "var(--PRIMARY_COLOR)",
      }}
    >
      <div
        style={{
          height: "calc(100%)",
          width: "calc(100%)",
          // background: "var(--background-color)",
          cursor: !editable ? "move" : "pointer",
        }}
        onMouseDownCapture={(e) => {
          e.stopPropagation();
        }}
        onClick={(e) => {
          e.stopPropagation();
        }}
      >
        <LexicalEditor
          defaultData={defaultData}
          handleChange={handleChange}
          editable={editable}
        />
      </div>
      {editable && (
        <div
          className="editableElementBorder-delete"
          onMouseDown={(e) => {
            e.stopPropagation();
          }}
          onClick={(e) => {
            e.stopPropagation();
            removeElement(element.id);
          }}
        >
          <TrashIcon color="var(--bosler-intent-danger)" />
        </div>
      )}
    </div>
  );
};
export default EditorElement;
