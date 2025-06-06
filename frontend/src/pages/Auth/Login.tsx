import { Form } from "antd";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
// import { setTheme } from "../App";
import React from "react";
import {
  convertStringToLowerCase,
  isIpPlatform,
  openNotification,
  setTheme,
} from "utils/utilities";
import { refreshTokenStatus } from "../../redux/actions/tokenActions";
import { login } from "../../redux/actions/userActions";
import { ThunkAppDispatch } from "../../redux/types/store";
import Loading from "../Errors/Loading";

import "Apps/Kepler/dashboard/DashboardSubscribeMenu/DashboardSubscribeMenu.scss";

import { API_BASE_URL } from "Authentication/constants";
import { BoslerIcon } from "assets/icons/boslerMiscellaneousIcons";
import MfaVerification from "pages/Settings/components/MfaEnabled";
import { ParticleApp } from "utils/ParticleApp";
import LoginModal from "./LoginModal";

const Login = () => {
  const [form] = Form.useForm();
  const [userDetails, setUserDetails] = useState<{
    userName: string;
    password: string;
  }>({ userName: "", password: "" });
  const [loginWithRecoveryCode, setLoginWithRecoveryCode] = useState(false);
  const [showMFA, setShowMFA] = useState<boolean>(false);
  const dispatch = useDispatch<ThunkAppDispatch>();
  const navigate = useNavigate();

  const { userInfo, error, loading } = useSelector(
    (state) => (state as $TSFixMe).userLogin
  );
  const { isTokenValid, loading: tokenStatusLoading } = useSelector(
    (state) => (state as $TSFixMe).tokenStatus
  );

  useEffect(() => {
    if (!tokenStatusLoading && isTokenValid) {
      navigate(-1);
    }
  }, [userInfo, loading, error, isTokenValid, tokenStatusLoading]);

  useEffect(() => {
    if (!loading && error) {
      let toast_msg = "";
      if (error.response !== undefined) {
        toast_msg = "Wrong username or password";
      } else {
        toast_msg = error.message + " from our side";
      }

      openNotification(
        "Login Error",
        "There was a login error, please check details.",
        "error"
      );
      // message.error(toast_msg);
    }
  }, [error]);

  if (tokenStatusLoading) return <Loading />;

  return (
    <>
      {/* <DancingLines lineStroke={"#738091"} /> */}

      <div
        className="login-container"
        style={{
          position: "fixed",
          top: 0,
          left: 0,
          width: "100%",
          height: "100%",
          background: "var(--background-color)",
        }}
      >
        <div
          style={{
            background: "var(--background-color)",
            position: "fixed",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
          }}
        >
          <ParticleApp
            fullScreen={true}
            height={1300}
            width={1300}
            numberOfParticles={50}
            speed={2}
            logoSize={8}
            image="/logo_hexa.svg"
          />
        </div>
        <div
          className="login-containerNew"
          style={{
            zIndex: 10,
          }}
        >
          {showMFA ? (
            <div className="login-containerNew">
              {!isIpPlatform() && <BoslerIcon size={128} />}
              <br />
              <div style={{ minWidth: "500px" }} className="form-containerNew">
                <MfaVerification
                  username={userDetails.userName}
                  password={userDetails.password}
                  showMFA={showMFA}
                  loginWithRecoveryCode={loginWithRecoveryCode}
                  setLoginWithRecoverycode={() =>
                    setLoginWithRecoveryCode(!loginWithRecoveryCode)
                  }
                />
              </div>
            </div>
          ) : (
            <Form
              form={form}
              layout="vertical"
              initialValues={{}}
              onFinish={(values) => {
                if (values.signInOption !== "password") {
                  window.location.href =
                    API_BASE_URL?.replace("/api", "") +
                    "/saml2/authenticate/" +
                    values.signInOption;
                } else if (values.username && values.password) {
                  setUserDetails({
                    userName: values.username,
                    password: values.password,
                  });
                  dispatch(
                    login(
                      convertStringToLowerCase(values.username),
                      values.password,
                      () => {
                        navigate("/portal/home");
                        dispatch(refreshTokenStatus());
                        setShowMFA(false);
                      },
                      (error: string) => {
                        openNotification("Login Error", error, "error");
                      },
                      () => {
                        setShowMFA(true);
                      }
                    )
                  ).then((data: $TSFixMe) => {
                    dispatch(refreshTokenStatus());
                    setTheme(data);
                  });
                }
              }}
              className="login-containerNew"
            >
              {!isIpPlatform() && <BoslerIcon size={128} />}
              <br />
              <LoginModal showMFA={showMFA} form={form} />
            </Form>
          )}
        </div>
      </div>
    </>
  );
};

export default Login;
