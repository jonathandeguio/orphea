import { Form } from "antd";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { ThunkAppDispatch } from "redux/types/store";
import { isIpPlatform, openNotification, setTheme } from "utils/utilities";
import { refreshTokenStatus } from "redux/actions/tokenActions";
import React from "react";
import { login } from "redux/actions/userActions";
import Loading from "errors/Loading";

import { MoveToDataIcon } from "assets/icons/movetodataMiscellaneousIcons";
import { ParticleApp } from "utils/ParticleApp";
import LoginModal from "./LoginModal";
import { useNavigate } from "react-router";
import MfaVerification from "apps/settings/mfaEnabled";

const Login = () => {
  const [form] = Form.useForm();
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [credentials, setCredentials] = useState({
    username: "",
    password: "",
  });
  // State to control the component swap
  const [showMFA, setShowMFA] = useState(false);
  const navigate = useNavigate();
  const { userInfo, error, loading } = useSelector(
    (state) => (state as any).userLogin
  );
  const { isTokenValid, loading: tokenStatusLoading } = useSelector(
    (state) => (state as any).tokenStatus
  );
  const [loginWithRecoveryCode, setLoginWithRecoveryCode] =
    useState<boolean>(false);

  // Handle login response and swap to MFA
  const handleLogin = async (values: any) => {
    if (values.email && values.password) {
      setCredentials({ username: values.email, password: values.password });
      try {
        await dispatch(
          login(
            values.email,
            values.password,
            () => {
              // Success callback
              navigate("/portal/home");
              dispatch(refreshTokenStatus());
              setShowMFA(false); // No MFA needed after successful login
            },
            (errorMessage: string) => {
              // Error callback
              openNotification(
                "Login Error",
                errorMessage ||
                  "There was a login error, please check your details.",
                "error"
              );
            },
            () => {
              // MFA callback
              setShowMFA(true); // Show MFA component after login if MFA is enabled
            }
          )
        );
      } catch (err) {
        openNotification(
          "Login Error",
          "An unexpected error occurred, please try again.",
          "error"
        );
      }
    }
  };

  useEffect(() => {
    if (!tokenStatusLoading && isTokenValid) {
      setShowMFA(false); // Reset to login if token is valid
    }
  }, [isTokenValid, tokenStatusLoading]);

  if (tokenStatusLoading) return <Loading />;

  return (
    <div
      className="login-container"
      style={{
        overflowY: "scroll",
        overflowX: "hidden",
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
      <div className="login-containerNew" style={{ zIndex: 10 }}>
        {/* Conditionally Render MFA or Login Form */}
        {showMFA ? (
          <div className="login-containerNew">
            {!isIpPlatform() && <MoveToDataIcon size={128} />}
            <div style={{ minWidth: "500px" }} className="form-containerNew">
              <MfaVerification
                username={credentials.username}
                password={credentials.password}
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
            onFinish={handleLogin}
            className="login-containerNew"
          >
            {!isIpPlatform() && <MoveToDataIcon size={128} />}
            <br />

            <LoginModal
              username={credentials.username}
              password={credentials.password}
              showMFA={showMFA}
            />
          </Form>
        )}
      </div>
    </div>
  );
};

export default Login;
