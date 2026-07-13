import {
  ClockCircleOutlined,
  CalendarOutlined,
  ThunderboltOutlined,
  UserOutlined,
} from "@ant-design/icons";
import { Avatar, Badge, Card, Col, Divider, Row, Statistic, Table, Tag, Typography } from "antd";
import axios from "axios";
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import BoslerLoader from "../../components/boslerLoader";
import { getLanguageLabel, openNotification } from "utils/utilities";

const { Title, Text } = Typography;

const getInitials = (name: string) =>
  name ? name.split(" ").map((n) => n[0]).join("").toUpperCase().slice(0, 2) : "?";

const formatDuration = (seconds: number | null | undefined): string => {
  if (seconds == null || seconds <= 0) return "—";
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  if (h > 0) return `${h}h ${m}m`;
  if (m > 0) return `${m}m ${s}s`;
  return `${s}s`;
};

const EndReasonTag = ({ reason }: { reason: string | null }) => {
  if (!reason) return <Text type="secondary">—</Text>;
  const colors: Record<string, string> = {
    MANUAL: "blue",
    TIMEOUT: "orange",
    EXPIRED: "red",
  };
  const labels: Record<string, string> = {
    MANUAL: "Manual",
    TIMEOUT: "Timeout",
    EXPIRED: "Expired",
  };
  return <Tag color={colors[reason] ?? "default"}>{labels[reason] ?? reason}</Tag>;
};

const LoginActivity = () => {
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);

  const [sessions, setSessions] = useState<any[]>([]);
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const displayName = user?.name || user?.givenName || user?.username || "—";
  const email = user?.email || "";

  useEffect(() => {
    if (!user?.id) return;
    Promise.all([
      axios.get(`/passport/users/${user.id}/last10Login`),
      axios.get(`/passport/users/${user.id}/activityStats`),
    ])
      .then(([sessionsRes, statsRes]) => {
        setSessions(sessionsRes.data);
        setStats(statsRes.data);
      })
      .catch(() => openNotification("Something went wrong", " ", "error"))
      .finally(() => setLoading(false));
  }, [user?.id]);

  const columns = [
    {
      title: "User",
      key: "user",
      width: 160,
      render: () => (
        <Text>
          {displayName}
          {email ? <Text type="secondary"> · {email}</Text> : null}
        </Text>
      ),
    },
    {
      title: "IP",
      dataIndex: "remoteAddr",
      key: "remoteAddr",
      width: 130,
    },
    {
      title: "Login",
      dataIndex: "lastLoginAt",
      key: "lastLoginAt",
      width: 160,
      render: (ts: number) => (ts ? new Date(ts).toLocaleString() : "—"),
    },
    {
      title: "Logout",
      dataIndex: "lastLogoutAt",
      key: "lastLogoutAt",
      width: 160,
      render: (ts: number) =>
        ts ? (
          new Date(ts).toLocaleString()
        ) : (
          <Badge status="processing" text={<Text type="success">Active</Text>} />
        ),
    },
    {
      title: getLanguageLabel("sessionDuration"),
      dataIndex: "durationSeconds",
      key: "durationSeconds",
      width: 110,
      render: (v: number) => formatDuration(v),
    },
    {
      title: getLanguageLabel("endReason"),
      dataIndex: "endReason",
      key: "endReason",
      width: 110,
      render: (v: string) => <EndReasonTag reason={v} />,
    },
  ];

  if (!user) return <BoslerLoader />;

  return (
    <div className="settings-center-block">
      <p>
        <Row align="middle" gutter={16}>
          <Col>
            <Avatar
              size={48}
              src={user?.profileImage || undefined}
              style={{ backgroundColor: "var(--primary-color)" }}
            >
              {!user?.profileImage && getInitials(displayName)}
            </Avatar>
          </Col>
          <Col>
            <Title level={3} style={{ margin: 0 }}>
              {getLanguageLabel("loginActivity")}
            </Title>
            <Text type="secondary">
              {displayName} {email ? `· ${email}` : ""}
            </Text>
          </Col>
        </Row>
        <Divider />
      </p>

      {loading ? (
        <BoslerLoader />
      ) : (
        <>
          {stats && (
            <>
              <Title level={5}>{getLanguageLabel("sessionStats")}</Title>
              <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
                <Col xs={12} sm={6}>
                  <Card size="small">
                    <Statistic
                      title={getLanguageLabel("totalSessions")}
                      value={stats.totalSessions ?? 0}
                      prefix={<UserOutlined />}
                    />
                  </Card>
                </Col>
                <Col xs={12} sm={6}>
                  <Card size="small">
                    <Statistic
                      title={getLanguageLabel("today")}
                      value={formatDuration(stats.todaySeconds)}
                      prefix={<ThunderboltOutlined />}
                    />
                  </Card>
                </Col>
                <Col xs={12} sm={6}>
                  <Card size="small">
                    <Statistic
                      title={getLanguageLabel("last7Days")}
                      value={formatDuration(stats.week7dSeconds)}
                      prefix={<CalendarOutlined />}
                    />
                  </Card>
                </Col>
                <Col xs={12} sm={6}>
                  <Card size="small">
                    <Statistic
                      title={getLanguageLabel("totalTime")}
                      value={formatDuration(stats.totalSeconds)}
                      prefix={<ClockCircleOutlined />}
                    />
                  </Card>
                </Col>
              </Row>
              <Divider />
            </>
          )}

          <Table
            dataSource={sessions}
            columns={columns}
            rowKey="id"
            pagination={{ pageSize: 20, hideOnSinglePage: true }}
            scroll={{ x: true }}
            size="small"
          />
        </>
      )}
    </div>
  );
};

export default LoginActivity;
