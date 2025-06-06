import { isDefined, isFloat } from "utils/utilities";

const lookup = [
  { value: 1, symbol: "" },
  { value: 1e3, symbol: "K" },
  { value: 1e6, symbol: "M" },
  { value: 1e9, symbol: "G" },
  { value: 1e12, symbol: "T" },
  { value: 1e15, symbol: "P" },
  { value: 1e18, symbol: "E" },
];

/**
 * Format any number as bigNumber format
 * @param number number you need to format
 * @param digits the digits you want the number to be reduced to
 * @returns string
 */
export function nFormatter(
  data: number | string | number[] | string[],
  precision: number,
  mode: string,
  scale: string,
  index = 0
): any {
  precision = precision ?? 2;
  mode = mode ?? "none";
  scale = scale ?? "K";

  if (Array.isArray(data)) {
    return nFormatter(data[index], precision, mode, "K", 0);
  }

  if (typeof data === "string" && !isNaN(Number(data))) {
    data = Number(data);
  }

  if (!isDefined(data) || typeof data !== "number") return data;

  if (mode === "none") {
    if (typeof data === "number" && isFloat(data)) {
      return data?.toFixed(precision);
    }
    return data;
  } else if (mode == "auto") {
    const rx = /\.0+$|(\.[0-9]*[1-9])0+$/;
    const item = lookup
      .slice()
      .reverse()
      .find(function (item) {
        return Number(data) >= item.value;
      });

    if (item) {
      const roundedNumber = (data / item.value).toFixed(precision);
      const compactNumber = parseFloat(roundedNumber).toString();
      return compactNumber.replace(rx, "$1") + item.symbol;
    } else {
      if (typeof data === "number" && isFloat(data)) {
        return data?.toFixed(precision);
      }
      return data;
    }
  } else if (mode == "manual") {
    const scales: { [key: string]: any } = {
      K: 1e3,
      M: 1e6,
      G: 1e9,
      T: 1e12,
      P: 1e15,
      E: 1e18,
    };

    if (scales.hasOwnProperty(scale)) {
      const threshold = scales[scale as any];
      return (data / threshold).toFixed(precision) + scale;
    } else {
      console.error("Invalid scale");
    }
  }
  return data;
}

export const parseCompactNumber = (number: string) => {
  const item = lookup.find((item) => number.endsWith(item.symbol));

  if (item) {
    const numberPart = parseFloat(number.replace(item.symbol, ""));
    return numberPart * item.value;
  } else {
    return parseFloat(number);
  }
};
