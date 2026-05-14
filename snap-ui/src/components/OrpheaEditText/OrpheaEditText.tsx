import React from "react";
import "./OrpheaEditText.scss";

const OrpheaEditText = (props: { children: any }) => {
  return (
    <div
      contentEditable="true"
      className="orphea_edit_text"
      onInput={(e) => {
        e.preventDefault();
      }}
    >
      {props.children}
    </div>
  );
};

export default OrpheaEditText;
