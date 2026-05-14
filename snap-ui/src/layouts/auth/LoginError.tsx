import { OrpheaIcon } from "assets/icons/orpheaMiscellaneousIcons";
import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { ParticleApp } from "utils/ParticleApp";
import { getLanguageLabel, isIpPlatform } from "utils/utilities";
import { ArrowRightIcon } from "assets/icons/orpheaNavigationIcon";
import {
  setTokenInvalid,
  setTokenValid,
} from "redux/actions/tokenActions";
import { RootState, ThunkAppDispatch } from "redux/types/store";
import Loading from "errors/Loading";
import { BASE_URL } from "./constants";
import { ping } from "../app/App.api";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";

const LoginError = () => {
  const navigate = useNavigate();

  const dispatch = useDispatch<ThunkAppDispatch>();
  const { isTokenValid, loading: tokenStatusLoading } = useSelector(
    (state: RootState) => state.tokenStatus
  );

  useEffect(() => {
    const login_refresh = setInterval(function () {
      if (localStorage.getItem("tokenUpdated") === "true") {
        navigate(-1);
        Ping();
        localStorage.removeItem("tokenUpdated");
      }
    }, 1000);

    if (!tokenStatusLoading && isTokenValid) navigate(-1);

    return () => {
      clearInterval(login_refresh);
    };
  }, [isTokenValid, tokenStatusLoading]);

  function Ping() {
    ping(window.location.pathname)
      .then(({ data }) => {
        if (data.message === "pong") {
          if (tokenStatusLoading || !isTokenValid) {
            dispatch(setTokenValid());
          }
        }
      })
      .catch((error) => {
        if (error.response.data.error === "Unauthorized") {
          if (tokenStatusLoading || isTokenValid) {
            dispatch(setTokenInvalid());
          }
        }
      });
  }

  return tokenStatusLoading ? (
    <Loading />
  ) : (
    <>
      <div className="relogin">
        {/* <DancingLines lineStroke={"#738091"} /> */}
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            background: "var(--background-color)",
            zIndex: "-2",
          }}
        ></div>
        <div
          className="login-container"
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            background: "var(--background-color)",
            // zIndex: "-2",
          }}
        >
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
            <ParticleApp
              fullScreen={true}
              height={1300}
              width={1300}
              numberOfParticles={20}
              speed={2}
              logoSize={12}
              image="/logo_hexa.svg"
            />
          </div>
          <div className="login-icon">
            {!isIpPlatform() && <OrpheaIcon size={"15vw"} />}

            {/* <div className="login-icon-orphea">ORPHEA</div> */}
            <h3>{getLanguageLabel("sessionExpired")}</h3>
          </div>

          <OrpheaButton
            key="submit"
            onClick={() => {
              window.open(
                BASE_URL + "/auth/login?relogin=true",
                "mypopup",
                "directories=no,titlebar=no,toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width=700,height=550"
              );
              return false;
            }}
            icon={<ArrowRightIcon />}
            intent="action"
          >
            {getLanguageLabel("login")}
          </OrpheaButton>
        </div>
      </div>
    </>
  );
};

export default LoginError;
