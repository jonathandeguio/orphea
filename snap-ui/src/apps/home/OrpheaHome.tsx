import { Card, Col, Modal, Row, Typography } from "antd";
import { getDefaultFavicon } from "components/orpheaLoader/FavIconLoader";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { ParticleApp } from "utils/ParticleApp";
import { getLanguageLabel, isDefined } from "utils/utilities";
import OrpheaHomeTable from "./OrpheaHomeTable";
import { getAllDeploymentDetails } from "redux/actions/DeploymentActions";
import { ThunkAppDispatch } from "redux/types/store"; // Import the new component
import OrpheaModal from "components/OrpheaModalContainer";
import MfaConfiguration from "apps/settings/mfaConfiguration";
import {
  isPlatformAdmin,
  setLoginMethod,
  updateUserDetails,
} from "redux/actions/userActions";
import { updateUserDataAPI } from "components/CommandPalette/CommandPalette.api";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
import { CrossIcon } from "assets/icons/orpheaActionIcons";
import { IS_LOGEDIN_WITH_OTP } from "redux/constants/userConstants";

const { Title, Text } = Typography;

const OrpheaHome = () => {
  // Get deployments and platform configuration from Redux
  const { allDeployments } = useSelector(
    (state) => (state as any).allDeploymentDetails
  );
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const { config } = useSelector((state) => (state as any).platformConfig);
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  const { method: loginMethod } = useSelector(
    (state) => (state as $TSFixMe).loginMethod
  );
  const [isOnRecoveryCode, setIsOnRecoveryCode] = useState<boolean>(false);
  // State to manage filtered data for the table
  const [FilteredData, setFilteredData] = useState<any[]>([]); // Initialize as an empty array
  const dispatch = useDispatch<ThunkAppDispatch>();
  // Function to handle table change events
  function onChange(pagination: any, filters: any, sorter: any, extra: any) {
    // Handle table change events if needed
  }
  useEffect(() => {
    if (isDefined(config) && isDefined(user)) {
      dispatch(isPlatformAdmin()).then((data: any) => {
        if (data === false) {
          const mfaSkippedDate = new Date(user?.mfaSkippedAt);
          const sevenDaysAgo = new Date();
          console.log(mfaSkippedDate);
          sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
          if (
            config.mfaEnforced === true &&
            (user.isMfaEnabled === false || user.isMfaEnabled === null)
          ) {
            setIsOpen(true);
          }
          if (
            (user.isMfaEnabled === false || user.isMfaEnabled === null) &&
            config.mfaEnabled === true
          ) {
            if (loginMethod === "Recovery code") {
              setIsOpen(true);
            }
            if (
              mfaSkippedDate < sevenDaysAgo &&
              loginMethod === "Credentials"
            ) {
              setIsOpen(true);
            }
          }
          if (!config.mfaEnforced) {
            window.addEventListener("beforeunload", () => {
              dispatch(setLoginMethod(IS_LOGEDIN_WITH_OTP));
            });
          }
        }
      });
    }
  }, [config, user]);
  const handleMFACancel = () => {
    if (config?.mfaEnforced === false) {
      updateUserDataAPI({
        ...user,
        mfaSkippedDate: new Date(),
        isMfaSkipped: true,
      }).then(({ data }) => {
        setIsOpen(false);
        dispatch(updateUserDetails(data));
      });
      setIsOpen(!isOpen);
    } else {
      if (isOnRecoveryCode) {
        setIsOpen(false);
      }
    }
  };
  // Effect hook to set up the page on load
  useEffect(() => {
    document.title = getLanguageLabel("home");
    let favicon = document.querySelector('link[rel="icon"]') as any;
    favicon.href = getDefaultFavicon();

    // Set page title to platform name or default
    return () => {
      document.title =
        isDefined(config) && isDefined(config.platformName)
          ? config.platformName
          : "Orphea";
    };
  }, [config]);
  useEffect(() => {
    if (loginMethod === "Recovery code") {
      setIsOpen(true);
    }
    dispatch(getAllDeploymentDetails());
  }, []);

  // Effect to set FilteredData when allDeployments changes or on component mount
  useEffect(() => {
    // Check if allDeployments is defined and has data
    if (allDeployments && allDeployments.length > 0) {
      console.log("allDeployments data: ", allDeployments); // Debugging log
      setFilteredData(allDeployments); // Populate FilteredData with allDeployments data
    } else {
      console.log("No allDeployments data found.");
    }
  }, [allDeployments]); // Dependency array ensures this runs when allDeployments is updated
  return (
    <>
      <div
        style={{
          border: "1px solid var(--background-color)",
          background:
            "linear-gradient(transparent 0%, var(--background-color) 100%)",
        }}
      >
        <OrpheaModal
          closable={isOnRecoveryCode ? true : false}
          width={"600px"}
          heading={getLanguageLabel("setUpMFA")}
          extraActionHeading={
            config?.mfaEnforced !== true ? (
              <div style={{ display: "flex", justifyContent: "flex-start" }}>
                <OrpheaButton
                  onClick={() => {
                    handleMFACancel();
                    dispatch(setLoginMethod(IS_LOGEDIN_WITH_OTP));
                  }}
                  minimal={true}
                  icononly={true}
                  intent="none"
                  actionIcon={<CrossIcon />}
                />
              </div>
            ) : (
              ""
            )
          }
          onCancel={handleMFACancel}
          open={isOpen}
        >
          <MfaConfiguration
            setIsOnRecoveryCode={setIsOnRecoveryCode}
            setIsOpen={setIsOpen}
          />
        </OrpheaModal>
        <br />
        <div
          style={{
            position: "relative",
            padding: "12px 12px",
            display: "flex",
            flexDirection: "column",
            minHeight: "28vh",
            justifyContent: "flex-end",
          }}
        >
          <Card
            style={{
              border: "0px solid var(--background-color)",
              borderRadius: "1",
              background: "transparent",
            }}
            className="card"
          >
            <Title level={1}>
              {getLanguageLabel("welcome")} sur{" "}
              {isDefined(config) && isDefined(config.platformName)
                ? config.platformName
                : "Orphea"}
            </Title>
            <br />
            <br />
            <br />
            <br />

            <Title level={3}>
              {getLanguageLabel("hello")} {user.givenName}
            </Title>
            <Text type="secondary">
              {getLanguageLabel("welcomeToSubTitle")}
            </Text>
          </Card>
          <div
            style={{
              position: "absolute",
              height: "100%",
              width: "100%",
              padding: "0 12px 12px 0",
            }}
          >
            <ParticleApp
              fullScreen={false}
              height={100}
              width={100}
              numberOfParticles={300}
              speed={0.5}
              logoSize={6}
              image="/logo_hexa.svg"
            />
          </div>
        </div>
      </div>

      <Row justify={"space-around"}>
        <Col span={8}>
          <div>
            <Title level={3}>Client</Title>
            {/* Pass FilteredData to the table, fallback to allDeployments */}
            <OrpheaHomeTable
              dataSource={FilteredData.length > 0 ? FilteredData : []} // Ensure data is passed
              onChange={onChange}
            />
          </div>
        </Col>
        <Col span={8}>
          <div></div>
        </Col>
        <Col span={8}>
          <div></div>
        </Col>
      </Row>
    </>
  );
};

export default OrpheaHome;
