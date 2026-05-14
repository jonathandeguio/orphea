import React from "react";
import { TOrpheaIconProps } from "./types";

function GithubIcon({
  size = 16, // or any default size of your choice
  color = "#717a94", // or any color of your choice
  style,
}: TOrpheaIconProps) {
  return (
    <div className="orphea-icons" style={style}>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        color={color}
        fill={color}
        height={size}
        width={size}
        viewBox="0 0 24 24"
      >
        <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" />
      </svg>
    </div>
  );
}

function MarkdownIcon({
  size = 16, // or any default size of your choice
  color = "#717a94", // or any color of your choice
  style,
}: TOrpheaIconProps) {
  return (
    <div className="orphea-icons" style={style}>
      <svg
        color={color}
        data-icon="markdown"
        fill={color}
        height={size}
        viewBox="0 0 16 16"
        width={size}
      >
        <desc>dynamic</desc>
        <g strokeWidth="1">
          <g transform="">
            <path
              clipRule="evenodd"
              d="M0 11.74c0 .879.72 1.593 1.605 1.593h12.79c.885 0 1.605-.714 1.605-1.593V4.26c0-.879-.72-1.593-1.605-1.593H1.605C.72 2.667 0 3.38 0 4.26v7.478Zm1-7.48a.6.6 0 0 1 .605-.593h12.79A.6.6 0 0 1 15 4.26v7.479a.6.6 0 0 1-.605.594H1.605A.6.6 0 0 1 1 11.74V4.26Zm10.685 6.407H11.667a.5.5 0 0 1-.407-.21L9.5 8.873a.5.5 0 0 1 .669-.744l.999.899V5.833a.5.5 0 0 1 1 0v3.213l.996-.915a.501.501 0 0 1 .676.738l-1.816 1.666a.5.5 0 0 1-.338.132Zm-4.185 0a.5.5 0 0 1-.5-.5V7.136l-1.628 1.81a.516.516 0 0 1-.743 0L3 7.136v3.03a.5.5 0 0 1-1 0V5.834a.5.5 0 0 1 .872-.334L5 7.864l2.128-2.365A.5.5 0 0 1 8 5.833v4.334a.5.5 0 0 1-.5.5Z"
              fill="currentColor"
              fillRule="evenodd"
            ></path>
          </g>
        </g>
      </svg>
    </div>
  );
}

function StylesIcon({
  size = 16, // or any default size of your choice
  color = "#717a94", // or any color of your choice
  style,
}: TOrpheaIconProps) {
  return (
    <div className="orphea-icons" style={style}>
      <svg
        color={color}
        data-icon="styles-icon"
        fill={color}
        height={size}
        viewBox="0 0 16 16"
        width={size}
      >
        <desc>dynamic</desc>
        <g strokeWidth="1">
          <g transform="">
            <path
              clipRule="evenodd"
              d="M4 8.21h-.06c-1.37 0-2.57.91-2.95 2.23l-.97 3.42c-.07.27.08.55.35.62.14.04.29.02.41-.06l.51-.34a1.58 1.58 0 0 1 1.57-.12c.35.17.73.25 1.12.25h.08c1.62 0 2.94-1.32 2.94-2.94v-.06c0-1.65-1.34-3-3-3Zm.06 5h-.08c-.24 0-.47-.05-.69-.15-.62-.3-1.33-.33-1.97-.1l.64-2.25c.25-.88 1.06-1.5 1.98-1.5H4a2 2 0 0 1 2 2v.06c0 1.07-.87 1.94-1.94 1.94ZM14.76.07a.515.515 0 0 0-.51 0l-9.5 5.5a.491.491 0 0 0-.1.78l4 4c.19.2.51.2.71 0 .03-.03.05-.06.08-.1l5.5-9.5a.5.5 0 0 0-.18-.68ZM8.9 9.19 5.81 6.1l1.64-.95 2.4 2.4-.95 1.64Zm1.46-2.54L8.35 4.64l4.79-2.77-2.78 4.78Z"
              fill="currentColor"
              fillRule="evenodd"
            ></path>
          </g>
        </g>
      </svg>
    </div>
  );
}

function TextIcon({
  size = 16, // or any default size of your choice
  color = "#717a94", // or any color of your choice
  style,
}: TOrpheaIconProps) {
  return (
    <div className="orphea-icons" style={style}>
      <svg
        color={color}
        data-icon="text"
        fill={color}
        height={size}
        viewBox="0 0 16 16"
        width={size}
      >
        <desc>dynamic</desc>
        <g strokeWidth="1">
          <g transform="">
            <path
              clipRule="evenodd"
              d="M1 1H15V5.23333H14V2H9V14H12V15H4V14H7V2H2V5.23333H1V1Z"
              fill="currentColor"
              fillRule="evenodd"
            ></path>
          </g>
        </g>
      </svg>
    </div>
  );
}

function CodeCellIcon({
  size = 16, // or any default size of your choice
  color = "#717a94", // or any color of your choice
  style,
}: TOrpheaIconProps) {
  return (
    <div className="orphea-icons" style={style}>
      <svg
        color={color}
        data-icon="code-cell-icon"
        fill={color}
        height={size}
        viewBox="0 0 16 16"
        width={size}
      >
        <desc>dynamic</desc>
        <g strokeWidth="1">
          <g transform="">
            <path
              clipRule="evenodd"
              d="M6.302 15.147 8.712.688l.986.165-2.41 14.459-.986-.165ZM5.092 4.385 1.478 8l3.614 3.615-.707.707L.063 8l4.322-4.322.707.707Zm6.523-.707L15.937 8l-4.322 4.322-.707-.707L14.522 8l-3.614-3.615.707-.707Z"
              fill="currentColor"
              fillRule="evenodd"
            ></path>
          </g>
        </g>
      </svg>
    </div>
  );
}

function CutIcon({
  size = 16, // or any default size of your choice
  color = "#717a94", // or any color of your choice
  style,
}: TOrpheaIconProps) {
  return (
    <div className="orphea-icons" style={style}>
      <svg
        data-icon="scissors"
        fill={color}
        height={size}
        viewBox="0 0 16 16"
        width={size}
      >
        <desc>dynamic</desc>
        <g strokeWidth="1">
          <g transform="">
            <path
              clipRule="evenodd"
              d="M5.847 5.365A2.798 2.798 0 0 0 6.395 3.7c0-1.625-1.38-2.9-3.027-2.9C1.722.8.342 2.075.342 3.7s1.38 2.9 3.026 2.9c.651 0 1.26-.199 1.759-.54L7.695 8.5l-2.568 2.44a3.111 3.111 0 0 0-1.759-.54c-1.646 0-3.026 1.274-3.026 2.9 0 1.625 1.38 2.9 3.026 2.9 1.647 0 3.027-1.274 3.027-2.9 0-.624-.204-1.197-.548-1.665l9.655-9.172a.5.5 0 1 0-.689-.725L8.421 7.81 5.847 5.365ZM3.368 1.8c-1.143 0-2.026.875-2.026 1.9s.883 1.9 2.026 1.9a2.08 2.08 0 0 0 1.467-.59c.35-.344.56-.809.56-1.31 0-1.025-.883-1.9-2.027-1.9Zm1.416 10.14a.793.793 0 0 0 .051.05c.35.344.56.81.56 1.31 0 1.025-.883 1.9-2.027 1.9-1.143 0-2.026-.874-2.026-1.9 0-1.026.883-1.9 2.026-1.9a2.08 2.08 0 0 1 1.416.54Z"
              fill="currentColor"
              fillRule="evenodd"
            ></path>
            <path
              clipRule="evenodd"
              d="M10.138 10.14a.5.5 0 0 1 .707-.019l4.657 4.416a.5.5 0 0 1-.688.726l-4.657-4.416a.5.5 0 0 1-.019-.707Z"
              fill="currentColor"
              fillRule="evenodd"
            ></path>
          </g>
        </g>
      </svg>
    </div>
  );
}

function PasteIcon({
  size = 16, // or any default size of your choice
  color = "#717a94", // or any color of your choice
  style,
}: TOrpheaIconProps) {
  return (
    <div className="orphea-icons" style={style}>
      <svg
        color={color}
        data-icon="copy"
        fill={color}
        height={size}
        viewBox="0 0 502 502"
        width={size}
      >
        <g>
          <g>
            <g>
              <path
                d="M467.35,190.176l-70.468-70.468c-1.876-1.875-4.419-2.929-7.071-2.929h-23.089V49c0-5.523-4.478-10-10-10h-115v-2.41
              c0-20.176-16.414-36.59-36.59-36.59h-11.819c-20.176,0-36.591,16.415-36.591,36.59V39h-115c-5.522,0-10,4.477-10,10v386
              c0,5.523,4.478,10,10,10h146.386v47c0,5.523,4.478,10,10,10h262.171c5.522,0,10-4.477,10-10V197.247
              C470.279,194.595,469.225,192.051,467.35,190.176z M399.811,150.921l36.326,36.326h-36.326V150.921z M144.721,59h47
              c5.522,0,10-4.477,10-10s-4.478-10-10-10h-15v-2.41c0-9.148,7.442-16.59,16.591-16.59h11.819c9.147,0,16.59,7.442,16.59,16.59V49
              c0,5.523,4.478,10,10,10h22v20h-109V59z M198.107,116.779c-5.522,0-10,4.477-10,10V425H51.721V59h73v30c0,5.523,4.478,10,10,10
              h129c5.522,0,10-4.477,10-10V59h73v57.779H198.107z M450.278,482H208.107V136.779H379.81v60.468c0,5.523,4.478,10,10,10h60.468
              V482z"
              />
              <path
                d="M243.949,253.468h125.402c5.522,0,10-4.477,10-10c0-5.523-4.478-10-10-10H243.949c-5.522,0-10,4.477-10,10
              C233.949,248.991,238.427,253.468,243.949,253.468z"
              />
              <path
                d="M414.437,283.478H243.949c-5.522,0-10,4.477-10,10s4.478,10,10,10h170.487c5.522,0,10-4.477,10-10
              S419.959,283.478,414.437,283.478z"
              />
              <path
                d="M414.437,333.487H243.949c-5.522,0-10,4.477-10,10s4.478,10,10,10h170.487c5.522,0,10-4.477,10-10
              S419.959,333.487,414.437,333.487z"
              />
              <path
                d="M414.437,383.497H243.949c-5.522,0-10,4.477-10,10s4.478,10,10,10h170.487c5.522,0,10-4.477,10-10
              S419.959,383.497,414.437,383.497z"
              />
              <path
                d="M397.767,253.468h16.67c5.522,0,10-4.477,10-10c0-5.523-4.478-10-10-10h-16.67c-5.522,0-10,4.477-10,10
              C387.767,248.991,392.245,253.468,397.767,253.468z"
              />
            </g>
          </g>
        </g>
      </svg>
    </div>
  );
}
function CopyIcon({
  size = 16, // or any default size of your choice
  color = "#717a94", // or any color of your choice
  style,
}: TOrpheaIconProps) {
  return (
    <div className="orphea-icons" style={style}>
      <svg
        color={color}
        data-icon="copy"
        fill={color}
        height={size}
        viewBox="0 0 16 16"
        width={size}
      >
        <desc>dynamic</desc>
        <g strokeWidth="1">
          <g transform="">
            <path
              clipRule="evenodd"
              d="M5 1.242h6v1h3v11.5a1.5 1.5 0 0 1-1.5 1.5h-9a1.5 1.5 0 0 1-1.5-1.5v-11.5h3v-1Zm1 1v1.5a.5.5 0 0 0 .5.5h3a.5.5 0 0 0 .5-.5v-1.5H6Zm-1 1.5v-.5H3v10.5a.5.5 0 0 0 .5.5h9a.5.5 0 0 0 .5-.5v-10.5h-2v.5a1.5 1.5 0 0 1-1.5 1.5h-3a1.5 1.5 0 0 1-1.5-1.5Z"
              fill="currentColor"
              fillRule="evenodd"
            ></path>
          </g>
        </g>
      </svg>
    </div>
  );
}

function EditIcon({
  size = 16, // or any default size of your choice
  color = "#717a94", // or any color of your choice
  style,
}: TOrpheaIconProps) {
  return (
    <div className="orphea-icons" style={style}>
      <svg
        color={color}
        data-icon="edit"
        fill={color}
        height={size}
        viewBox="0 0 16 16"
        width={size}
      >
        <desc>dynamic</desc>
        <g strokeWidth="1">
          <g transform="">
            <path
              d="M0.500002 9.50089L0.146001 9.14689L0 9.29389V9.50089H0.500002ZM9.50004 0.500894L9.85404 0.146894C9.80759 0.100331 9.75242 0.0633877 9.69167 0.0381812C9.63093 0.0129748 9.56581 0 9.50004 0C9.43427 0 9.36915 0.0129748 9.3084 0.0381812C9.24766 0.0633877 9.19248 0.100331 9.14604 0.146894L9.50004 0.500894ZM14.5001 5.50089L14.8541 5.85489C14.9006 5.80845 14.9376 5.75327 14.9628 5.69253C14.988 5.63178 15.001 5.56666 15.001 5.50089C15.001 5.43513 14.988 5.37 14.9628 5.30926C14.9376 5.24851 14.9006 5.19334 14.8541 5.14689L14.5001 5.50089ZM5.50002 14.5009V15.0009H5.70702L5.85402 14.8549L5.50002 14.5009ZM0.500002 14.5009H0C0 14.6335 0.0526786 14.7607 0.146447 14.8544C0.240216 14.9482 0.367393 15.0009 0.500002 15.0009V14.5009ZM0.854003 9.85489L9.85404 0.854894L9.14604 0.146894L0.146001 9.14689L0.854003 9.85489ZM9.14604 0.854894L14.1461 5.85489L14.8541 5.14689L9.85404 0.146894L9.14604 0.854894ZM14.1461 5.14689L5.14602 14.1469L5.85402 14.8549L14.8541 5.85489L14.1461 5.14689ZM5.50002 14.0009H0.500002V15.0009H5.50002V14.0009ZM1 14.5009V9.50089H0V14.5009H1ZM6.14602 3.85489L11.146 8.85489L11.854 8.14689L6.85403 3.14689L6.14602 3.85489ZM8.00003 15.0009H15.0001V14.0009H8.00003V15.0009Z"
              fill="currentColor"
            ></path>
          </g>
        </g>
      </svg>
    </div>
  );
}

export {
  CodeCellIcon,
  CopyIcon,
  CutIcon,
  EditIcon,
  MarkdownIcon,
  StylesIcon,
  TextIcon,
  PasteIcon,
  GithubIcon,
};
