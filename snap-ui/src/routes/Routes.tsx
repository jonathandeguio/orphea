import React from "react";
import {
  createBrowserRouter,
  createRoutesFromElements,
  Navigate,
  Route,
} from "react-router-dom";
import AppContainer from "layouts/app/AppContainer";

import Home from "apps/home";

import MoveToDataUIComponents from "apps/developer/MoveToDataUIComponents";
import NoDataComponents from "apps/developer/NoDataComponents";
import TestingArea from "apps/developer/TestingArea";
import Typhography from "apps/developer/Typhography";

import NotificationSettings from "apps/settings/NotificationPreferences/Notifications";
import PlatformSettings from "apps/settings/platform";
import ArtifactorySettings from "apps/settings/platform/ArtifactorySettings.view";
import { BackingFsSettings } from "apps/settings/platform/BackingFs/BackingFsSettings.view";
import BranchSettings from "apps/settings/platform/BranchSettings";
import { BulkUserCreationSettings } from "apps/settings/platform/BulkUserCreation/BulkUserCreationSettings.view";
import { CacheSettings } from "apps/settings/platform/CacheSettings.view";
import { DownloadSettings } from "apps/settings/platform/DownloadSettings.view";
import { GitSettings } from "apps/settings/platform/Git/GitSettings.view";
import { HistorySettings } from "apps/settings/platform/HistorySettings.view";
import HttpProxySettings from "apps/settings/platform/HTTPProxySettings.view";
import SMTPSettings from "apps/settings/platform/SMTPSettings.view";
import { ThemeSettings } from "apps/settings/platform/ThemeSettings.view";
import { TimezoneSettings } from "apps/settings/platform/TimezoneSettings.view";
import { UploadSettings } from "apps/settings/platform/UploadSettings.view";
import MoveToDataComponents from "apps/developer/MoveToDataComponents";
import DeveloperHome from "apps/developer/DeveloperHome";
import IconList from "apps/developer/iconsList";
import ChangePassword from "apps/settings/ChangePassword";
import Groups from "apps/settings/Groups";
import LoginActivity from "apps/settings/LoginActivity";
import DeveloperSettings from "apps/settings/platform/DeveloperSettings";
import Preferences from "apps/settings/Preferences";
import ProfileMe from "apps/settings/ProfileMe";
import SSO from "apps/settings/SSO";
import Tokens from "apps/settings/Tokens/Tokens";
import Users from "apps/settings/Users/Users";

import Login from "layouts/auth/Login";
import Logout from "layouts/auth/Logout";
import PrivateOutlet from "layouts/auth/PrivateOutlet";

import Notfound from "errors/Notfound";
import LoginError from "layouts/auth/LoginError";

import Settings from "apps/settings";

import Artifacts from "apps/builds/artifacts/Artifacts";
import Triggers from "apps/builds/triggers/Triggers";
import OAuth2RedirectHandler from "layouts/auth/OAuth2RedirectHandler";
import MoveToDataHome from "apps/home/MoveToDataHome";
import ManageGroups from "apps/settings/ManageGroups";
import ProfileUser from "apps/settings/ProfileUser";
import Deployments from "apps/deployments/deployment/Deployments";
import Configuration from "apps/deployments/configuration/Configurations";
import DisableMfa from "apps/settings/DisableMfa";
import { MfaSetting } from "apps/settings/platform/MfaSetting.view";

const useRouter = () => {
  return createBrowserRouter(
    createRoutesFromElements(
      <Route path="/" element={<AppContainer />}>
        <Route path="/" element={<PrivateOutlet />}>
          <Route index element={<Navigate to="/portal/home" />} />

          <Route path="/portal" element={<Home />}>
            <Route index element={<Navigate to="/portal/home" />} />
            <Route path="home" element={<MoveToDataHome />} />
          </Route>
          <Route path="/portal/triggers" element={<Triggers />} />
          <Route path="/portal/artifacts/:id" element={<Artifacts />} />
          
          <Route path="/portal/deployments" element={<Deployments />} />
          <Route path="/portal/deployments/configuration/:id" element={<Configuration />} />
          <Route path="/portal" element={<DeveloperHome />}>
            {process.env.NODE_ENV !== "production" && (
              <>
                <Route path="icons" element={<IconList />} />
                <Route path="components" element={<MoveToDataComponents />} />
                <Route path="typography" element={<Typhography />} />
                <Route path="uicomponents" element={<MoveToDataUIComponents />} />
                <Route path="nodata" element={<NoDataComponents />} />
                <Route path="testingarea" element={<TestingArea />} />
              </>
            )}
          </Route>

          <Route path="/portal/settings/user/:id" element={<ProfileUser />} />

          <Route
            path="/portal/settings/groups/:id/manageGroup"
            element={<ManageGroups />}
          />

          <Route path="/portal/settings" element={<Settings />}>
            <Route path="profile" element={<ProfileMe />} />
            <Route path="preferences" element={<Preferences />} />
            <Route path="notifications" element={<NotificationSettings />} />
            <Route path="tokens" element={<Tokens />} />
            <Route path="users" element={<Users />} />
            <Route path="groups" element={<Groups />} />
            <Route path="loginActivity" element={<LoginActivity />} />
            <Route path="changePassword" element={<ChangePassword />} />
            <Route path="mfaConfig" element={<DisableMfa />} />
            <Route path="sso" element={<SSO />} />

            <Route path="platform">
              <Route path="home" element={<PlatformSettings />} />
              <Route path="upload" element={<UploadSettings />} />
              <Route path="download" element={<DownloadSettings />} />
              <Route path="cache" element={<CacheSettings />} />
              <Route path="history" element={<HistorySettings />} />
              <Route path="timezone" element={<TimezoneSettings />} />
              <Route path="theme" element={<ThemeSettings />} />
              <Route path="smtp" element={<SMTPSettings />} />
              <Route path="artifactory" element={<ArtifactorySettings />} />
              <Route path="branch" element={<BranchSettings />} />
              <Route path="httpProxy" element={<HttpProxySettings />} />
              <Route path="backingFs" element={<BackingFsSettings />} />
              <Route path="git" element={<GitSettings />} />

              <Route
                path="bulkUserCreation"
                element={<BulkUserCreationSettings />}
              />
              <Route
                path="mfa"
                element={<MfaSetting />}
              />

              <Route path="developerSettings" element={<DeveloperSettings />} />
            </Route>
          </Route>
        </Route>

        <Route path="/auth/login" element={<Login />} />
        <Route path="/auth/relogin" element={<LoginError />} />
        <Route path="/auth/logout" element={<Logout />} />
        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
        <Route path="*" element={<Notfound />} />
      </Route>
    )
  );
};

export { useRouter };
