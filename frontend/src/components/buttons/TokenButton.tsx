import { Col, Divider, Row, Tooltip, Typography } from "antd";
import { AddUserIcon } from "assets/icons/boslerInterfaceIcons";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  copyToClipboard,
  getLanguageLabel,
  getStringWithAllowedChars,
  openNotification,
} from "utils/utilities";
import { AddIcon } from "../../assets/icons/boslerActionIcons";
import { CopyIcon } from "../../assets/icons/boslerEditorIcons";
import { TickIcon } from "../../assets/icons/boslerNavigationIcon";
import { createToken, listTokens } from "../../redux/actions/tokenActions";
import { ThunkAppDispatch } from "../../redux/types/store";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";
import BoslerLoader from "../boslerLoader";

const antIcon = <BoslerLoader />;

const { Title, Text } = Typography;
let prevToken: $TSFixMe = undefined;
const TokenButton = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [showtoken, setshowtoken] = useState(false);
  // const history = useNavigate();
  const [name, setName] = useState("");
  const [expiry, setExpiry] = useState("");
  const [visible, setVisible] = useState(false);
  const [confirmLoading, setConfirmLoading] = useState(false);

  const [tooltipTitle, setTooltipTitle] = useState(
    getLanguageLabel("clickToCopyIntoClipboard")
  );
  // const [longlivetoken, setlonglivetoken] = useState(undefined);
  const { loading, tokens } = useSelector(
    (state) => (state as $TSFixMe).tokenCreate
  );
  // setlonglivetoken(tokens)
  // useEffect(() => {
  // 	if (success) {
  // 		history.push("/Settings/tokens");
  // 	}
  // }, [success, history]);

  const handleOk = () => {
    if (!name || !expiry) {
      openNotification(
        "Details Incomplete",
        "Enter the complete details",
        "warning"
      );
      return;
    }
    setshowtoken(!showtoken);

    setConfirmLoading(true);
    dispatch(
      createToken({
        name: name,
        expiry: expiry,
      })
    ).then(() => {
      dispatch(listTokens());
      setConfirmLoading(false);
    });

    // setVisible(false);
  };

  const handleCancel = () => {
    setVisible(false);
    setshowtoken(false);
    prevToken = tokens;
    // setlonglivetoken(undefined)
  };
  const handleCopy = () => {
    copyToClipboard(tokens);
    setVisible(false);
    setshowtoken(false);
    prevToken = tokens;
    // setlonglivetoken(undefined)
  };

  return (
    <div>
      <p>
        <Row justify="space-between">
          <Col>
            <Title level={3}>{getLanguageLabel("tokens")}</Title>

            <Text type="secondary">{getLanguageLabel("tokenMsg")}</Text>
          </Col>
          <Col>
            <BoslerButton
              icon={<AddIcon />}
              intent="action"
              onClick={() => setVisible(true)}
            >
              {" "}
              {getLanguageLabel("newToken")}{" "}
            </BoslerButton>
          </Col>
        </Row>
        <Divider />
      </p>

      <BoslerModal
        headingIcon={<AddUserIcon />}
        heading={
          showtoken
            ? getLanguageLabel("tokenVisibleMsg")
            : getLanguageLabel("createNewToken")
        }
        open={visible}
        // onOk={handleOk}
        // loading={loading}
        onCancel={handleCancel}
        footerButtonArea={
          showtoken ? (
            <BoslerButton
              icon={<CopyIcon />}
              intent="action"
              onClick={handleCopy}
            >
              {getLanguageLabel("copy")}
            </BoslerButton>
          ) : (
            <BoslerButton
              icon={<TickIcon />}
              intent="action"
              loading={confirmLoading}
              onClick={handleOk}
              key="submit"
            >
              {getLanguageLabel("create")}
            </BoslerButton>
          )
        }
      >
        {showtoken ? (
          // FIX THIS
          <div onClick={handleCopy}>
            <Tooltip title={tooltipTitle}>
              <p>
                {tokens && tokens !== prevToken ? (
                  <>{tokens}</>
                ) : (
                  <div style={{ textAlign: "center" }}>{antIcon}</div>
                )}
              </p>
            </Tooltip>
          </div>
        ) : (
          <>
            <div className="BoslerHeader1">{getLanguageLabel("name")}</div>
            <BoslerInput
              bordered
              autofocus
              onChange={(e) =>
                setName(getStringWithAllowedChars(e.target.value))
              }
              value={name}
              type="name"
              required
            />

            <div className="BoslerHeader1">
              {getLanguageLabel("expiryDate")}
            </div>
            <BoslerInput
              placeholder="Expiry"
              onChange={(e) => setExpiry(e.target.value)}
              type="date"
              required
            />
          </>
        )}
      </BoslerModal>
    </div>
  );
};

export default TokenButton;
