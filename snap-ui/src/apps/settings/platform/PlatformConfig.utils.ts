import { openNotification } from "utils/utilities";
import { downloadCSVAPI } from "./PlatformConfig.api";

export const timezones = [
  { label: "-11:00", value: "Pacific/Midway (SST)" },
  { label: "-10:00", value: "Pacific/Honolulu (HST)" },
  { label: "-09:30", value: "Pacific/Marquesas" },
  { label: "-09:00", value: "America/Adak (HDT)" },
  { label: "-08:00", value: "America/Anchorage (AKDT)" },
  { label: "-07:00", value: "America/Los_Angeles (PDT)" },
  { label: "-06:00", value: "America/Denver (MDT)" },
  { label: "-05:00", value: "America/Mexico_City (CDT)" },
  { label: "-04:00", value: "America/New_York (EDT)" },
  { label: "-03:00", value: "America/Sao_Paulo" },
  { label: "-02:00", value: "Atlantic/South_Georgia" },
  { label: "-01:00", value: "Atlantic/Cape_Verde" },
  { label: "00:00", value: "Etc/UTC" },
  { label: "+01:00", value: "Europe/London (BST)" },
  { label: "+02:00", value: "Europe/Paris (CEST)" },
  { label: "+03:00", value: "Europe/Moscow (MSK)" },
  { label: "+03:30", value: "Asia/Tehran" },
  { label: "+04:00", value: "Asia/Dubai" },
  { label: "+04:30", value: "Asia/Kabul" },
  { label: "+05:00", value: "Asia/Karachi (PKT)" },
  { label: "+05:30", value: "Asia/Kolkata (IST)" },
  { label: "+05:45", value: "Asia/Kathmandu" },
  { label: "+06:00", value: "Asia/Dhaka (BST)" },
  { label: "+06:30", value: "Asia/Yangon" },
  { label: "+07:00", value: "Asia/Jakarta" },
  { label: "+08:00", value: "Asia/Shanghai (CST)" },
  { label: "+08:45", value: "Australia/Eucla" },
  { label: "+09:00", value: "Asia/Tokyo (JST)" },
  { label: "+09:30", value: "Australia/Adelaide (ACST)" },
  { label: "+10:00", value: "Australia/Brisbane (AEST)" },
  { label: "+10:30", value: "Australia/Lord_Howe" },
  { label: "+11:00", value: "Asia/Magadan (MAGT)" },
  { label: "+12:00", value: "Pacific/Auckland (NZST)" },
  { label: "+12:45", value: "Pacific/Chatham" },
  { label: "+13:00", value: "Pacific/Tongatapu" },
  { label: "+14:00", value: "Pacific/Kiritimati" },
];

export const downloadCSV = (
  id: string,
  branch: string,
  transactionId: string
) => {
  downloadCSVAPI(id, branch, transactionId)
    .then(({ data }) => {
      const blob = new Blob([data], { type: "text/csv" });
      const url = URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = url;
      link.download = `${id}.csv`;

      document.body.appendChild(link);
      link.click();

      URL.revokeObjectURL(url);
      document.body.removeChild(link);
    })
    .catch((error) => {
      openNotification(
        "Download Failed",
        "The Dataset which you are trying to download failed",
        "error"
      );
    });
};
