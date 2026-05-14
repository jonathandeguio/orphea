import React, { useEffect } from "react";
import { RouterProvider } from "react-router-dom";
import "../../App.scss";
import { RootState } from "redux/types/store";

import "@fontsource/ibm-plex-mono";
import "@fontsource/ibm-plex-sans";
import { ConfigProvider, theme } from "antd";
import { HotkeysProvider } from "react-hotkeys-hook";
import { useSelector } from "react-redux";
import { isCurrentConfigThemeDark, setTheme } from "utils/utilities";
import OrpheaLoader from "components/orpheaLoader";
import { useThemeDetector } from "hooks/useThemeDetector";
import { useRouter } from "routes/Routes";

function App() {
  const isPreferedModeDark = useThemeDetector();
  const { user } = useSelector((state: RootState) => state.userDetails);

  const router = useRouter();

  useEffect(() => {
    setTheme(user);
  }, [user, isPreferedModeDark]);

  return (
    <ConfigProvider
      theme={{
        algorithm: isCurrentConfigThemeDark(user)
          ? theme.darkAlgorithm
          : theme.defaultAlgorithm,
        token: {
          colorPrimary: "#24527a",
          fontSize: 14,
          fontWeightStrong: 500,
          // lineHeight: 1.2,
          wireframe: false,
          borderRadius: 4,
          fontFamily:
            '"IBM Plex Sans", system-ui, -apple-system, BlinkMacSystemFont, sans-serif;',
          // fontFamilyCode:
          //   "Space Mono, Monaco, Roboto, Oxygen-Sans, Ubuntu, Cantarell",
        },
      }}
    >
      <React.Suspense fallback={<OrpheaLoader type="fallback" />}>
        <HotkeysProvider>
          <RouterProvider router={router} />
        </HotkeysProvider>
      </React.Suspense>
    </ConfigProvider>
  );
}

export default App;
