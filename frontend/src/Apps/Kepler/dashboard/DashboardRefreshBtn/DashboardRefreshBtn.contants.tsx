export const DASHBOARD_REFRESH_CONFIG = [
  {
    key: "off",
    label: "Off",
    value: null,
  },{
    key: 30000,
    label: "30 seconds",
    value: 30000,
  },
  {
    key: 60000,
    label: "1 minute",
    value: 60000,
  },
  {
    key: 3000000,
    label: "5 minute",
    value: 3000000,
  },
  {
    key: 6000000,
    label: "60 minute",
    value: 6000000,
  },
];

export const DASHBOARD_REFRESH_DEFAULT_CONFIG = {
  isPlaying: true,
  refreshInterval: 3600000,
  liveRefresh: false,
};
