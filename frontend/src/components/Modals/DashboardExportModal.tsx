import { DocsIcon } from "assets/icons/boslerFileIcons";
import {
  CollectionIcon,
  DownloadIcon,
} from "assets/icons/boslerInterfaceIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React from "react";
import { useDispatch } from "react-redux";
import { getLanguageLabel } from "utils/utilities";
import { ThunkAppDispatch } from "../../redux/types/store";

interface TProps {
  openExportModal: boolean;
  setOpenExportModal: React.Dispatch<React.SetStateAction<boolean>>;
  donwloadImageCallback: (convertTo: "image" | "pdf") => void;
  gridRef: any;
}
const DashboardExportModal = ({
  openExportModal,
  setOpenExportModal,
  donwloadImageCallback,
  gridRef,
}: TProps) => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const handleImageExport = () => {
    donwloadImageCallback("image");
  };

  const handlePDFExport = () => {
    donwloadImageCallback("pdf");
  };

  return (
    <BoslerModal
      open={openExportModal}
      onCancel={() => setOpenExportModal(false)}
      headingIcon={<DownloadIcon />}
      heading={getLanguageLabel("export")}
    >
      <div
        style={{
          display: "flex",
          flexDirection: "row",
          gap: "20px",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <div
          style={{
            flex: 1,
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            flexDirection: "column",
            background: "var(--background-color)",
            padding: "20px",
            border: "1px solid var(--bosler-border-color-default)",
            height: "200px",
            width: "250px",
            gap: "10px",
          }}
        >
          <div className="BoslerHeader1">
            <CollectionIcon />
            Image
          </div>
          <div
            style={{
              marginBottom: "10px",
            }}
          >
            {getLanguageLabel("getParticularTab")} Image
          </div>
          <BoslerButton icon={<DownloadIcon />} onClick={handleImageExport}>
            {getLanguageLabel("download")} Image
          </BoslerButton>
        </div>
        <div
          style={{
            flex: 1,
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
            background: "var(--background-color)",
            padding: "20px",
            border: "1px solid var(--bosler-border-color-default)",
            height: "200px",
            width: "250px",
            gap: "10px",
          }}
        >
          <div className="BoslerHeader1">
            <DocsIcon />
            PDF
          </div>
          <div
            style={{
              marginBottom: "10px",
            }}
          >
            {getLanguageLabel("getParticularTab")} PDF
          </div>
          <BoslerButton icon={<DownloadIcon />} onClick={handlePDFExport}>
            {getLanguageLabel("download")} PDF
          </BoslerButton>
        </div>
      </div>
    </BoslerModal>
  );
};

export default DashboardExportModal;
