import { Modal, Progress, Typography } from "antd";
import React from "react";
import { getLanguageLabel } from "utils/utilities";

const { Text } = Typography;

interface SessionTimeoutModalProps {
  visible: boolean;
  secondsLeft: number;
  warningBeforeSeconds?: number;
  onExtend: () => void;
  onLogout: () => void;
}

const SessionTimeoutModal: React.FC<SessionTimeoutModalProps> = ({
  visible,
  secondsLeft,
  warningBeforeSeconds = 120,
  onExtend,
  onLogout,
}) => {
  const percent = Math.round((secondsLeft / warningBeforeSeconds) * 100);

  return (
    <Modal
      open={visible}
      title={getLanguageLabel("sessionExpiringSoon")}
      okText={getLanguageLabel("stayConnected")}
      cancelText={getLanguageLabel("logout")}
      onOk={onExtend}
      onCancel={onLogout}
      closable={false}
      maskClosable={false}
      centered
    >
      <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
        <Text>
          {getLanguageLabel("sessionExpiringSoonDesc")}{" "}
          <Text strong>
            {secondsLeft} {getLanguageLabel("seconds")}
          </Text>
          .
        </Text>
        <Progress
          percent={percent}
          showInfo={false}
          strokeColor={percent > 50 ? "var(--primary-color)" : percent > 20 ? "#faad14" : "#ff4d4f"}
          trailColor="var(--border-color)"
        />
      </div>
    </Modal>
  );
};

export default SessionTimeoutModal;
