import { Form, Input } from "antd";
import { BoslerIcon } from "assets/icons/boslerMiscellaneousIcons";
import { ArrowRightIcon } from "assets/icons/boslerNavigationIcon";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModalContainer from "components/CommonUI/BoslerModalContainer/BoslerModalContainer";
import React from "react";
import { getLanguageLabel } from "utils/utilities";

const LoginModal = () => {
  return (
    <div className="form-containerNew">
      <BoslerModalContainer
        heading={<BoslerIcon size={32} />}
        footerExtraText={getLanguageLabel("loginAgreement")}
        footerButtonArea={
          <Form.Item style={{ margin: 0 }}>
            <BoslerButton
              intent="success"
              icon={<ArrowRightIcon />}
              htmlType="submit"
            >
              {getLanguageLabel("login")}
            </BoslerButton>
          </Form.Item>
        }
        // information={
        //   <div
        //     style={{
        //       padding: "10px",
        //       height: "100%",
        //       display: "flex",
        //       justifyContent: "center",
        //       alignItems: "center",
        //       gap: "1rem",
        //       flexDirection: "column",
        //       backgroundColor: "var(--movetodata-bkg-color-muted)",
        //       textAlign: "left",
        //     }}
        //   >
        //     <div className="BoslerHeader1" style={{ marginBottom: "10px" }}>
        //       Single-Sign-On
        //     </div>

        //     {/* <Link to={GITHUB_AUTH_URL} style={{ width: "100%" }}>
        //       <BoslerButton icon={<CodeCellIcon />} fill outlined>
        //         Github
        //       </BoslerButton>
        //     </Link> */}
        //      <a className="btn btn-block social-btn google" style = {{margin: "0 5px" }} href={GITHUB_AUTH_URL}>
        //         <img height={"25px"} width={"25px"}src="/github.svg" alt="image" />
        //       </a>
        //     <a className="btn btn-block social-btn google" style = {{margin: "0 5px" }} href={GOOGLE_AUTH_URL}>
        //         <img height={"25px"} width={"25px"}src="/google.svg" alt="image" />
        //       </a>

        //     {/* <Link to={GOOGLE_AUTH_URL} style={{ width: "100%" }}>
        //       <BoslerButton icon={<EmailIcon />} fill outlined>
        //         Google
        //       </BoslerButton>
        //     </Link> */}
        //     <BoslerButton icon={<LockIcon />} fill outlined disabled>
        //       Keycloak
        //     </BoslerButton>
        //   </div>
        // }
        outerBorder={false}
      >
        <div className="BoslerHeader1" style={{ marginBottom: "10px" }}>
          {getLanguageLabel("login")}
        </div>
        <Form.Item
          name="username"
          // label={<div className="boslerFormLabel">{getLanguageLabel("userName")}</div>}
          colon={false}
          required
          rules={[
            {
              required: true,
              message: getLanguageLabel("pleaseInputYourUsername"),
            },
          ]}
        >
          <BoslerInput autofocus placeholder={getLanguageLabel("userName")} />
        </Form.Item>
        <Form.Item
          name="password"
          // label={<div className="boslerFormLabel">{getLanguageLabel("password")}</div>}
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
        </Form.Item>
        {/* <Form.Item name="rememberMe" colon={false}>
          <div
            style={{
              display: "flex",
              gap: "1rem",
              alignItems: "center",
            }}
          >
            <Switch checked={true} onChange={() => {}} size="small"></Switch>
            {getLanguageLabel("rememberMe")}
          </div>
        </Form.Item> */}
      </BoslerModalContainer>
    </div>
  );
};

export default LoginModal;
