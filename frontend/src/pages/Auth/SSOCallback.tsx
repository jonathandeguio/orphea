import React, { useEffect } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router";

export const loginRequest = {
  scopes: ["openid", "profile", "User.Read"], // Define scopes based on your API permissions
};

const SSOCallback = () => {
  const navigate = useNavigate();

  const { isTokenValid, loading: tokenStatusLoading } = useSelector(
    (state) => (state as $TSFixMe).tokenStatus
  );
  useEffect(() => {
    if (!tokenStatusLoading && !isTokenValid) {
      // navigate("/auth/login");
    } else {
      navigate("/portal/home");
    }
  }, [isTokenValid, tokenStatusLoading]);
  return (
    <div>
      <h1>Signing in with SSO...</h1>
    </div>
  );
};

export default SSOCallback;
