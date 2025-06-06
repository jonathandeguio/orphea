import de from "javascript-time-ago/locale/de";
import en from "javascript-time-ago/locale/en";
import es from "javascript-time-ago/locale/es";
import fr from "javascript-time-ago/locale/fr";
import hi from "javascript-time-ago/locale/hi";
import React, { useEffect, useState } from "react";
import store from "../redux/store";

import TimeAgo from "javascript-time-ago";

import { Client } from "@stomp/stompjs";
import { notification } from "antd";
import axios from "axios";
import cronParser from "cron-parser";
import type { Dayjs } from "dayjs";
import dayjs from "dayjs";
import dayLocaleData from "dayjs/plugin/localeData";
import { User } from "global";
import SockJS from "sockjs-client";
import { KeyCommandIcon } from "../assets/icons/boslerInterfaceIcons";
import { SingleChevronUpIcon } from "../assets/icons/boslerNavigationIcon";
import { BoslerConfig } from "../config";
import { AllLabels } from "./language";

dayjs.extend(dayLocaleData);

function getLastClassOfUUID(uuid: string) {
  const parts = uuid.split("-");
  return parts[parts.length - 1];
}

function isInt(n: number) {
  return Number(n) === n && n % 1 === 0;
}

function isFloat(n: number) {
  return Number(n) === n && n % 1 !== 0;
}

function ObjectKeys(value: Object | undefined | null): string[] {
  return isDefined(value) ? Object.keys(value) : [];
}

function isDefined<T>(value: T | undefined | null): value is T {
  if (value === null) {
    return false;
  }
  if (value === undefined) {
    return false;
  }
  if ((typeof value).toLowerCase() === "number") {
    if (value === Infinity || value === -Infinity) return false;

    return !isNaN(value as number);
  }
  return true;
}

function isEmpty<T>(value: T | undefined | null): value is T {
  if (isDefined(value)) {
    const type = (typeof value).toLowerCase();
    switch (type) {
      case "boolean":
      case "number":
        return false;
      case "string":
        return "" === value;
      case "object":
        return Array.isArray(value)
          ? (value as Array<T>).length === 0
          : Object.keys(value as object).length === 0;
      case "enum":
      case "tuple":
      case "function":
      case "interface":
      case "class":
        return false;
    }
  } else {
    return true;
  }
  return true;
}

function notEmpty<T>(value: T | undefined | null): value is T {
  if (isDefined(value)) {
    const type = (typeof value).toLowerCase();
    switch (type) {
      case "boolean":
      case "number":
        return true;
      case "string":
        return "" !== value;
      case "object":
        return Array.isArray(value)
          ? (value as Array<T>).length != 0
          : Object.keys(value as object).length != 0;
      case "enum":
      case "tuple":
      case "function":
      case "interface":
      case "class":
        return true;
    }
  }
  return false;
}

function getTheme() {
  const ele = document.body;

  if (isDefined(ele)) {
    return ele?.classList.contains("dark") ? "dark" : "light";
  } else {
    return "light";
  }
}

function setTheme(user: User) {
  const isDarkMode = window.matchMedia("(prefers-color-scheme: dark)").matches;

  if (isDefined(user) && isDefined(user.preferences)) {
    if (
      (user.preferences.mode === "auto" && isDarkMode) ||
      user.preferences.mode === "dark"
    )
      document.body?.classList.add("dark");
    else document.body?.classList.remove("dark");
  } else if (isDarkMode) document.body?.classList.add("dark");
  else document.body?.classList.remove("dark");
}

function isCurrentConfigThemeDark(user: User) {
  const isDarkMode = window.matchMedia("(prefers-color-scheme: dark)").matches;

  if (isDefined(user) && isDefined(user.preferences)) {
    if (
      (user.preferences.mode === "auto" && isDarkMode) ||
      user.preferences.mode === "dark"
    )
      return true;
    return false;
  }
  if (isDarkMode) return true;
  return false;
}

const closeTab = () => {
  window.opener = null;
  window.open("", "_self");
  window.close();
};

const bytesToHumanReadableSize = (size: number): string => {
  const units = ["B", "kB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(size) / Math.log(1024));
  const readableSize = (size / Math.pow(1024, i)).toFixed(0);
  const unit = units[i];
  return `${readableSize} ${unit}`;
};

const numbersToHumanReadable = (num: number): string => {
  let result = num.toString();
  switch (true) {
    case num >= 1e9:
      result = (num / 1e9).toFixed(1).replace(/\.0$/, "") + "b";
      break;
    case num >= 1e6:
      result = (num / 1e6).toFixed(1).replace(/\.0$/, "") + "m";
      break;
    case num >= 1e3:
      result = (num / 1e3).toFixed(1).replace(/\.0$/, "") + "k";
      break;
  }
  return result;
};

function padZero(value: number): string {
  return value < 10 ? `0${value}` : `${value}`;
}

function timeConverter(
  UNIX_timestamp: any,
  showSecond: boolean = true,
  showTime: boolean = true,
  dateFormat: string = "dd/MM/yyyy HH:mm:ss"
): string {
  const date = new Date(UNIX_timestamp);
  const now = new Date();
  const diff = now.getTime() - date.getTime(); // Difference in milliseconds

  // Define time constants
  const msInSecond = 1000;
  const msInMinute = 60 * msInSecond;
  const msInHour = 60 * msInMinute;
  const msInDay = 24 * msInHour;
  const msInWeek = 7 * msInDay;

  // Calculate time difference in a human-readable format
  if (diff < msInMinute) {
    const seconds = Math.floor(diff / msInSecond);
    return `${seconds} second${seconds !== 1 ? "s" : ""} ago`;
  } else if (diff < msInHour) {
    const minutes = Math.floor(diff / msInMinute);
    return `${minutes} minute${minutes !== 1 ? "s" : ""} ago`;
  } else if (diff < msInDay) {
    const hours = Math.floor(diff / msInHour);
    return `${hours} hour${hours !== 1 ? "s" : ""} ago`;
  } else if (diff < msInWeek) {
    const days = Math.floor(diff / msInDay);
    return `${days} day${days !== 1 ? "s" : ""} ago`;
  }

  // Define replacements for date parts
  const dateReplacements = {
    yyyy: date.getFullYear(),
    MM: padZero(date.getMonth() + 1),
    dd: padZero(date.getDate()),
  };

  // Define replacements for time parts
  const timeReplacements = {
    HH: padZero(date.getHours()),
    mm: padZero(date.getMinutes()),
    ss: padZero(date.getSeconds()),
  };

  let formattedDate = dateFormat;

  // Replace date parts in the format string
  for (const [key, value] of Object.entries(dateReplacements)) {
    formattedDate = formattedDate.replace(key, value.toString());
  }

  if (showTime) {
    // Replace time parts in the format string if showTime is true
    for (const [key, value] of Object.entries(timeReplacements)) {
      if (key === "ss" && !showSecond) {
        // Skip seconds if showSecond is false
        continue;
      }
      formattedDate = formattedDate.replace(key, value.toString());
    }
  }

  return formattedDate.trim(); // Trim in case of leading/trailing spaces after modifications
}

// Helper function to pad single digi

function capitalizeFirstLetter(string: string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}

const isMacOS = navigator.platform.toUpperCase().indexOf("MAC") >= 0;
const userOSkey = isMacOS ? (
  <KeyCommandIcon size={12} />
) : (
  <SingleChevronUpIcon size={12} />
);

const text_truncate = function (str: string, length: number, ending: string) {
  if (length == null) {
    length = 100;
  }
  if (ending == null) {
    ending = "...";
  }
  if (str.length > length) {
    return str.substring(0, length - ending.length) + ending;
  } else {
    return str;
  }
};

interface Unit {
  name: string;
  multiplier: number;
  lessThanSecond: string;
}

type UnitsMap = {
  [key: string]: Unit[];
};

function formatTime(milliseconds: number): string {
  const seconds = Math.floor(milliseconds / 1000);
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;

  if (minutes > 0) {
    return `${minutes}m${remainingSeconds}s`;
  } else {
    return `${seconds}s`;
  }
}

function formatDuration(milliseconds: number): string {
  const state = store.getState();
  const userLan = getUserLanguage(state.userDetails.user);

  const units: UnitsMap = {
    en: [
      {
        name: "year",
        multiplier: 31536000,
        lessThanSecond: "less than a second",
      },
      { name: "day", multiplier: 86400, lessThanSecond: "less than a second" },
      { name: "hour", multiplier: 3600, lessThanSecond: "less than a second" },
      { name: "minute", multiplier: 60, lessThanSecond: "less than a second" },
      { name: "second", multiplier: 1, lessThanSecond: "less than a second" },
    ],
    fr: [
      {
        name: "an",
        multiplier: 31536000,
        lessThanSecond: "moins d'une seconde",
      },
      {
        name: "jour",
        multiplier: 86400,
        lessThanSecond: "moins d'une seconde",
      },
      {
        name: "heure",
        multiplier: 3600,
        lessThanSecond: "moins d'une seconde",
      },
      { name: "minute", multiplier: 60, lessThanSecond: "moins d'une seconde" },
      { name: "seconde", multiplier: 1, lessThanSecond: "moins d'une seconde" },
    ],
    de: [
      {
        name: "Jahr",
        multiplier: 31536000,
        lessThanSecond: "less than a second",
      },
      { name: "Tag", multiplier: 86400, lessThanSecond: "less than a second" },
      {
        name: "Stunde",
        multiplier: 3600,
        lessThanSecond: "less than a second",
      },
      { name: "Minute", multiplier: 60, lessThanSecond: "less than a second" },
      { name: "Sekunde", multiplier: 1, lessThanSecond: "less than a second" },
    ],
    es: [
      {
        name: "año",
        multiplier: 31536000,
        lessThanSecond: "less than a second",
      },
      { name: "día", multiplier: 86400, lessThanSecond: "less than a second" },
      { name: "hora", multiplier: 3600, lessThanSecond: "less than a second" },
      { name: "minuto", multiplier: 60, lessThanSecond: "less than a second" },
      { name: "segundo", multiplier: 1, lessThanSecond: "less than a second" },
    ],
    hi: [
      {
        name: "साल",
        multiplier: 31536000,
        lessThanSecond: "less than a second",
      },
      { name: "दिन", multiplier: 86400, lessThanSecond: "less than a second" },
      { name: "घंटा", multiplier: 3600, lessThanSecond: "less than a second" },
      { name: "मिनट", multiplier: 60, lessThanSecond: "less than a second" },
      { name: "सेकंड", multiplier: 1, lessThanSecond: "less than a second" },
    ],
  };

  const selectedUnits: Unit[] = units[userLan] || units.en;

  const totalSeconds: number = Math.floor(milliseconds / 1000);

  for (const unit of selectedUnits) {
    const value: number = Math.floor(totalSeconds / unit.multiplier);
    if (value > 0) {
      const plural: string = value > 1 ? "" : "";
      return `${value} ${unit.name}${plural}`;
    }
  }

  return selectedUnits[4].lessThanSecond;
}

function getTimeDisplay(timestamp: number): string {
  const state = store.getState();
  const userLan = getUserLanguage(state.userDetails.user);

  let timeAgo: TimeAgo;

  if (userLan === "fr") {
    TimeAgo.addLocale(fr);
    timeAgo = new TimeAgo("fr-FR");
  } else if (userLan === "de") {
    TimeAgo.addLocale(de);
    timeAgo = new TimeAgo("de-DE");
  } else if (userLan === "es") {
    TimeAgo.addLocale(es);
    timeAgo = new TimeAgo("es");
  } else if (userLan === "hi") {
    TimeAgo.addLocale(hi);
    timeAgo = new TimeAgo("hi");
  } else {
    TimeAgo.addLocale(en);
    timeAgo = new TimeAgo("en-US");
  }

  const currentTime = Date.now();
  const timeDifference = currentTime - timestamp;
  const oneWeek = 7 * 24 * 60 * 60 * 1000; // Milliseconds in a week

  if (timeDifference < oneWeek) {
    return timeAgo.format(timestamp);
  } else {
    // If more than a week has passed, display the normal time
    const date = new Date(timestamp);
    return date.toLocaleString(userLan);
  }
}

function getUserLanguage(user: { [id: string]: any }) {
  const detectBrowserLanguage = require("detect-browser-language");

  if (user === undefined) {
    if (detectBrowserLanguage != null)
      return detectBrowserLanguage().substring(0, 2);
    else {
      return "en";
    }
  } else {
    if (!isDefined(user.preferences)) {
      return detectBrowserLanguage().substring(0, 2);
    } else if (user.preferences?.language === "auto") {
      return detectBrowserLanguage().substring(0, 2);
    } else {
      return user.preferences?.language;
    }
  }
}

function getLanguageLabel(label: AllLabels) {
  const myLanguages = require("./language").default;
  const detectBrowserLanguage = require("detect-browser-language");
  let userLanguage = localStorage.getItem("bUserLanguage") ?? "auto"; // state.userDetails?.user?.preferences?.language;

  const browserLan = detectBrowserLanguage().substring(0, 2);
  const myLanguageLabel = myLanguages[label] as any;

  if (myLanguageLabel === undefined || myLanguageLabel === null) {
    return "!! " + label;
  } else {
    if (
      userLanguage == "auto" &&
      detectBrowserLanguage != undefined &&
      myLanguageLabel[browserLan] != undefined
    )
      return myLanguageLabel[browserLan];
    else if (myLanguageLabel[userLanguage] === undefined) {
      if (myLanguageLabel[browserLan] != undefined) {
        return myLanguageLabel[browserLan];
      } else return myLanguageLabel["en"]; // return default language if not found
    } else {
      return myLanguageLabel[userLanguage];
    }
  }
}

function TimeCounter({ nudge, poke }: { nudge: string; poke?: any }) {
  const [time, setTime] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setTime((time) => time + 10);
    }, 10);

    if (nudge === "start") {
      setTime(0);
    }
    if (nudge === "stop") {
      clearInterval(interval);
    }

    return () => clearInterval(interval);
  }, [nudge, poke]);

  const hours = Math.floor((time / (1000 * 60 * 60)) % 24);
  const minutes = Math.floor((time / (1000 * 60)) % 60);
  const seconds = Math.floor((time / 1000) % 60);
  const milliseconds = time % 1000;

  let formattedTime = "";

  if (hours > 0) {
    formattedTime = `${hours}h`;

    if (minutes > 0) {
      formattedTime += `${minutes}m`;
    }
  } else if (minutes > 0) {
    formattedTime = `${minutes}m`;

    if (seconds > 0) {
      formattedTime += `${seconds}s`;
    }
  } else if (seconds > 0) {
    formattedTime = `${seconds}s`;

    // if (milliseconds > 0) {
    //   formattedTime += `${milliseconds}ms`;
    // }
  } else {
    formattedTime = `${milliseconds}ms`;
  }

  return <>{formattedTime}</>;
}

function getWeek() {
  const currentDate = new Date();
  const firstDateOfYear = new Date(currentDate.getFullYear(), 0, 1);
  const pastDaysOfYear =
    (currentDate.getTime() - firstDateOfYear.getTime()) / 86400000;
  return Math.ceil((pastDaysOfYear + firstDateOfYear.getDay() + 1) / 7);
}

function getUserDocsLanguage() {
  const state = store.getState();
  const detectBrowserLanguage = require("detect-browser-language");
  let userLanguage = state.userDetails?.user?.preferences?.language;

  const browserLan = detectBrowserLanguage().substring(0, 2);

  if (userLanguage == undefined || userLanguage == null) userLanguage = "";
  else if (userLanguage == "auto")
    if (detectBrowserLanguage != undefined && detectBrowserLanguage != null)
      userLanguage = detectBrowserLanguage().substr(0, 2);
    else userLanguage = "";
  else if (
    userLanguage == "de" ||
    userLanguage == "es" ||
    userLanguage == "hi" ||
    userLanguage == "en"
  ) {
    userLanguage = "";
  }

  if (userLanguage == "fr") {
    userLanguage = "fr" + "/";
  } else if (
    userLanguage == "de" ||
    userLanguage == "es" ||
    userLanguage == "hi" ||
    userLanguage == "en"
  ) {
    userLanguage = "";
  }

  return userLanguage;
}

const openNotification = (
  message: any,
  description: any,
  type: "success" | "error" | "info" | "warning",
  duration: number = 4.5
) => {
  if (type == "success")
    notification.success({
      message: message,
      description: description,
      duration: duration,
    });
  else if (type == "error")
    notification.error({
      message: message,
      description: description,
      style: { backgroundColor: "var(--background-color)" },
      duration: duration,
    });
  else if (type == "info")
    notification.info({
      message: message,
      description: description,
      duration: duration,
    });
  else if (type == "warning")
    notification.warning({
      message: message,
      description: description,
      duration: duration,
    });
  else
    notification.open({
      message: message,
      description: description,
      duration: duration,
    });
};

const copyToClipboard = async (
  text: any,
  setTooltipTitle: any = () => {},
  intialMsg: any = "clickToCopyIntoClipboard"
) => {
  const sleep = (ms: number | undefined) =>
    new Promise((r) => setTimeout(r, ms));
  if (navigator.clipboard) {
    // Use the Clipboard API if available
    navigator.clipboard.writeText(text).catch((error) => {
      console.error("Failed to copy text: ", error);
      return;
    });
  } else {
    // Use the legacy document.execCommand method
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.setAttribute("readonly", "");
    textarea.style.position = "absolute";
    textarea.style.left = "-9999px";
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
  }

  setTooltipTitle(getLanguageLabel("copied"));
  await sleep(1000);

  setTooltipTitle(getLanguageLabel(intialMsg));
};

/**
 * Fetches the theme
 * @param themeName name of theme you want to get
 * @returns
 */
const getColorTheme = (themeName: string) => {
  if (BoslerConfig.colorTheme.hasOwnProperty(themeName)) {
    return BoslerConfig.colorTheme[themeName];
  } else {
    return BoslerConfig.colorTheme.custom;
  }
};

const changeName = (id: string, newName: string) => {
  return axios.get(`/kitab/${id}/${newName}/rename`);
};

const changeDesc = (id: string, newDesc: string) => {
  return axios.get(`/kitab/${id}/${newDesc}/renameDescription`);
};

const blobFileType = (fileName: string, checkType: string) => {
  const excelExtensions = [
    ".xlsx",
    ".xls",
    ".xlsm",
    ".xlsb",
    ".xlt",
    ".xla",
    ".xlam",
    ".xltx",
    ".xltm",
    ".xll",
    ".xlw",
  ];

  const csvExtensions = [".csv"];
  const fileExtension = fileName.substring(fileName.lastIndexOf("."));

  return checkType == "excel"
    ? excelExtensions.includes(fileExtension)
    : checkType == "csv"
    ? csvExtensions.includes(fileExtension)
    : false;
};

function makeDebounceFunction(func: any, delay: number) {
  let timeoutId: any = undefined;

  return function (...args: any) {
    clearTimeout(timeoutId);

    timeoutId = setTimeout(() => {
      func(...args);
    }, delay);
  };
}

const generateKey = (pre: string) => {
  return `${pre}_${new Date().getTime()}`;
};

const getSocketClient = () => {
  const WSUrl: string = isDefined(process.env.REACT_APP_WS_URL)
    ? process.env.REACT_APP_WS_URL
    : process.env.PUBLIC_URL + "/api/ws";

  return new Client({
    webSocketFactory: () => new SockJS(WSUrl),
    // brokerURL: WSUrl,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    // debug: (text) =>
  });
};

const getSQLFormatLink = () => {
  return process.env.NODE_ENV === "production"
    ? location.protocol === "https:"
      ? "https://" + location.host
      : "http://" + location.host
    : "http://staging.bosler.io";
};

const isIpPlatform = () => {
  const currentURL = window.location.href;
  const ipAddressPattern = /^(http|https):\/\/\d+\.\d+\.\d+\.\d+/;

  return ipAddressPattern.test(currentURL);
};

export const fuzzyFilter = (t: string, s: string) => {
  var hay = t.toLowerCase(),
    i = 0,
    n = -1,
    l;
  s = s.toLowerCase();
  for (; (l = s[i++]); ) if (!~(n = hay.indexOf(l, n + 1))) return false;
  return true;
};

export const IsUUID = (id: any) => {
  const stringId = String(id);
  return isDefined(
    stringId.match(
      /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/
    )
  );
};

function encodeToBase64(inputString: string) {
  const encoder = new TextEncoder();
  const bytes = encoder.encode(inputString);
  const base64String = btoa(String.fromCharCode(...bytes));
  return base64String;
}

function decodeFromBase64(base64String: string) {
  const bytes = Uint8Array.from(atob(base64String), (c) => c.charCodeAt(0));
  const decoder = new TextDecoder("utf-8");
  const decodedString = decoder.decode(bytes);
  return decodedString;
}

function isBase64(str: string): boolean {
  try {
    return btoa(atob(str)) === str;
  } catch (error) {
    return false;
  }
}

export const getStringWithAllowedChars = (str: string) => {
  return str.replace(/[\s~`!@#$%^&*+=\-[\]\\';,/{}|\\":<>?()_]/g, "_");
};

function hardRefresh() {
  window.location.href =
    window.location.href + "?nocache=" + new Date().getTime();
  window.location.replace(window.location.href);
  for (let i = 0; i < 5; i++) {
    window.location.reload();
  }

  caches.keys().then(function (names) {
    for (let name of names) caches.delete(name);
  });
}

function convertDateToLocalDate(value: Dayjs) {
  const dateString = value.format("YYYY,MM,DD");
  const LocalDate = dateString.split(",").map((part) => parseInt(part, 10));
  return LocalDate;
}

function getRandomNumber() {
  const randomNumber = Math.random();
  const scaledNumber = Math.floor(randomNumber * 91) + 10; // 91 because (100 - 10 + 1)
  return scaledNumber;
}

function getYesterdayDate() {
  return new Date(new Date().setDate(new Date().getDate() - 1)).valueOf();
}

function getNextCronOccurenceDate(
  cronExpression: string,
  lastExecutionTime: number
) {
  const interval = cronParser.parseExpression(cronExpression, {
    currentDate: new Date(lastExecutionTime),
  });

  return interval.next().toDate().valueOf();
}

export const isReactAppDevelopment = () => {
  return process.env.NODE_ENV != "production";
};

export const isPlatformExpiringIn30Days = (expiresOn: Date) => {
  const currentDate = new Date();
  const differenceInMs = expiresOn.valueOf() - currentDate.valueOf();

  // Convert milliseconds to days
  const differenceInDays = differenceInMs / (1000 * 60 * 60 * 24);

  // Check if the difference is less than 30 days
  return differenceInDays <= 30;
};

export const globalSearch = (
  searchInput: $TSFixMe,
  data: $TSFixMe,
  columns: $TSFixMe
) => {
  const filteredData = data.filter((value: $TSFixMe) => {
    let rows = false;
    columns !== undefined &&
      columns.forEach(({ dataIndex }: $TSFixMe) => {
        const obj = value[dataIndex];

        if (obj !== undefined && obj !== null) {
          rows =
            rows ||
            obj.toString().toLowerCase().includes(searchInput.toLowerCase());
          if (rows) {
            return true;
          }
        }
      });

    return rows;
  });

  return filteredData;
};

export {
  getLastClassOfUUID,
  TimeCounter,
  blobFileType,
  bytesToHumanReadableSize,
  capitalizeFirstLetter,
  changeDesc,
  changeName,
  closeTab,
  convertDateToLocalDate,
  copyToClipboard,
  decodeFromBase64,
  encodeToBase64,
  formatDuration,
  formatTime,
  generateKey,
  getColorTheme,
  getLanguageLabel,
  getRandomNumber,
  getSQLFormatLink,
  getSocketClient,
  getTheme,
  getTimeDisplay,
  getUserDocsLanguage,
  getUserLanguage,
  hardRefresh,
  isBase64,
  isDefined,
  isEmpty,
  isFloat,
  isInt,
  isIpPlatform,
  makeDebounceFunction,
  notEmpty,
  numbersToHumanReadable,
  openNotification,
  setTheme,
  text_truncate,
  timeConverter,
  isCurrentConfigThemeDark,
  userOSkey,
  getYesterdayDate,
  getNextCronOccurenceDate,
  ObjectKeys,
};
