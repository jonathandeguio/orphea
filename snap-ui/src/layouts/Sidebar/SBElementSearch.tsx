import { SearchIcon } from "assets/icons/movetodataActionIcons";
import HeaderSearch from "layouts/Header/HeaderSearch";
import React, { useState } from "react";
import { useHotkeys } from "react-hotkeys-hook";
import { getLanguageLabel } from "utils/utilities";
import SBElement from "./SBElement";
import MoveToDataModal from "components/MoveToDataModalContainer";

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
        icon={<SearchIcon size={iconSize} />}
        tooltip={getLanguageLabel("searchMsg")}
        text="search"
        showText={showText}
        onClick={() => {
          setIsHeaderSearchModalOpen(true);
        }}
      />
      <MoveToDataModal
        footer={null}
        open={isHeaderSearchModalOpen}
        onCancel={() => setIsHeaderSearchModalOpen(false)}
        width={600}
      >
        <HeaderSearch setIsHeaderSearchModalOpen={setIsHeaderSearchModalOpen} />
        <br />
      </MoveToDataModal>
    </>
  );
};

export default SBElementSearch;
