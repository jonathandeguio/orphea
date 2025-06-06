import {
  Card,
  Col,
  Divider,
  notification,
  Row,
  Switch,
  Typography,
} from "antd";
import { ClearCacheRunIcon } from "assets/icons/boslerActionIcons";
import { CopyIcon } from "assets/icons/boslerEditorIcons";
import axios from "axios";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel, isDefined } from "utils/utilities";
import {
  setLoginMethod,
  updateUserDetails,
} from "../../../redux/actions/userActions";
import { IS_LOGEDIN_WITH_OTP } from "../../../redux/constants/userConstants";
import { RootState } from "redux/types/store";

const { Text } = Typography;

export const openNotificationWithIcon = (
  type: "success" | "error",
  message: string,
  description?: string
) => {
  notification[type]({
    message,
    description,
  });
};
const MfaConfiguration: React.FC<{
  setIsOnRecoveryCode?: (val: boolean) => void;
  setIsOpen?: (val: boolean) => void;
}> = ({ setIsOpen, setIsOnRecoveryCode }) => {
  const { user } = useSelector((state: RootState) => state.userDetails);
  const [qrImage, setQrImage] = useState<string>("");
  const [mfaEnabled, setMfaEnabled] = useState<boolean>(false);
  const [otpCode, setOtpCode] = useState<string>("");
  const [isOtpSubmitted, setIsOtpSubmitted] = useState<boolean>(false);
  const [recoveryCodes, setRecoveryCodes] = useState<string[]>([]);
  const { loginMethod } = useSelector(
    (state: RootState) => state.userLoginConfig
  );
  const [isOTPVerificationError, setVerificationError] =
    useState<boolean>(false); // Track OTP submission
  // Notification helper

  // Toggle MFA and fetch QR Code
  const getNewQr = async () => {
    const username = user.username;

    // Enable MFA and get QR Code
    try {
      const response = await axios.post(`/passport/mfa/enable/${username}`);
      // Assuming the QR code is returned as a Base64-encoded string
      setQrImage(`data:image/png;base64,${response.data.qrCodeBase64}`); // Set the QR code image
      setOtpCode(""); // Clear previous OTP
      setIsOtpSubmitted(false); // Reset OTP submission state
    } catch (error) {
      openNotificationWithIcon(
        "error",
        "MFA Enable Error",
        "Failed to enable MFA. Please try again."
      );
      setMfaEnabled(false); // Revert the switch in case of failure
    }
  };

  const dispatch = useDispatch<$TSFixMe>();
  // Submit OTP for MFA verification
  const handleOtpSubmit = async () => {
    try {
      const response = await axios.post(`/passport/verify-internal`, {
        username: user.username,
        otp: otpCode,
      });
      if (response.data.status === true) {
        if (isDefined(response.data.recoveryCodes)) {
          setIsOnRecoveryCode && setIsOnRecoveryCode(true);
          setRecoveryCodes(response.data.recoveryCodes.split(", ")); // Ensure it's an array
        }
        setVerificationError(false);
        setMfaEnabled(true);
        setIsOtpSubmitted(true); // Mark OTP as successfully submitted
      } else {
        setVerificationError(true);
        // openNotificationWithIcon(
        //   "error",
        //   "Invalid OTP",
        //   "The OTP you entered is incorrect. Please try again."
        // );
      }
    } catch (error) {
      setVerificationError(true);
      // openNotificationWithIcon(
      //   "error",
      //   "OTP Verification Error",
      //   "Failed to verify OTP. Please try again."
      // );
    }
  };

  const handleCopyToClipboard = () => {
    const codesString = recoveryCodes.join("\n");
    navigator.clipboard.writeText(codesString).then(
      () => {
        setRecoveryCodes([]); // Clear recovery codes after copying
        dispatch(setLoginMethod(IS_LOGEDIN_WITH_OTP));
        setIsOpen && setIsOpen(false);
        dispatch(updateUserDetails({ ...user, isMfaEnabled: true }));
      },
      (err) => {
        openNotificationWithIcon(
          "error",
          "Copy Error",
          "Failed to copy the recovery codes. Please try again."
        );
      }
    );
  };
  // Handle keypress to submit OTP on Enter key
  const handleKeyPress = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") {
      handleOtpSubmit();
    }
  };
  useEffect(() => {
    getNewQr();
  }, []);

  return (
    <>
      <Row style={{ padding: "10px 0" }} gutter={16} align="middle">
        <Col span={12}>
          <Text type="secondary">
            {getLanguageLabel("multiFactorAuthentication")} (MFA)
          </Text>
        </Col>
      </Row>

      {/* Show QR Code and OTP submission when MFA is enabled */}
      {qrImage && !isOtpSubmitted && (
        <>
          <Card>
            <>
              <Row gutter={16} align="middle" style={{ marginTop: 20 }}>
                <Col span={6}></Col>
                <Col>
                  <img
                    src={qrImage}
                    alt="MFA QR Code"
                    style={{ width: "200px", height: "200px" }}
                  />
                </Col>
              </Row>
              <Row gutter={16} align="middle">
                <Col span={16}>
                  <BoslerInput
                    placeholder={getLanguageLabel("enterVerificationCode")}
                    value={otpCode}
                    onKeyDown={handleKeyPress}
                    onChange={(e) => setOtpCode(e.target.value)}
                    autoFocus
                  />
                </Col>
                <Col>
                  <BoslerButton
                    intent={isOTPVerificationError ? "dangerous" : "primary"}
                    onClick={handleOtpSubmit}
                    icon={<ClearCacheRunIcon />}
                    size={"small"}
                  >
                    {getLanguageLabel("verify")}
                  </BoslerButton>
                </Col>
              </Row>
              {isOTPVerificationError &&
                "Failed to verify OTP. Please try again."}
              <Divider />
              <Text style={{ fontSize: "0.8rem" }} type="secondary">
                {getLanguageLabel("scanQRCode")}
              </Text>
            </>
          </Card>
        </>
      )}

      {Array.isArray(recoveryCodes) && recoveryCodes.length > 0 && (
        <>
          <Card>
            <Row gutter={16} align="middle" style={{ marginTop: 20 }}>
              <Col span={24}>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <Text type="secondary">
                    {getLanguageLabel("recoveryCodes")}
                  </Text>
                  <BoslerButton
                    intent="primary"
                    onClick={handleCopyToClipboard}
                    icon={<CopyIcon />}
                  >
                    {getLanguageLabel("copy")}
                  </BoslerButton>
                </div>
              </Col>
              <Col span={24}>
                <Divider />
              </Col>
              <div
                style={{
                  width: "100%",
                  display: "flex",
                  flexWrap: "wrap",
                  gap: "10px",
                  justifyContent: "left",
                  padding: "8px 12px",
                  border: "1px solid #d9d9d9",
                  borderRadius: "4px",
                  backgroundColor: "#f5f5f5",
                  boxShadow: "0 1px 2px rgba(0, 0, 0, 0.1)",
                  whiteSpace: "nowrap",
                }}
              >
                {recoveryCodes.map((code, index) => (
                  <div key={index}>{code}</div>
                ))}
              </div>
            </Row>
          </Card>
        </>
      )}
    </>
  );
};

export default MfaConfiguration;
