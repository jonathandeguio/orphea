import React, { useState } from "react";

const FloatLabel = (props: $TSFixMe) => {
  const [focus, setFocus] = useState(false);
  const { children, label, value, style } = props;

  const labelClass =
    focus || (value && value.length !== 0) ? "label2 label-float" : "label2";

  return (
    <div
      className="float-label"
      onBlur={() => setFocus(false)}
      onFocus={() => setFocus(true)}
    >
      {children}
      <label className={labelClass} style={style}>
        {label}
      </label>
    </div>
  );
};

export default FloatLabel;
