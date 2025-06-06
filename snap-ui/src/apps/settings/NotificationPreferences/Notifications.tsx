import { Card, Col, Divider, Row, Switch, Typography } from "antd";
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";

import { getLanguageLabel } from "utils/utilities";
import {
  getNotificationPreferencesAPI,
  updateNotificationPreferencesAPI,
} from "./NotificationPreferences.api";
const { Text, Title } = Typography;

const NotificationSettings = () => {
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);

  const [notificationPreferences, setNotificationPreferences] = useState({
    mention: false,
    subscription: false,
  });

  useEffect(() => {
    getNotificationPreferencesAPI().then(({ data }) => {
      setNotificationPreferences({
        mention: data.mention,
        subscription: data.subscription,
      });
    });
  }, []);
  return (
    <>
      <div className="settings-center-block">
        <p>
          <Row justify="space-between">
            <Col>
              <Title level={3}>{getLanguageLabel("notifications")}</Title>
              <Text
                type="secondary"
                style={{ fontSize: "0.8rem", fontWeight: "100" }}
              >
                {getLanguageLabel("manageYourNotificationsPreferences")}
              </Text>
            </Col>
          </Row>
          <Divider />
        </p>

        <Card
          title={getLanguageLabel("defaultNotificationsEmail")}
          className="card"
          style={{ marginTop: "20px", width: "45vw" }}
        >
          <Text
            type="secondary"
            style={{ fontSize: "0.8rem", fontWeight: "100" }}
          >
            {getLanguageLabel("notificationsMsg")}
          </Text>

          <br />
          <br />

          <Row justify="space-between">
            <Col>{user.email}</Col>
            <Col>
              <Switch
                checked={
                  notificationPreferences.mention ||
                  notificationPreferences.subscription
                }
                onChange={(e) =>
                  updateNotificationPreferencesAPI({
                    subscription: e,
                    mention: e,
                  }).then(({ data }) => {
                    setNotificationPreferences({
                      mention: data.mention,
                      subscription: data.subscription,
                    });
                  })
                }
                size="small"
              />
            </Col>
          </Row>
        </Card>

        <Card
          title={getLanguageLabel("notifyMeAbout")}
          className="card"
          style={{ marginTop: "20px", width: "45vw" }}
        >
          <Row justify="space-between">
            <Col>
              <div>
                <Text style={{ fontWeight: "500" }}>
                  {getLanguageLabel("comments")}
                </Text>
                <br />
                <Text
                  type="secondary"
                  style={{ fontSize: "0.8rem", fontWeight: "100" }}
                >
                  {getLanguageLabel("mentionInCommentNotification")}
                </Text>
              </div>
            </Col>
            <Col>
              <Switch
                checked={notificationPreferences.mention}
                size="small"
                onChange={(e) =>
                  updateNotificationPreferencesAPI({
                    ...notificationPreferences,
                    mention: e,
                  }).then(({ data }) => {
                    setNotificationPreferences({
                      mention: data.mention,
                      subscription: data.subscription,
                    });
                  })
                }
              />
            </Col>
          </Row>
          <br />

          <br />
          <Row justify="space-between">
            <Col>
              <div>
                <Text style={{ fontWeight: "500" }}>
                  {getLanguageLabel("subscriptions")}
                </Text>
                <br />
                <Text
                  type="secondary"
                  style={{ fontSize: "0.8rem", fontWeight: "100" }}
                >
                  {getLanguageLabel("subscriptionNotification")}
                </Text>
              </div>
            </Col>
            <Col>
              <Switch
                size="small"
                checked={notificationPreferences.subscription}
                onChange={(e) =>
                  updateNotificationPreferencesAPI({
                    ...notificationPreferences,
                    subscription: e,
                  }).then(({ data }) => {
                    setNotificationPreferences({
                      mention: data.mention,
                      subscription: data.subscription,
                    });
                  })
                }
              />
            </Col>
          </Row>
          {/* <Card>
            <br />
            <Row justify="space-between">
              <Col>
                <div>
                  <Text>Charts</Text>
                  <br />
                  <Text type="secondary">
                    When someone comments on a project you're part of, or they
                    mention you.
                  </Text>
                </div>
              </Col>
              <Col>
                <Switch size="small" />
              </Col>
            </Row>

            <br />
            <Row justify="space-between">
              <Col>
                <div>
                  <Text>Dashboard</Text>
                  <br />
                  <Text type="secondary">
                    When someone comments on a project you're part of, or they
                    mention you.
                  </Text>
                </div>
              </Col>
              <Col>
                <Switch size="small" />
              </Col>
            </Row>

            <br />
            <Row justify="space-between">
              <Col>
                <div>
                  <Text>Monitoring</Text>
                  <br />
                  <Text type="secondary">
                    When someone comments on a project you're part of, or they
                    mention you.
                  </Text>
                </div>
              </Col>
              <Col>
                <Switch size="small" />
              </Col>
            </Row>
          </Card> */}
        </Card>
      </div>
    </>
  );
};

export default NotificationSettings;
