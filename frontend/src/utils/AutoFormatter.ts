type Unit = "number" | "bytes" | "time";

const formatBytes = (value: number, precision: number): string => {
  if (value === 0) return "0 Bytes";
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];
  const i = Math.floor(Math.log(value) / Math.log(k));
  let formattedValue = value / Math.pow(k, i);

  return (
    (Number.isInteger(formattedValue)
      ? formattedValue.toFixed(0)
      : formattedValue.toFixed(precision)
    ).toString() + sizes[i]
  );
};

const formatTime = (value: number, precision: number): string => {
  const hours = Math.floor(value / 3600);
  const minutes = Math.floor((value % 3600) / 60);
  const seconds = Math.floor(value % 60);
  return `${hours}h ${minutes}m ${seconds}s`;
};

const formatNumber = (value: number, precision: number): string => {
  const absValue = Math.abs(value);
  let formattedValue: number;

  if (absValue >= 1e12) {
    formattedValue = value / 1e12;
    return Number.isInteger(formattedValue)
      ? formattedValue.toFixed(0) + "T"
      : formattedValue.toFixed(precision) + "T";
  } else if (absValue >= 1e9) {
    formattedValue = value / 1e9;
    return Number.isInteger(formattedValue)
      ? formattedValue.toFixed(0) + "B"
      : formattedValue.toFixed(precision) + "B";
  } else if (absValue >= 1e6) {
    formattedValue = value / 1e6;
    return Number.isInteger(formattedValue)
      ? formattedValue.toFixed(0) + "M"
      : formattedValue.toFixed(precision) + "M";
  } else if (absValue >= 1e3) {
    formattedValue = value / 1e3;
    return Number.isInteger(formattedValue)
      ? formattedValue.toFixed(0) + "K"
      : formattedValue.toFixed(precision) + "K";
  } else {
    return Number.isInteger(value)
      ? value.toFixed(0)
      : value.toFixed(precision);
  }
};

export const autoFormatter = (
  value: number,
  unit: Unit = "number",
  precision: number = 2
): string => {
  switch (unit) {
    case "number":
      return formatNumber(value, precision);
    case "bytes":
      return formatBytes(value, precision);
    case "time":
      return formatTime(value, precision);
    default:
      throw new Error(`Unsupported unit: ${unit}`);
  }
};
