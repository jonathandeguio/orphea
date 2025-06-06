import React from "react";
import { useSelector } from "react-redux";
import Profile from "./Profile";

const ProfileMe = () => {
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  return <Profile user={user} showPreferences={false} self={true} />;
};

export default ProfileMe;
