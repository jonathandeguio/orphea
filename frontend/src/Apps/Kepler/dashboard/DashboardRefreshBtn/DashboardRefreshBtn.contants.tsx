
export const DASHBOARD_REFRESH_CONFIG = [
  {
    key: "off",
    label: "Off",
    value: null,
  },
  {
    key: "10000",
    label: "10s",
    value: 10000,
  },
  {
    key: "30000",
    label: "30s",
    value: 30000,
  },
  {
    key: "60000",
    label: "1m ",
    value: 60000,
  },
  {
    key: "300000",
    label: "5m",
    value: 300000,
  },
  {
    key: "900000",
    label: "15m",
    value: 900000,
  },
  {
    key: "1800000",
    label: "30m",
    value: 1800000,
  },
  {
    key: "3600000",
    label: "1h" ,
    value: 3600000,
  },
  {
    key: "7200000",
    label: "2h",
    value: 7200000,
  },
  {
    key: "86400000",
    label: "1d",
    value: 86400000,
  },
];

export const DASHBOARD_REFRESH_DEFAULT_CONFIG = {
  isPlaying: true,
  refreshInterval: 3600000,
  liveRefresh: false,
};
