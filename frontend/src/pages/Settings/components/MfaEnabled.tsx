import { Col, Form, notification, Row, Typography } from "antd";
import { BoslerIcon } from "assets/icons/boslerMiscellaneousIcons";
import { ArrowRightIcon } from "assets/icons/boslerNavigationIcon";
import axios from "axios";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModalContainer from "components/CommonUI/BoslerModalContainer/BoslerModalContainer";
import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router";
import { getLanguageLabel, isDefined } from "utils/utilities";
import { refreshTokenStatus } from "../../../redux/actions/tokenActions";
import { setLoginMethod } from "../../../redux/actions/userActions";
import { IS_LOGEDIN_WITH_RECOVERYCODE } from "../../../redux/constants/userConstants";
import styles from "./mfa.module.scss";
const { Text } = Typography;

interface IMfaVerificationProps {
  username: string | null;
  password: string | null;
  loginWithRecoveryCode: boolean;
  showMFA: boolean;
  setLoginWithRecoverycode: (arg: boolean) => void;
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
  const dispatch = useDispatch<$TSFixMe>();

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

  const handleOtpSubmit = () => {
    if (!username) {
      openNotificationWithIcon(
        "error",
        "MFA Verification Error",
        "User details or token are missing."
      );
      return;
    }
    if (loginWithRecoveryCode) {
      dispatch(setLoginMethod(IS_LOGEDIN_WITH_RECOVERYCODE));
      axios
        .post(`/passport/recovery`, {
          username: username,
          password: password,
          recoveryCode: otpCode,
        })
        .then(({ data }) => {
          if (isDefined(data)) {
            setIsOtpSubmitted(true);
            dispatch(refreshTokenStatus());
            navigate("/portal/home");
          } else {
            openNotificationWithIcon(
              "error",
              "Invalid OTP",
              "The OTP you entered is incorrect. Please try again."
            );
          }
        })
        .catch((err) => {
          openNotificationWithIcon(
            "error",
            "OTP Verification Error",
            "Failed to verify OTP. Please try again."
          );
        });
    } else {
      dispatch(setLoginMethod("OTP"));
      axios
        .post(`/passport/verify`, {
          username: username,
          password: password,
          otp: otpCode,
        })
        .then(({ data }) => {
          if (isDefined(data)) {
            setIsOtpSubmitted(true);
            dispatch(refreshTokenStatus());
            navigate("/portal/home");
          } else {
            openNotificationWithIcon(
              "error",
              "Invalid OTP",
              "The OTP you entered is incorrect. Please try again."
            );
          }
        })
        .catch((err) => {
          openNotificationWithIcon(
            "error",
            "OTP Verification Error",
            "Failed to verify OTP. Please try again."
          );
        });
    }
  };

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
                onClick={() => setLoginWithRecoverycode(!loginWithRecoveryCode)}
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
