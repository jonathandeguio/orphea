import { Avatar, Col, Collapse, Divider, Row, Typography } from "antd";
import { getDefaultFavicon } from "components/orpheaLoader/FavIconLoader";
import { User } from "global";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import { getLanguageLabel, isDefined } from "utils/utilities";
import { ArrowLeftIcon } from "assets/icons/orpheaNavigationIcon";
import { isPlatformAdmin, isUserAdmin } from "redux/actions/userActions";
import { ThunkAppDispatch } from "redux/types/store";
import { getUserDetailsByIdAPI } from "./apis";
import Profile from "./Profile";
import OrpheaInput from "components/InputComponent/OrpheaInput";

const { Title, Text } = Typography;
const { Panel } = Collapse;

const ProfileUser = () => {
  const { id } = useParams();
  const { user: currentUser } = useSelector(
    (state) => (state as $TSFixMe).userDetails
  );
  const [user, setUser] = useState<User>();

  const { user: userAdmin } = useSelector((state) => (state as any).userAdmin);
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );
  const navigate = useNavigate();

  const dispatch = useDispatch<ThunkAppDispatch>();
  useEffect(() => {
    if (isDefined(id)) {
      getUserDetailsByIdAPI(id).then(({ data }) => {
        setUser(data);
      });
    }

    dispatch(isPlatformAdmin());
    dispatch(isUserAdmin());
  }, []);

  const { config } = useSelector((state) => (state as any).platformConfig);

  useEffect(() => {
    document.title = getLanguageLabel("settings");
    let favicon = document.querySelector('link[rel="icon"]') as any;
    favicon.href = getDefaultFavicon();

    return () => {
      document.title =
        isDefined(config) && isDefined(config.platformName)
          ? config.platformName
          : "Orphea";
    };
  }, []);

  return (
    <>
      {user &&
        (userAdmin || platformAdmin ? (
          <>
            <div
              className="settings-center-block"
              style={{ marginTop: "3rem" }}
            >
              <Row justify="start">
                <Col onClick={() => navigate(-1)}>
                  <div className="text-and-icon-center">
                    <ArrowLeftIcon size={30} />{" "}
                    <Text style={{ fontSize: "30px", fontWeight: "bold" }}>
                      {getLanguageLabel("userDetails")}
                    </Text>
                  </div>
                </Col>
              </Row>
            </div>
            <Profile
              user={user}
              self={currentUser.id == user.id}
              loginHistory={true}
              showPreferences={true}
            />
          </>
        ) : (
          <div className="settings-center-block" style={{ marginTop: "3rem" }}>
            <Row justify="start">
              <Col onClick={() => navigate(-1)}>
                <div className="text-and-icon-center">
                  <ArrowLeftIcon size={30} />{" "}
                  <Text style={{ fontSize: "30px", fontWeight: "bold" }}>
                    {getLanguageLabel("userDetails")}
                  </Text>
                </div>
              </Col>
            </Row>
            <Row justify="center">
              <Col>
                <Avatar
                  className="interactive"
                  src={
                    user.profileImage && user.profileImage != ""
                      ? user.profileImage
                      : null
                  }
                  size={200}
                >
                  {user.name ? user.name.charAt(0).toUpperCase() : "B"}
                </Avatar>
              </Col>
            </Row>
            <br />
            <Row justify="center">
              <Col>
                <Title level={1}>{user.name}</Title>
              </Col>
            </Row>
            <Divider />
            <div className="OrpheaHeader1">{getLanguageLabel("givenName")}</div>
            <OrpheaInput
              placeholder={user.givenName}
              // @ts-expect-error TS(2322): Type 'string[]' is not assignable to type 'string'... Remove this comment to see the full error message
              name={["user", "fname"]}
              disabled
            />
            <div className="OrpheaHeader1">
              {getLanguageLabel("familyName")}
            </div>
            <OrpheaInput
              // @ts-expect-error TS(2322): Type 'string[]' is not assignable to type 'string'... Remove this comment to see the full error message
              name={["user", "lname"]}
              placeholder={user.familyName}
              disabled
            />
            <div className="OrpheaHeader1">{getLanguageLabel("email")}</div>
            <OrpheaInput
              placeholder={user.email}
              // @ts-expect-error TS(2322): Type 'string[]' is not assignable to type 'string'... Remove this comment to see the full error message
              name={["user", "email"]}
              type="email"
              disabled
            />
            <div className="OrpheaHeader1">{getLanguageLabel("location")}</div>
            <OrpheaInput
              placeholder={user.location}
              // @ts-expect-error TS(2322): Type 'string[]' is not assignable to type 'string'... Remove this comment to see the full error message
              name={["location"]}
              disabled
            />

            <Row>
              <Col>
                {user.ssoAttributes && (
                  <Collapse>
                    <Panel header={`SSO Attributes`} key="1">
                      {Object.keys(user.ssoAttributes).map((key: any) => {
                        return (
                          <Row gutter={16}>
                            <Col>
                              <Text type="secondary">{key}:</Text>
                            </Col>
                            <Col>
                              <Text>
                                {user.ssoAttributes[key]
                                  ? user.ssoAttributes[key]
                                  : "--"}
                              </Text>
                            </Col>
                          </Row>
                        );
                      })}
                    </Panel>
                  </Collapse>
                )}
              </Col>
            </Row>
          </div>
        ))}
    </>
  );
};

export default ProfileUser;
