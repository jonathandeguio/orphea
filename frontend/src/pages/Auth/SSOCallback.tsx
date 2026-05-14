import React, { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";

const SSOCallback = () => {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    // Parse SAML response from URL params or POST body
    const queryParams = new URLSearchParams(location.search);
    const samlResponse = queryParams.get("SAMLResponse");

    if (samlResponse) {
      // Decode SAML response if needed
      const decodedSAMLResponse = window.atob(samlResponse);

      console.log("decodedSAMLResponse", decodedSAMLResponse);
      // Call API with SAML response
      //   axios
      //     .post("https://your-backend-api.com/auth/sso-login", {
      //       samlResponse: decodedSAMLResponse,
      //     })
      //     .then((response) => {
      //       // On success, store tokens or user data, then navigate to your app
      //       localStorage.setItem("authToken", response.data.token);
      //       navigate("/dashboard"); // Redirect to your dashboard or home page
      //     })
      //     .catch((error) => {
      //       console.error("SSO Login Failed:", error);
      //       // Handle error, show notification, etc.
      //       navigate("/login"); // Redirect to login or error page if failed
      //     });
    } else {
      //   console.error("No SAML response received.");
      //   navigate("/login");
    }
  }, [location, navigate]);

  return (
    <div>
      <h1>Signing in with SSO...</h1>
    </div>
  );
};

export default SSOCallback;
