import {
  Avatar,
  Badge,
  Divider,
  Form,
  Input,
  Mentions,
  Popover,
  Row,
  Tabs,
  Tooltip,
} from "antd";

import React, { useEffect, useRef, useState } from "react";
import { AddIcon } from "assets/icons/movetodataActionIcons";
import { CommentIcon } from "assets/icons/movetodataMiscellaneousIcons";

import {
  getLanguageLabel,
  getSocketClient,
  isDefined,
  openNotification,
} from "utils/utilities";
import MoveToDataButton from "../ButtonComponent/MoveToDataButton";

import { KeyboardEvent } from "react";
import {
  addCommentAPI,
  fetchCommentsAPI,
  fetchUsersDetailsAPI,
} from "./Comments.api";

import MoveToDataLoader from "components/movetodataLoader";
import { User } from "global";
import { useDispatch, useSelector } from "react-redux";
import { getAllUserDetails } from "redux/actions/userActions";
import { RootState, ThunkAppDispatch } from "redux/types/store";
import OpenComments from "./OpenComments.view";
import ResolvedComments from "./ResolvedComments.view";

const { TabPane } = Tabs;

const { TextArea } = Input;

const Comments = ({ id }: any) => {
  const ref = useRef(null);
  const [allOpenComments, setAllOpenComments] = useState<Comment[]>([]);
  const [allResolvedComments, setAllResolvedComments] = useState<Comment[]>([]);
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [userMap, setUserMap] = useState(undefined);
  const { allusers } = useSelector((state: RootState) => state.allUserDetails);

  const [isCommentingOn, setIsCommentingOn] = useState(false);
  const [isEditingOnFor, setIsEditingOnFor] = useState("");
  const [isReplyOnFor, setIsReplyOnFor] = useState("");

  const [activeTab, setActiveTab] = useState("open");

  const [AddForm] = Form.useForm();

  const handleKeyPressAdd = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      AddForm.submit();
    }
  };

  const getAllData = () => {
    fetchCommentsAPI(id, "open").then(({ data: data1 }) => {
      setAllOpenComments(data1);
      fetchCommentsAPI(id, "resolved").then(({ data: data2 }) => {
        setAllResolvedComments(data2);
        const combinedData = [...data1, ...data2];
        getUsersDetails(combinedData);
      });
      dispatch(getAllUserDetails());
    });
  };

  const getUsersDetails = (data: Comment[]) => {
    const user_list: any[] = [];
    for (let i = 0; i < data.length; i++) {
      user_list.push(data[i].createdBy);
      for (let j = 0; j < data[i].replies.length; j++)
        user_list.push(data[i].replies[j].createdBy);
    }

    const uniqueUSERIDList: string[] = user_list.filter(
      (item, index) => user_list.indexOf(item) === index
    );
    fetchUsersDetailsAPI(uniqueUSERIDList).then(({ data: usersDetails }) =>
      setUserMap(usersDetails)
    );
  };

  useEffect(() => {
    getAllData();

    const client = getSocketClient();

    client.activate();

    client.onConnect = (frame) => {
      client.subscribe(`/topic/comment/${id}`, async function (mail) {
        const msg = JSON.parse(mail.body).message;
        if (msg === "commentsUpdated") getAllData();
      });
    };

    return () => {
      client.deactivate();
    };
  }, [id]);

  return (
    <div>
      <Popover
        overlayStyle={{ height: "500px", width: "450px" }}
        content={
          isDefined(allusers) ? (
            <div ref={ref}>
              {allResolvedComments.length > 0 ? (
                <Tabs
                  hideAdd
                  centered
                  size="small"
                  defaultActiveKey="open"
                  style={{ height: "400px", overflowY: "auto" }}
                  activeKey={activeTab}
                  onChange={(key) => setActiveTab(key)}
                >
                  <TabPane tab={getLanguageLabel("open")} key="open">
                    <OpenComments
                      id={id}
                      allOpenComments={allOpenComments}
                      userMap={userMap}
                      setIsCommentingOn={setIsCommentingOn}
                      isReplyOnFor={isReplyOnFor}
                      setIsReplyOnFor={setIsReplyOnFor}
                      isEditingOnFor={isEditingOnFor}
                      setIsEditingOnFor={setIsEditingOnFor}
                      parentNode={ref}
                    />
                  </TabPane>
                  <TabPane tab={getLanguageLabel("resolved")} key="resolved">
                    <ResolvedComments
                      id={id}
                      allResolvedComments={allResolvedComments}
                      userMap={userMap}
                      setIsCommentingOn={setIsCommentingOn}
                      setIsReplyOnFor={setIsReplyOnFor}
                      isEditingOnFor={isEditingOnFor}
                      setIsEditingOnFor={setIsEditingOnFor}
                      parentRef={ref}
                    />
                  </TabPane>
                </Tabs>
              ) : (
                <div style={{ height: "400px", overflowY: "auto" }}>
                  <OpenComments
                    id={id}
                    allOpenComments={allOpenComments}
                    userMap={userMap}
                    setIsCommentingOn={setIsCommentingOn}
                    isReplyOnFor={isReplyOnFor}
                    setIsReplyOnFor={setIsReplyOnFor}
                    isEditingOnFor={isEditingOnFor}
                    setIsEditingOnFor={setIsEditingOnFor}
                    parentNode={ref}
                  />
                </div>
              )}
              {(activeTab == "open" || allResolvedComments.length == 0) && (
                <div>
                  <Divider style={{ margin: "0.5rem 0" }} />
                  {isCommentingOn ? (
                    <>
                      <Form
                        form={AddForm}
                        onFinish={(values: any) => {
                          if (
                            values.message == undefined ||
                            values.message == ""
                          ) {
                            openNotification(
                              "Empty Comments not allowed",
                              "",
                              "error"
                            );
                            return;
                          }
                          AddForm.resetFields();

                          addCommentAPI({
                            resourceId: id,
                            message: values.message,
                          }).then(() => setIsCommentingOn(false));
                        }}
                        onFinishFailed={() => setIsCommentingOn(false)}
                      >
                        <Form.Item name="message" style={{ margin: "0.5rem" }}>
                          <Mentions
                            getPopupContainer={() => ref?.current!}
                            autoFocus
                            rows={2}
                            style={{ resize: "none", fontWeight: 600 }}
                            placeholder={`Mention with @userName`}
                            onPressEnter={handleKeyPressAdd}
                            options={allusers?.map((user: User) => {
                              return {
                                label: (
                                  <>
                                    <Avatar src={user.profileImage}>
                                      {user.name
                                        ? user.name.charAt(0).toUpperCase()
                                        : "B"}
                                    </Avatar>{" "}
                                    {user.name} ({user.username})
                                  </>
                                ),
                                value: user.username,
                              };
                            })}
                          />
                        </Form.Item>
                        <Row justify="end">
                          <MoveToDataButton
                            intent="dangerous"
                            size="small"
                            onClick={(e: any) => {
                              // e.preventDefault();
                              e.stopPropagation();
                              setIsCommentingOn(false);
                            }}
                            // icon={<CrossIcon />}
                            minimal
                          >
                            {getLanguageLabel("cancel")}
                          </MoveToDataButton>
                          &nbsp;
                          <MoveToDataButton
                            intent="primary"
                            size="small"
                            htmlType="submit"
                            // icon={<AddIcon />}
                            minimal
                          >
                            {getLanguageLabel("addComment")}
                          </MoveToDataButton>
                        </Row>
                      </Form>
                    </>
                  ) : (
                    <MoveToDataButton
                      onClick={(e: any) => {
                        // e.preventDefault();
                        e.stopPropagation();
                        setIsReplyOnFor("");
                        setIsEditingOnFor("");
                        setIsCommentingOn(true);
                      }}
                      icon={<AddIcon />}
                      fill
                      minimal
                    >
                      {getLanguageLabel("new")} {getLanguageLabel("comment")}
                    </MoveToDataButton>
                  )}
                </div>
              )}
            </div>
          ) : (
            <MoveToDataLoader />
          )
        }
        trigger={"click"}
      >
        <div
          className="text-and-icon-center"
          style={{
            marginTop: "0.2rem",
            // marginLeft: "0.5rem",
            marginRight: "0.5rem",
          }}
        >
          <Tooltip title={getLanguageLabel("comments")} placement={"bottom"}>
            <Badge
              count={allOpenComments.length}
              color={
                allOpenComments.length == 0
                  ? "var(--movetodata-font-color-muted)"
                  : "var(--movetodata-intent-danger)"
              }
              offset={[-5, 3]}
              size="small"
            >
              <MoveToDataButton
                icon={
                  <CommentIcon
                    size={23}
                    color={
                      allOpenComments.length == 0
                        ? "var(--movetodata-font-color-muted)"
                        : "var(--movetodata-intent-danger)"
                    }
                  />
                }
                icononly
                trimicononlypadding
                minimal
              />
            </Badge>
          </Tooltip>
        </div>
      </Popover>
    </div>
  );
};

export default Comments;
