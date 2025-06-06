import { RefSelectProps, Select, SelectProps, Typography } from "antd";
import React, { useEffect, useRef, useState } from "react";
import { getLanguageLabel, isDefined } from "utils/utilities";
import { SearchIcon } from "assets/icons/boslerActionIcons";

import axios from "axios";
import { useNavigate } from "react-router";
import { bolserApplications, getApplicationLink } from "./Header.utils";

const { Option } = Select;

const { Title, Text } = Typography;

const HeaderSearch = ({
  setIsHeaderSearchModalOpen,
}: {
  setIsHeaderSearchModalOpen: any;
}) => {
  const selectRef: React.RefObject<RefSelectProps> | null = useRef(null);

  let timeout: ReturnType<typeof setTimeout> | null;

  const navigateApplication = useNavigate();

  const [resources, setResources] = useState<SelectProps["options"]>([]);
  const [applications, setApplications] = useState<SelectProps["options"]>([]);

  const getFilteredResources = (query: string) => {
    if (timeout) {
      clearTimeout(timeout);
      timeout = null;
    }

    const getFilteredResourcesAPI = () => {
      if (isDefined(query)) {
        axios
          .post(`/kitab/globalSearch`, query)
          .then(({ data }) => {
            setResources(data);
          })
          .catch((error) => {});
      }
    };
    timeout = setTimeout(getFilteredResourcesAPI, 300);
    const newApplications = bolserApplications?.filter((application: any) =>
      application.name.toLowerCase().includes(query.toLowerCase())
    );

    setApplications(newApplications);
  };

  useEffect(() => {
    (selectRef as any)?.current?.focus();
  }, []);

  return (
    <Select
      style={{ width: "100%", padding: 0, margin: 0 }}
      autoFocus={true}
      ref={selectRef}
      value={<></>}
      placeholder={getLanguageLabel("searchMsg")}
      options={[
        {
          label: getLanguageLabel("application"),
          id: "application",
          options: applications,
        },
      ].filter((option) => {
        if (option.id == "resource")
          return isDefined(resources) && resources.length > 0;
        else if (option.id == "application")
          return isDefined(applications) && applications.length > 0;
      })}
      onSelect={(e: any) => {
        setIsHeaderSearchModalOpen(false);

        navigateApplication(getApplicationLink(e));
        setApplications(bolserApplications);
        setResources([]);
      }}
      filterOption={false}
      showSearch={true}
      notFoundContent={null}
      onSearch={(e) => getFilteredResources(e)}
      suffixIcon={
        <div
          onClick={() => {
            alert("search");
          }}
          style={{
            color: "#1967d2",
            cursor: "pointer",
            fontSize: "13px",
            display: "flex",
            flexDirection: "row",
          }}
        >
          <SearchIcon />
        </div>
      }
    />
  );
};

export default HeaderSearch;
