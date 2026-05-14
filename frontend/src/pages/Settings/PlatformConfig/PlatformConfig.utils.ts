import { openNotification } from "utils/utilities";
import { downloadCSVAPI } from "./PlatformConfig.api";

type TimeZone = {
  label: string;
  value: string;
  shortForm?: string;
};

export const timezones: TimeZone[] = [
  { label: "-11:00", value: "Pacific/Midway", shortForm: "SST" },
  { label: "-10:00", value: "Pacific/Honolulu", shortForm: "HST" },
  { label: "-09:30", value: "Pacific/Marquesas" },
  { label: "-09:00", value: "America/Adak", shortForm: "HDT" },
  { label: "-08:00", value: "America/Anchorage", shortForm: "AKDT" },
  { label: "-07:00", value: "America/Los_Angeles", shortForm: "PDT" },
  { label: "-06:00", value: "America/Denver", shortForm: "MDT" },
  { label: "-05:00", value: "America/Mexico_City", shortForm: "CDT" },
  { label: "-04:00", value: "America/New_York", shortForm: "EDT" },
  { label: "-03:00", value: "America/Sao_Paulo" },
  { label: "-02:00", value: "Atlantic/South_Georgia" },
  { label: "-01:00", value: "Atlantic/Cape_Verde" },
  { label: "00:00", value: "Atlantic/Azores" },
  { label: "+01:00", value: "Europe/London", shortForm: "BST" },
  { label: "+02:00", value: "Europe/Paris", shortForm: "CEST" },
  { label: "+03:00", value: "Europe/Moscow", shortForm: "MSK" },
  { label: "+03:30", value: "Asia/Tehran" },
  { label: "+04:00", value: "Asia/Dubai" },
  { label: "+04:30", value: "Asia/Kabul" },
  { label: "+05:00", value: "Asia/Karachi", shortForm: "PKT" },
  { label: "+05:30", value: "Asia/Kolkata", shortForm: "IST" },
  { label: "+05:45", value: "Asia/Kathmandu" },
  { label: "+06:00", value: "Asia/Dhaka", shortForm: "BST" },
  { label: "+06:30", value: "Asia/Yangon" },
  { label: "+07:00", value: "Asia/Jakarta" },
  { label: "+08:00", value: "Asia/Shanghai", shortForm: "CST" },
  { label: "+08:45", value: "Australia/Eucla" },
  { label: "+09:00", value: "Asia/Tokyo", shortForm: "JST" },
  { label: "+09:30", value: "Australia/Adelaide", shortForm: "ACST" },
  { label: "+10:00", value: "Australia/Brisbane", shortForm: "AEST" },
  { label: "+10:30", value: "Australia/Lord_Howe" },
  { label: "+11:00", value: "Asia/Magadan", shortForm: "MAGT" },
  { label: "+12:00", value: "Pacific/Auckland", shortForm: "NZST" },
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
