import { Breadcrumb } from "Apps/explorer/BreadCrumb";
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useParams, useSearchParams } from "react-router-dom";
import { isDefined } from "utils/utilities";

// BreadCrumb Component
const CustomBreadCrumb = ({ alwaysShow = true }) => {
  const { id, branch } = useParams();
  const [searchParams] = useSearchParams();
  const { links } = useSelector((state) => (state as $TSFixMe).headerLink);
  const [showBreadCrumb, setShowBreadCrumb] = useState(false);
  const activeId = searchParams.get("activeId") ?? id;

  useEffect(() => {
    if (isDefined(links) && links?.length > 0) {
      const lastLink = links[links.length - 1];
      if (
        lastLink.type == "CHART" ||
        lastLink.type == "DATASET" ||
        lastLink.type == "DASHBOARD" ||
        lastLink.type == "REPOSITORY" ||
        lastLink.type == "AGENT" ||
        lastLink.type == "SOURCE" ||
        lastLink.type == "LINK" ||
        lastLink.type == "FILE"
      )
        setShowBreadCrumb(true);
      else setShowBreadCrumb(false);
    } else setShowBreadCrumb(false);
  }, [links]);

  if (isDefined(activeId) && showBreadCrumb)
    return <Breadcrumb id={activeId} />;

  return <></>;
};

export default CustomBreadCrumb;
