import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
// import MainLayout from "../layouts/layout";
import { useSelector } from "react-redux";
import Loading from "../utils/Loading";
import React from 'react';

const PrivateOutlet = () => {
  const navigate = useNavigate();
	const { isTokenValid, loading: tokenStatusLoading } = useSelector((state) => (state as any).tokenStatus);
  const { user } = useSelector((state) => (state as any).userDetails);

  useEffect(() => {
    if(localStorage.getItem('orpheaToken') !== undefined && localStorage.getItem('orpheaToken') !== null) {
      if(!isTokenValid && !tokenStatusLoading) {
        navigate('/relogin');
      }
    } else {
      navigate("/login");
    }
  }, [isTokenValid, navigate, tokenStatusLoading]);

  if (isTokenValid && !tokenStatusLoading && user) {
    // return <MainLayout />;
    return <div> Hello </div>;
  } else {
    return <Loading/>;
  }
};

export default PrivateOutlet;