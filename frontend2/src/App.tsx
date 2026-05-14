import React, { useEffect } from 'react';
import axios from "axios";
import { useSelector, useDispatch } from "react-redux";
import { Route, Routes, useMatch, useNavigate } from "react-router-dom";
import { useMediaQuery } from '@mui/material';

import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';

import { getUserDetails } from "./redux/actions/userActions";

import { closeTab } from "./utils/utilities";
import { RootState, ThunkAppDispatch } from "./redux/types/store";
import { setnetworkErrorFalse, setnetworkErrorTrue } from "./redux/actions/authActions";
import { setTokenInvalid, setTokenValid } from "./redux/actions/tokenActions";
import PrivateOutlet from './auth/PrivateOutlet';
import Login from './auth/Login';
import LoginError from './auth/LoginError';
import OAuth2RedirectHandler from './auth/OAuth2RedirectHandler';
import Notfound from './utils/Notfound';
import NetworkError from './utils/NetworkError';
import "./App.scss";


function App() {

  const navigate = useNavigate();
  const dispatch = useDispatch<ThunkAppDispatch>();

  const { networkError } = useSelector((state: RootState) => state.networkError);

  const { isTokenValid, loading: tokenStatusLoading } = useSelector(
    (state) => (state as any).tokenStatus
  );

  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');

  const theme = React.useMemo(
    () =>
      createTheme({
        palette: {
          mode: prefersDarkMode ? 'dark' : 'light',
        },
      }),
    [prefersDarkMode],
  );

  useEffect(() => {
    Ping();
    const timer = setInterval(function () {
      Ping();
    }, 10000);

    if (!tokenStatusLoading && isTokenValid) {
      dispatch(getUserDetails());
      if (localStorage.getItem("tokenRefresh") === "0") {
        localStorage.removeItem("tokenRefresh");
        localStorage.setItem("login-refresh", "0");
        closeTab();
      }
    }
    return () => {
      clearInterval(timer);
    };
  }, [isTokenValid, networkError, tokenStatusLoading]);

  async function Ping() {
    const BASE_URL = process.env.REACT_APP_BASE_URL_API;
    const orpheaToken = localStorage.getItem("orpheaToken");

    const config = {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${orpheaToken}`,
      },
    };

    try {
      const { data } = await axios.get(`${BASE_URL}/ping`, config);

      if (data.message === "pong") {
        if (networkError === true) {
          dispatch(setnetworkErrorFalse())
          navigate(-1);
        }

        if (tokenStatusLoading || !isTokenValid) {
          dispatch(setTokenValid());
        }
      }
    } catch (error) {
      if ((error as any).code === "ERR_NETWORK") {
        if (networkError === false) {
          dispatch(setnetworkErrorTrue())
          navigate("/NetworkError");
        }
      } else if (
        (error as { [id: string]: any }).response?.data.error === "Unauthorized"
      ) {
        if (tokenStatusLoading || isTokenValid) {
          dispatch(setTokenInvalid());
        }
        if (networkError) {
          dispatch(setnetworkErrorFalse());
          // while (useMatch("/NetworkError")) {
            // navigate(-1);
          // }
        }
      }
    }
  }


  return (
    <>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Routes>
        {/* Each route capsulated inside MainLayout is protected */}
        {/* User with invalid token will be redirected to login page */}
        <Route path="/" element={<PrivateOutlet />}></Route>



        <Route path="/login" element={<Login />} />
        <Route path="/relogin" element={<LoginError />} />
        <Route path="/NetworkError" element={<NetworkError />} />
        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
        <Route path="*" element={<Notfound />} />

        </Routes>
      </ThemeProvider>
    </>
  );
}

export default App;
