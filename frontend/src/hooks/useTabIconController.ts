import { useEffect } from "react";
import { useSelector } from "react-redux";
import { getLanguageLabel, isDefined } from "utils/utilities";

/**
 * DEV - WE NEED TO ADD ALL APPS HERE.
 * MAKE SURE THAT LABEL IS ADDED TO LANGUAGE JSON FILE
 */
export const BOSLER_APPS = {
  DATASET: {
    label: "dataset",
    iconHref: "/favicons/general/accessManager.svg",
  },
  ACCESS_MANAGER: {
    label: "accessManager",
    iconHref: "/favicons/general/accessManager.svg",
  },
  SCHEDULER: {
    label: "scheduler",
    iconHref: "/favicons/general/schedulerIcon.svg",
  },
  PROJECTS: {
    label: "projects",
    iconHref: "/favicons/explorer/projectIcon.svg",
  },
} as const;

export type BOSLER_APPS_TYPES = keyof typeof BOSLER_APPS;

export const useTabMetaDataController = (app: BOSLER_APPS_TYPES) => {
  const { config } = useSelector((state) => (state as any).platformConfig);
  useEffect(() => {
    document.title = getLanguageLabel(BOSLER_APPS[app].label);
    document
      .querySelector('link[rel="icon"]')
      ?.setAttribute("href", BOSLER_APPS[app].iconHref);
    return () =>
      (document.title =
        isDefined(config) && isDefined(config.platformName)
          ? config.platformName
          : "MoveToData");
  }, []);
};
