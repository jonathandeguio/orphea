import { SearchFiledIcon } from "assets/icons/boslerActionIcons";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import HeaderSearch from "layouts/components/GlobalSearch";
import React, { useState } from "react";
import { useHotkeys } from "react-hotkeys-hook";
import { getLanguageLabel } from "utils/utilities";
import SBElement from "./SBElement";
import { BoslerTypography } from "components/CommonUI/BoslerTypography";

interface TProps {
  iconSize?: number;
  showText?: any;
  selected?: boolean;
  onClick?: any;
  ref?: any;
}

const SBElementSearch = ({
  iconSize,
  showText,
  selected,
  onClick,
  ref,
}: TProps) => {
  const [isHeaderSearchModalOpen, setIsHeaderSearchModalOpen] = useState(false);
  useHotkeys("/", (event: any) => {
    event.preventDefault();
    setIsHeaderSearchModalOpen(true);
  });
  return (
    <>
      <SBElement
        icon={<SearchFiledIcon size={iconSize} color="#D3D7E2" />}
        tooltip={getLanguageLabel("searchMsg")}
        text="search"
        showText={showText}
        onClick={() => {
          setIsHeaderSearchModalOpen(true);
        }}
      />
      <BoslerModal
        className="global-search-modal"
        // footer={
        //   <div
        //     style={{
        //       width: "100%",
        //       textAlign: "center",
        //       paddingBottom: "10px",
        //     }}
        //   >
        //     <BoslerTypography variant="para">
        //       press / to search
        //     </BoslerTypography>
        //   </div>
        // }
        open={isHeaderSearchModalOpen}
        onCancel={() => setIsHeaderSearchModalOpen(false)}
        width={950}
      >
        <HeaderSearch setIsHeaderSearchModalOpen={setIsHeaderSearchModalOpen} />
      </BoslerModal>
    </>
  );
};

export default SBElementSearch;
