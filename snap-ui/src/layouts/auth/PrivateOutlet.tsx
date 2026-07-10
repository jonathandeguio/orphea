import React, { useEffect } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import MainLayout from "../layout";
import Loading from "errors/Loading";
import { MOVETODATA_TOKEN } from "./constants";

const PrivateOutlet = () => {
  const navigate = useNavigate();
  const { isTokenValid, loading: tokenStatusLoading } = useSelector(
    (state) => (state as $TSFixMe).tokenStatus
  );
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);

  useEffect(() => {
    if (
      localStorage.getItem(MOVETODATA_TOKEN) !== undefined &&
      localStorage.getItem(MOVETODATA_TOKEN) !== null
    ) {
      if (!isTokenValid && !tokenStatusLoading) {
        navigate("/auth/relogin");
      }
    } else {
      navigate("/auth/login");
    }
  }, [isTokenValid, tokenStatusLoading]);

  if (isTokenValid && !tokenStatusLoading && user) {
    return <MainLayout />;
  } else {
    return <Loading />;
  }
};

export default PrivateOutlet;
