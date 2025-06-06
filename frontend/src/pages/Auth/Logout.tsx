import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { RootState, ThunkAppDispatch } from "../../redux/types/store";

import "Apps/Kepler/dashboard/DashboardSubscribeMenu/DashboardSubscribeMenu.scss";

import axios from "axios";
import { setTokenInvalid } from "../../redux/actions/tokenActions";

const Logout = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const navigate = useNavigate();

  const { isTokenValid, loading } = useSelector(
    (state: RootState) => state.tokenStatus
  );

  useEffect(() => {
    axios.post("/passport/logout").then(() => {
      dispatch(setTokenInvalid());
    });
  }, []);

  useEffect(() => {
    if (!loading && !isTokenValid) {
      navigate("/auth/login");
    }
  }, [isTokenValid, loading]);

  return <></>;
};

export default Logout;
