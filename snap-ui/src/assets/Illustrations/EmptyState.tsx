import React from "react";
interface TProps {
  size?: string;
}
export function SearchEmptyState({ size }: TProps) {
  if (!size) {
    size = "48px";
  }
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={size}
      height={size}
      viewBox="0 0 120 120"
      fill="none"
    >
      <g clipPath="url(#clip0_1_186)">
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M60 120C93.1371 120 120 93.1371 120 60C120 26.8629 93.1371 0 60 0C45.133 0 39.4818 17.8318 29 26.7868C16.1188 37.7917 0 41.7299 0 60C0 93.1371 26.8629 120 60 120Z"
          fill="#EEF0F6"
        />
        <g filter="url(#filter0_f_1_186)">
          <path
            d="M92 40H40C38.3431 40 37 41.1485 37 42.5652V96.4348C37 97.8515 38.3431 99 40 99H92C93.6569 99 95 97.8515 95 96.4348V42.5652C95 41.1485 93.6569 40 92 40Z"
            fill="white"
          />
          <path
            d="M92 40H40C38.3431 40 37 41.1485 37 42.5652V96.4348C37 97.8515 38.3431 99 40 99H92C93.6569 99 95 97.8515 95 96.4348V42.5652C95 41.1485 93.6569 40 92 40Z"
            fill="#E1E5EF"
          />
        </g>
        <path
          d="M35 40C35 35.5817 38.5817 32 43 32H81C85.4183 32 89 35.5817 89 40V83C89 87.4183 85.4183 91 81 91H43C38.5817 91 35 87.4183 35 83V40Z"
          fill="white"
        />
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M84.6609 87.8685C85.6929 86.9512 85.7858 85.3711 84.8686 84.3391L76.8686 75.3391C75.9513 74.3071 74.3711 74.2142 73.3391 75.1315C72.3072 76.0488 72.2142 77.629 73.1315 78.6609L81.1315 87.6609C82.0488 88.6929 83.629 88.7858 84.6609 87.8685Z"
          fill="#676C93"
        />
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M66 54.5C60.201 54.5 55.5 59.201 55.5 65C55.5 70.799 60.201 75.5 66 75.5C71.799 75.5 76.5 70.799 76.5 65C76.5 59.201 71.799 54.5 66 54.5ZM50.5 65C50.5 56.4396 57.4396 49.5 66 49.5C74.5604 49.5 81.5 56.4396 81.5 65C81.5 73.5604 74.5604 80.5 66 80.5C57.4396 80.5 50.5 73.5604 50.5 65Z"
          fill="#676C93"
        />
      </g>
      <defs>
        <filter
          id="filter0_f_1_186"
          x="26"
          y="29"
          width="80"
          height="81"
          filterUnits="userSpaceOnUse"
          colorInterpolationFilters="sRGB"
        >
          <feFlood floodOpacity="0" result="BackgroundImageFix" />
          <feBlend
            mode="normal"
            in="SourceGraphic"
            in2="BackgroundImageFix"
            result="shape"
          />
          <feGaussianBlur
            stdDeviation="5.5"
            result="effect1_foregroundBlur_1_186"
          />
        </filter>
        <clipPath id="clip0_1_186">
          <rect width="120" height="120" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
}

export function WarningState({ size = "48px" }: TProps) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={size}
      height={size}
      viewBox="0 0 180 180"
      fill="none"
    >
      <g clipPath="url(#clip0_106_76)">
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M90 180C139.706 180 180 139.706 180 90C180 40.2944 139.706 0 90 0C67.6996 0 59.2227 26.7477 43.5 40.1802C24.1782 56.6875 0 62.5948 0 90C0 139.706 40.2944 180 90 180Z"
          fill="#EEF0F6"
        />
        <g filter="url(#filter0_f_106_76)">
          <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M91.5 165C126.294 165 154.5 136.794 154.5 102C154.5 67.2061 126.294 39 91.5 39C75.8897 39 69.9559 57.7234 58.95 67.1261C45.4247 78.6812 28.5 82.8164 28.5 102C28.5 136.794 56.7061 165 91.5 165Z"
            fill="#E1E5EF"
          />
        </g>
        <path
          d="M91.5 147C120.495 147 144 123.495 144 94.5C144 65.5051 120.495 42 91.5 42C62.5051 42 39 65.5051 39 94.5C39 123.495 62.5051 147 91.5 147Z"
          fill="white"
        />
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M92 59.5C94.7188 59.5 97.4903 61.5196 97.5 63.5L95.3125 108.295C95.3016 110.5 94 112 91.5 112C89 112 87.6903 110.275 87.6806 108.295L85.5101 63.3493C85.5003 61.3689 88.3437 59.5001 92 59.5Z"
          fill="#676C93"
        />
        <path
          d="M92 128C95.3137 128 98 125.314 98 122C98 118.686 95.3137 116 92 116C88.6863 116 86 118.686 86 122C86 125.314 88.6863 128 92 128Z"
          fill="#676C93"
        />
      </g>
      <defs>
        <filter
          id="filter0_f_106_76"
          x="12"
          y="22.5"
          width="159"
          height="159"
          filterUnits="userSpaceOnUse"
          colorInterpolationFilters="sRGB"
        >
          <feFlood floodOpacity="0" result="BackgroundImageFix" />
          <feBlend
            mode="normal"
            in="SourceGraphic"
            in2="BackgroundImageFix"
            result="shape"
          />
          <feGaussianBlur
            stdDeviation="8.25"
            result="effect1_foregroundBlur_106_76"
          />
        </filter>
        <clipPath id="clip0_106_76">
          <rect width="180" height="180" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
}

export function CommentState({ size = "48px" }: TProps) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={size}
      height={size}
      viewBox="0 0 180 180"
      fill="none"
    >
      <g clipPath="url(#clip0_1_260)">
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M90 180C139.706 180 180 139.706 180 90C180 40.2944 139.706 0 90 0C67.6996 0 59.2227 26.7477 43.5 40.1802C24.1782 56.6875 0 62.5948 0 90C0 139.706 40.2944 180 90 180Z"
          fill="#EEF0F6"
        />
        <g filter="url(#filter0_f_1_260)">
          <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M96 151.5C124.995 151.5 148.5 131.689 148.5 107.25C148.5 82.8114 124.995 63 96 63C82.9914 63 78.0466 76.151 68.875 82.7553C57.604 90.8713 43.5 93.7758 43.5 107.25C43.5 131.689 67.0051 151.5 96 151.5Z"
            fill="#E1E5EF"
          />
        </g>
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M124.412 88.5H97.5882C89.2552 88.5 82.5 95.2136 82.5 103.495V110.802C82.5 119.084 89.2552 125.797 97.5882 125.797H125.907L134.575 132.825C135.866 133.872 137.767 133.68 138.821 132.397C139.26 131.861 139.5 131.192 139.5 130.501V103.495C139.5 95.2136 132.745 88.5 124.412 88.5Z"
          fill="#676C93"
        />
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M61.7414 58.5H110.759C122.49 58.5 132 68.1127 132 79.9705V99.2669C132 111.125 122.49 120.737 110.759 120.737H62.3207L48.541 132.351C46.4617 134.103 43.3707 133.82 41.637 131.718C40.9023 130.828 40.5 129.705 40.5 128.545V79.9705C40.5 68.1127 50.0101 58.5 61.7414 58.5Z"
          fill="white"
        />
      </g>
      <defs>
        <filter
          id="filter0_f_1_260"
          x="27"
          y="46.5"
          width="138"
          height="121.5"
          filterUnits="userSpaceOnUse"
          colorInterpolationFilters="sRGB"
        >
          <feFlood floodOpacity="0" result="BackgroundImageFix" />
          <feBlend
            mode="normal"
            in="SourceGraphic"
            in2="BackgroundImageFix"
            result="shape"
          />
          <feGaussianBlur
            stdDeviation="8.25"
            result="effect1_foregroundBlur_1_260"
          />
        </filter>
        <clipPath id="clip0_1_260">
          <rect width="180" height="180" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
}
