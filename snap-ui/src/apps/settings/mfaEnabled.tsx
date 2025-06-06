import { Col, Form, notification, Row, Typography } from "antd";
import axios from "axios";
import BoslerInput from "components/InputComponent/BoslerInput";
import { BOSLER_TOKEN } from "layouts/auth/constants";
import React, { forwardRef, useImperativeHandle, useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router";
import { refreshTokenStatus } from "redux/actions/tokenActions";
import { getLanguageLabel, isDefined } from "utils/utilities";
import styles from "./mfa.module.scss";
import { setLoginMethod } from "redux/actions/userActions";
import BoslerModalContainer from "components/BoslerModalContainer/BoslerModalContainer";
import { BoslerIcon } from "assets/icons/boslerMiscellaneousIcons";
import BoslerButton from "components/ButtonComponent/BoslerButton";
import { ArrowRightIcon } from "assets/icons/boslerNavigationIcon";
const { Text } = Typography;

interface IMfaVerificationProps {
  username: string | null;
  password: string | null;
  loginWithRecoveryCode: boolean;
  showMFA: boolean;
  setLoginWithRecoverycode: () => void;
}
const MfaVerification: React.FC<IMfaVerificationProps> = ({
  username,
  password,
  loginWithRecoveryCode,
  showMFA,
  setLoginWithRecoverycode,
}) => {
  const [otpCode, setOtpCode] = useState<string>("");

  const [isOtpSubmitted, setIsOtpSubmitted] = useState<boolean>(false);
  const navigate = useNavigate();
  const dispatch = useDispatch();

  // Notification helper
  const openNotificationWithIcon = (
    type: "success" | "error",
    message: string,
    description?: string
  ) => {
    notification[type]({
      message,
      description,
    });
  };

  // Submit OTP for verification
  const handleOtpSubmit = async () => {
    if (!username) {
      openNotificationWithIcon(
        "error",
        "MFA Verification Error",
        "User details or token are missing."
      );
      return;
    }
    try {
      if (loginWithRecoveryCode) {
        dispatch(setLoginMethod("Recovery code"));
        const response = await axios.post(
          `/passport/recovery/${username}/${password}/${otpCode}`
        );
        const data = await response.data;
        if (isDefined(data)) {
          localStorage.setItem(BOSLER_TOKEN, data.authResponse.accessToken);
          setIsOtpSubmitted(true);
          navigate("/portal/home");
          dispatch(refreshTokenStatus());
        } else {
          openNotificationWithIcon(
            "error",
            "Invalid OTP",
            "The OTP you entered is incorrect. Please try again."
          );
        }
      } else {
        dispatch(setLoginMethod("OTP"));
        const response = await axios.post(
          `/passport/verify/${username}/${password}/${otpCode}`
        );
        const data = await response.data;
        if (isDefined(data)) {
          localStorage.setItem(BOSLER_TOKEN, data.accessToken);
          setIsOtpSubmitted(true);
          navigate("/portal/home");
          dispatch(refreshTokenStatus());
        } else {
          openNotificationWithIcon(
            "error",
            "Invalid OTP",
            "The OTP you entered is incorrect. Please try again."
          );
        }
      }
    } catch (error) {
      openNotificationWithIcon(
        "error",
        "OTP Verification Error",
        "Failed to verify OTP. Please try again."
      );
    }
  };

  // Handle keypress to submit OTP on Enter key
  const handleKeyPress = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") {
      handleOtpSubmit();
    }
  };

  return (
    <>
      <BoslerModalContainer
        heading={<BoslerIcon size={32} />}
        footerExtraText={
          !showMFA ? (
            getLanguageLabel("loginAgreement")
          ) : (
            <Text>
              <span>
                {!loginWithRecoveryCode
                  ? "If you've lost your device, enter a "
                  : " If you've your Device, verify with "}
              </span>
              <span
                style={{
                  color: "blue",
                  cursor: "pointer",
                  textDecoration: "underline",
                }}
                onClick={() => setLoginWithRecoverycode()}
              >
                {" "}
                {!loginWithRecoveryCode
                  ? "recovery code."
                  : "authenticator OTP"}
              </span>
            </Text>
          )
        }
        footerButtonArea={
          <Form.Item style={{ margin: 0 }}>
            <BoslerButton
              onClick={() => handleOtpSubmit()}
              intent="success"
              icon={<ArrowRightIcon />}
              htmlType="submit"
            >
              {getLanguageLabel("verify")}
            </BoslerButton>{" "}
          </Form.Item>
        }
      >
        {loginWithRecoveryCode ? (
          <div className={styles.pageWrapper}>
            <Row gutter={16} align="middle" className={styles.main}>
              <Col span={24} className={styles.input_col}>
                {/* <Text strong>Enter Verification Code</Text> */}
                <BoslerInput
                  placeholder={"Enter Recovery Code"}
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value)}
                  className={styles.inputField}
                  onKeyPress={handleKeyPress}
                  autoFocus
                />
              </Col>
            </Row>
          </div>
        ) : (
          <div className={styles.pageWrapper}>
            <Row gutter={16} align="middle" className={styles.main}>
              <Col span={24} className={styles.input_col}>
                {/* <Text strong>Enter Verification Code</Text> */}
                <BoslerInput
                  placeholder={getLanguageLabel("enterVerificationCode")}
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value)}
                  className={styles.inputField}
                  onKeyPress={handleKeyPress}
                  autoFocus
                />
              </Col>
            </Row>
          </div>
        )}
      </BoslerModalContainer>
    </>
  );
};

export default MfaVerification;
