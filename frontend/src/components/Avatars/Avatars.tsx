import { Avatar, Divider, Popover, Typography } from "antd";
import React, { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { getLanguageLabel, getSocketClient } from "utils/utilities";
import { getUserDetailByID } from "../../redux/actions/userActions";

const { Text } = Typography;

const Avatars = ({ link }: any) => {
  const dispatch = useDispatch<any>();
  const CURRENT_ACTIVE = getLanguageLabel("currentActiveOnRepo");
  const [onlineUsers, setOnlineUsers] = useState(new Map());
  const [userMap, setUserMap] = useState(new Map());

  useEffect(() => {
    const client = getSocketClient();

    client.activate();
    client.onConnect = (frame) => {
      client.subscribe(link, async function (mail) {
        const currentUser = JSON.parse(mail.body).message;
        if (currentUser != undefined && currentUser != "") {
          if (!userMap.has(currentUser)) {
            await dispatch(getUserDetailByID(currentUser)).then(
              (result: any) => {
                setUserMap((prev) => {
                  return new Map(prev.set(currentUser, result));
                });
              }
            );
          }

          setOnlineUsers((prev) => {
            return new Map(
              prev.set(currentUser, Math.floor(Date.now() / 1000))
            );
          });

          setOnlineUsers((prev: any) => {
            const temp = prev;
            for (const [key, value] of prev.entries()) {
              if (
                key != currentUser &&
                Math.floor(Date.now() / 1000) - value >= 120
              )
                temp.delete(key);
            }
            return temp;
          });
        }
      });
    };

    return () => {
      setOnlineUsers(new Map());
      client.deactivate();
    };
  }, [link]);

  return (
    <>
      <Avatar.Group
        maxCount={10}
        maxPopoverTrigger="click"
        maxStyle={{
          cursor: "pointer",
        }}
      >
        {Array.from(onlineUsers.entries()).map(([key, value]) => {
          const unixTimestamp = Math.floor(Date.now() / 1000);

          return (
            userMap.has(key) && (
              <Popover
                title={
                  <>
                    <Text type="secondary" strong>
                      <>
                        <Avatar src={userMap.get(key)?.profileImage}>
                          {userMap.get(key).name
                            ? (userMap.get(key) as $TSFixMe).name
                                .charAt(0)
                                .toUpperCase()
                            : "B"}
                        </Avatar>
                      </>{" "}
                      &nbsp; {userMap.get(key).name}{" "}
                    </Text>
                    <Divider style={{ margin: "1px" }} /> {CURRENT_ACTIVE}
                  </>
                }
                placement="bottom"
              >
                {
                  <Avatar
                    src={userMap.get(key)?.profileImage}
                    style={
                      unixTimestamp - value >= 30
                        ? {
                            backgroundColor: "(--background-color)",
                            borderColor: "#FF9980",
                            borderWidth: 2,
                            borderStyle: "dashed",
                          }
                        : {
                            // "var(--movetodata-border-color-default)"
                            borderColor: "#72CA9B",
                            borderWidth: 2,
                          }
                    }
                  >
                    {userMap.get(key).name != null
                      ? userMap.get(key).name.charAt(0).toUpperCase()
                      : ""}
                  </Avatar>
                }
              </Popover>
            )
          );
        })}
      </Avatar.Group>
    </>
  );
};

export default Avatars;
