import { Tooltip } from "antd";
import { CopyIcon } from "assets/icons/boslerEditorIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React, { useEffect, useState } from "react";
import { copyToClipboard, getLanguageLabel } from "utils/utilities";

interface IProps {
  jsonString: string;
  heading: string;
}

const JsonFormatter = ({ heading, jsonString }: IProps) => {
  const [formattedJSON, setFormattedJSON] = useState("");
  const [isHovered, setIsHovered] = useState<boolean>(false);
  const [tooltipTitle, setTooltipTitle] = useState(
    getLanguageLabel("clickToCopyIntoClipboard")
  );

  const handleMouseEnter = (e: any) => {
    setIsHovered(true);
  };

  const handleMouseLeave = (e: any) => {
    setIsHovered(false);
  };

  useEffect(() => {
    try {
      const parsedJSON = JSON.parse(jsonString);
      setFormattedJSON(JSON.stringify(parsedJSON, null, 2));
    } catch (error) {
      setFormattedJSON(jsonString);
    }
  }, [jsonString]);

  return (
    <div onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
      <div className="--flex-row-space-between">
        <div className="BoslerHeader1">{heading}</div>
        {isHovered && (
          <Tooltip title={tooltipTitle}>
          <BoslerButton
            icon={<CopyIcon />}
            onClick={() => copyToClipboard(formattedJSON, setTooltipTitle)}
            minimal
            icononly
            trimicononlypadding
          />
          </Tooltip>
        )}
      </div>
      <div
        style={{
          overflow: "auto",
          backgroundColor: "var(--bosler-bkg-color-muted)",
          color: "var(--bosler-font-color-light-black)",
          padding: "10px",
        }}
      >
        <pre style={{ whiteSpace: "pre-wrap" }}>{formattedJSON}</pre>
      </div>
    </div>
  );
};

export default JsonFormatter;
