import BoslerLoader from "components/boslerLoader";
import UserPopOver from "components/UserPopover/userpopover";
import React, { useEffect, useState } from "react";
import { fetchUserDetailsAPI } from "./UserInfo.api";
interface TProps {
  userId: string;
}

interface TUserData {
  name: string;
}

const UserInfo = ({ userId }: TProps) => {
  const [userData, setUserData] = useState<TUserData>();
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<boolean>(false);
  useEffect(() => {
    fetchUserDetailsAPI(userId)
      .then(({ data }) => {
        setUserData(data);
      })
      .catch((error) => {
        setError(true);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

  if (error) {
    return <>Error!</>;
  }

  if (isLoading) {
    return <BoslerLoader size="tiny" />;
  }

  if (!userData) {
    return <>No user data available</>;
  }

  return (
    <UserPopOver record={userData}>
      <div className="pop-over-item" style={{ display: "inline" }}>
        {userData.name}
      </div>
    </UserPopOver>
  );
};

export default UserInfo;
