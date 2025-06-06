import { Typography } from "antd";
import { IHeaderSearchFooter } from "layouts/GlobalSearchFilter/HeaderSearch.types";
import React from "react";
import { useNavigate } from "react-router";
import { getApplicationLink, getLanguageLabel } from "utils/utilities";
import { BoslerTypography } from "components/CommonUI/BoslerTypography";

const HeaderSearchFooter: React.FC<IHeaderSearchFooter> = ({
  applications,
  setIsHeaderSearchModalOpen,
}) => {
  const navigateApplication = useNavigate();
  return (
    <div className="global-search-footer">
      <Typography.Text type="secondary" className="apllication-heading">
        {getLanguageLabel("application")}
      </Typography.Text>
      <div className="application-chip-container">
        {applications.map((application) => (
          <div
            className="application-chip"
            onClick={() => {
              navigateApplication(getApplicationLink(application));
              setIsHeaderSearchModalOpen(false);
            }}
          >
            <BoslerTypography>{application}</BoslerTypography>
          </div>
        ))}
      </div>
    </div>
  );
};
export default HeaderSearchFooter;
