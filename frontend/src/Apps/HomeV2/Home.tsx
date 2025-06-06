import { Card, Typography } from "antd";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import Favs from "components/UserActivityVault/Favs.view";
import { News } from "components/UserActivityVault/News.view";
import RecentlyViewed from "components/UserActivityVault/RecentlyViewed.view";
import BoslerLoader from "components/boslerLoader";
import { getDefaultFavicon } from "components/boslerLoader/FavIconLoader";
import { ActivatePlatform } from "pages/Settings/PlatformConfig/License/ActivatePlatform.view";
import MfaConfiguration from "pages/Settings/components/MfaConfiguration";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { ParticleApp } from "utils/ParticleApp";
import {
  getLanguageLabel,
  isDefined,
  isLicenseKeyUsedValid,
} from "utils/utilities";
import {
  isPlatformAdmin,
  setLoginMethod,
  updateUserDetails,
} from "../../redux/actions/userActions";
import { IS_LOGEDIN_WITH_OTP } from "../../redux/constants/userConstants";
import "./Home.scss";
import { CrossIcon } from "assets/icons/boslerActionIcons";
import { updateUserDataAPI } from "components/CommandPalette/CommandPalette.api";
import { RootState } from "redux/types/store";

const { Title, Text } = Typography;

const Home = () => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const { config } = useSelector((state) => (state as any).platformConfig);
  const { user } = useSelector((state: RootState) => state.userDetails);
  const [isOnRecoveryCode, setIsOnRecoveryCode] = useState<boolean>(false);
  const { info, loading: licenseLoading } = useSelector(
    (state) => (state as any).license
  );
  const { loginMethod } = useSelector(
    (state) => (state as any).userLoginConfig
  );
  const dispatch = useDispatch<$TSFixMe>();
  useEffect(() => {
    document.title = getLanguageLabel("home");
    let favicon = document.querySelector('link[rel="icon"]') as any;
    favicon.href = getDefaultFavicon();

    return () => {
      document.title =
        isDefined(config) && isDefined(config.platformName)
          ? config.platformName
          : "Bosler";
    };
  }, []);

  useEffect(() => {
    if (isDefined(config) && isDefined(user)) {
      dispatch(isPlatformAdmin()).then((data: any) => {
        if (data === false) {
          const mfaSkippedDate = new Date(user.mfaSkippedAt);
          const sevenDaysAgo = new Date();
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
        dispatch(updateUserDetails(data));
      });
      setIsOpen(!isOpen);
    } else {
      if (isOnRecoveryCode) {
        setIsOpen(false);
      }
    }
  };

  if (licenseLoading) return <BoslerLoader />;

  if (!isLicenseKeyUsedValid(info)) return <ActivatePlatform />;

  return (
    <>
      <div className="home-head">
        <BoslerModal
          closable={isOnRecoveryCode ? true : false}
          width={"600px"}
          heading={getLanguageLabel("setUpMFA")}
          extraActionHeading={
            config?.mfaEnforced !== true ? (
              <div style={{ display: "flex", justifyContent: "flex-start" }}>
                <BoslerButton
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
        </BoslerModal>
        <br />
        <div className="home-head-title">
          <Card className="home-head-title-card">
            <Title level={1}>
              {getLanguageLabel("welcome")} sur{" "}
              {isDefined(config) && isDefined(config.platformName)
                ? config.platformName
                : "Bosler"}
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
          <div className="home-head-title-particle">
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

      <div className="home-body">
        <div className="home-body-recent">
          <RecentlyViewed />
        </div>
        <div className="home-body-fav">
          <Favs />
        </div>
        <div className="home-body-new">
          <News />
        </div>
      </div>
    </>
  );
};

export default Home;
