import React from "react";
import "./BoslerEditText.scss";

const BoslerEditText = (props: { children: any }) => {
  return (
    <div
      contentEditable="true"
      className="bosler_edit_text"
      onInput={(e) => {
        e.preventDefault();
      }}
    >
      {props.children}
    </div>
  );
};

export default BoslerEditText;
