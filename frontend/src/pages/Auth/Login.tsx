import { Form } from "antd";
import { useEffect } from "react";
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

import { ParticleApp } from "utils/ParticleApp";
import LoginModal from "./LoginModal";

const Login = () => {
  const [form] = Form.useForm();
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
      navigate("/portal/home");
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
            image="/logo_hexa.png"
          />
        </div>
        <div
          className="login-containerNew"
          style={{
            zIndex: 10,
          }}
        >
          <Form
            form={form}
            layout="vertical"
            initialValues={{}}
            onFinish={(values) => {
              if (values.username && values.password) {
                dispatch(
                  login(
                    convertStringToLowerCase(values.username),
                    values.password
                  )
                ).then((data: $TSFixMe) => {
                  dispatch(refreshTokenStatus());
                  setTheme(data);
                });
              }
            }}
            className="login-containerNew"
          >
            {!isIpPlatform() && <img src="/logoMoveToData.png" alt="MoveToData" style={{ height: 128, marginBottom: 8 }} />}
            <br />
            <LoginModal />
          </Form>
        </div>
      </div>
    </>
  );
};

export default Login;
