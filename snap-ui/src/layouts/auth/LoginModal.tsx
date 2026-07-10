import { Form, Input, Typography } from "antd";
import { LockIcon } from "assets/icons/movetodataActionIcons";
import { MoveToDataIcon } from "assets/icons/movetodataMiscellaneousIcons";
import { ArrowRightIcon } from "assets/icons/movetodataNavigationIcon";

import MfaVerification from "apps/settings/mfaEnabled";
import MoveToDataModalContainer from "components/MoveToDataModalContainer/MoveToDataModalContainer";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";
import React, { useRef, useState } from "react";
import { getLanguageLabel } from "utils/utilities";
import { GITHUB_AUTH_URL, GOOGLE_AUTH_URL } from "./constants";

const { Text } = Typography;

const LoginModal = ({
  showMFA,
  username,
  password,
}: {
  showMFA: boolean;
  username: string;
  password: string;
}) => {
  const [loginWithRecoveryCode, setLoginWithRecoveryCode] =
    useState<boolean>(false);

  return (
    <div style={{ minWidth: "31rem" }} className="form-containerNew">
      <MoveToDataModalContainer
        heading={<MoveToDataIcon size={32} />}
        footerExtraText={getLanguageLabel("loginAgreement")}
        footerButtonArea={
          <Form.Item style={{ margin: 0 }}>
            <MoveToDataButton
              intent="success"
              icon={<ArrowRightIcon />}
              htmlType="submit"
            >
              {getLanguageLabel("login")}
            </MoveToDataButton>
          </Form.Item>
        }
        information={
          <div
            style={{
              padding: "10px",
              height: "100%",
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              gap: "1rem",
              flexDirection: "column",
              textAlign: "left",
            }}
          >
            <div className="MoveToDataHeader1" style={{ marginBottom: "10px" }}>
              Single-Sign-On
            </div>

            {/* <Link to={GITHUB_AUTH_URL} style={{ width: "100%" }}>
              <MoveToDataButton icon={<CodeCellIcon />} fill outlined>
                Github
              </MoveToDataButton>
            </Link> */}
            <div className="icons">
              <a
                className="btn btn-block social-btn google"
                style={{ margin: "0 5px" }}
                href={GITHUB_AUTH_URL}
              >
                <img
                  height={"25px"}
                  width={"25px"}
                  src="/github.svg"
                  alt="image"
                />
              </a>
              <a
                className="btn btn-block social-btn google"
                style={{ margin: "0 5px" }}
                href={GOOGLE_AUTH_URL}
              >
                <img
                  height={"25px"}
                  width={"25px"}
                  src="/google.svg"
                  alt="image"
                />
              </a>
            </div>

            {/* <Link to={GOOGLE_AUTH_URL} style={{ width: "100%" }}>
              <MoveToDataButton icon={<EmailIcon />} fill outlined>
                Google
              </MoveToDataButton>
            </Link> */}
            <MoveToDataButton icon={<LockIcon />} fill outlined disabled>
              Keycloak
            </MoveToDataButton>
          </div>
        }
        outerBorder={false}
      >
        <>
          <div className="MoveToDataHeader1" style={{ marginBottom: "10px" }}>
            {getLanguageLabel("login")}
          </div>
          <Form.Item
            name="email"
            // label={<div className="movetodataFormLabel">{getLanguageLabel("userName")}</div>}
            colon={false}
            required
            rules={[
              {
                required: true,
                message: getLanguageLabel("pleaseInputYourUsername"),
              },
            ]}
          >
            <MoveToDataInput autofocus placeholder={getLanguageLabel("userName")} />
          </Form.Item>
          <Form.Item
            name="password"
            // label={<div className="movetodataFormLabel">{getLanguageLabel("password")}</div>}
            colon={false}
            required
            rules={[
              {
                required: true,
                message: getLanguageLabel("pleaseInputYourPassword"),
              },
            ]}
          >
            <Input.Password
              className="inputPasswordComponent"
              placeholder={getLanguageLabel("password")}
            />
          </Form.Item>{" "}
          {/* <Form.Item name="rememberMe" colon={false}>
        //   <div
        //     style={{
        //       display: "flex",
        //       gap: "1rem",
        //       alignItems: "center",
        //     }}
        //   >
        //     <Switch checked={true} onChange={() => {}} size="small"></Switch>
        //     {getLanguageLabel("rememberMe")}
        //   </div>
        // </Form.Item> */}
        </>
      </MoveToDataModalContainer>
    </div>
  );
};

export default LoginModal;
