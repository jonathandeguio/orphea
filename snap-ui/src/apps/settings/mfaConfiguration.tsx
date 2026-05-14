import {
  Card,
  Col,
  Divider,
  notification,
  Row,
  Switch,
  Typography,
} from "antd";
import { CopyIcon } from "assets/icons/orpheaEditorIcons";
import { ArrowRightIcon } from "assets/icons/orpheaNavigationIcon";
import axios from "axios";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
import OrpheaInput from "components/InputComponent/OrpheaInput";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { setLoginMethod, updateUserDetails } from "redux/actions/userActions";
import { IS_LOGEDIN_WITH_OTP } from "redux/constants/userConstants";
import { getLanguageLabel, isDefined } from "utils/utilities";

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
  const { user } = useSelector((state) => (state as any).userDetails); // Assuming Redux stores user details
  const [qrImage, setQrImage] = useState<string>(""); // QR Code image URL (Base64)
  const [mfaEnabled, setMfaEnabled] = useState<boolean>(false); // Track MFA state
  const [otpCode, setOtpCode] = useState<string>(""); // Track the OTP entered by the user
  const [isOtpSubmitted, setIsOtpSubmitted] = useState<boolean>(false); // Track OTP submission
  const [recoveryCodes, setRecoveryCodes] = useState<string[]>([]); // Track recovery codes
  const { method } = useSelector((state) => (state as $TSFixMe).loginMethod);
  const [isOTPVerificationError, setVerificationError] =
    useState<boolean>(false); // Track OTP submission

  // Notification helper

  // Fetch user's current MFA status on component mount
  useEffect(() => {
    if (user && user.isMfaEnabled !== undefined) {
      setMfaEnabled(user.isMfaEnabled); // Initialize MFA status from user details
    }
  }, [user]);

  // Toggle MFA and fetch QR Code
  const handleMfaToggle = async (checked: boolean) => {
    setMfaEnabled(checked);
    const username = user.username;

    if (checked) {
      // Enable MFA and get QR Code
    } else {
      // Disable MFA
      try {
        await axios.post(`/passport/mfa/disable/${username}`);
        setQrImage(""); // Clear QR code if MFA is disabled
        setOtpCode(""); // Clear OTP
        setIsOtpSubmitted(false); // Reset OTP submission state
      } catch (error) {
        openNotificationWithIcon(
          "error",
          "MFA Disable Error",
          "Failed to disable MFA. Please try again."
        );
        setMfaEnabled(true); // Revert the switch in case of failure
      }
    }
  };
  const dispatch = useDispatch();
  // Submit OTP for MFA verification
  const handleOtpSubmit = async () => {
    try {
      const response = await axios.post(
        `/passport/verify/${user.username}/${otpCode}`
      );
      if (response.data.status === true) {
        if (isDefined(response.data.recoveryCodes)) {
          setIsOnRecoveryCode && setIsOnRecoveryCode(true);
          setRecoveryCodes(response.data.recoveryCodes.split(", ")); // Ensure it's an array
        }
        setVerificationError(false);
        openNotificationWithIcon(
          "success",
          "MFA Setup Complete",
          "MFA has been successfully enabled."
        );
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
        // openNotificationWithIcon(
        //   "success",
        //   "Recovery Codes Copied",
        //   "Your recovery codes have been copied to the clipboard."
        // );
        setRecoveryCodes([]); // Clear recovery codes after copying
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
    axios
      .post(`/passport/mfa/enable/${user.username}`)
      .then((res) => {
        setQrImage(`data:image/png;base64,${res.data.qrCodeBase64}`); // Set the QR code image
        setOtpCode(""); // Clear previous OTP
        setIsOtpSubmitted(false); // Reset OTP submission state
      })
      .catch((error) => {
        openNotificationWithIcon(
          "error",
          "MFA Enable Error",
          "Failed to enable MFA. Please try again."
        );
        setMfaEnabled(false); // Revert the switch in case of failure
      });
  }, []);

  return (
    <>
      <Row style={{ padding: "10px 0" }} gutter={16} align="middle">
        <Col span={12}>
          <Text type="secondary">
            {getLanguageLabel("multiFactorAuthentication")}
          </Text>
        </Col>
      </Row>

      {/* Show QR Code and OTP submission when MFA is enabled */}
      {qrImage && !isOtpSubmitted && (
        <>
          <Card className={"selected-card"}>
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
                  <OrpheaInput
                    placeholder={getLanguageLabel("enterVerificationCode")}
                    value={otpCode}
                    onKeyDown={handleKeyPress}
                    onChange={(e) => setOtpCode(e.target.value)}
                    autoFocus
                  />
                </Col>
                <Col>
                  <OrpheaButton
                    intent={isOTPVerificationError ? "dangerous" : "primary"}
                    onClick={handleOtpSubmit}
                    icon={<ArrowRightIcon />}
                    size={"small"}
                  >
                    {getLanguageLabel("verify")}
                  </OrpheaButton>
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
          <Card className={"selected-card"}>
            <Row gutter={16} align="middle" style={{ marginTop: 20 }}>
              <Col span={24}>
                <Text type="secondary">
                  {getLanguageLabel("recoveryCodes")}
                </Text>
                <Divider />
                <div>
                  {recoveryCodes.map((code, index) => (
                    <div key={index}>{code}</div>
                  ))}
                </div>
              </Col>
            </Row>
            <Row gutter={16} align="middle" style={{ marginTop: 20 }}>
              <Col>
                <OrpheaButton
                  intent="primary"
                  onClick={handleCopyToClipboard}
                  icon={<CopyIcon />}
                >
                  {getLanguageLabel("copy")}
                </OrpheaButton>
              </Col>
            </Row>
          </Card>
        </>
      )}
    </>
  );
};

export default MfaConfiguration;
