import { getSparkles } from "Apps/explorer/explorer.utils";
import { Card, Col, Form, List, Row, Select, Tooltip, Typography } from "antd";
import { AddIcon, CrossIcon } from "assets/icons/boslerActionIcons";
import { EditIcon } from "assets/icons/boslerEditorIcons";
import { PulseIcon, TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import { TickIcon } from "assets/icons/boslerNavigationIcon";
import BoslerDatePicker from "components/BoslerComponents/BoslerDatePicker";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { ThunkAppDispatch } from "redux/types/store";
import { getLanguageLabel, timeConverter } from "utils/utilities";
import { isPlatformAdmin } from "../../redux/actions/userActions";
import {
  createLatestNewsAPI,
  getNewsAPI,
  updateLatestNewsAPI,
} from "./UserActivity.api";
import { NEWS_PRIORITY, NEWS_STATUS } from "./UserActivity.constants";

const { Title } = Typography;

export const News = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );

  useEffect(() => {
    dispatch(isPlatformAdmin());
  }, []);
  const [form] = Form.useForm();
  const [editForm] = Form.useForm();
  const [allNews, setAllNews] = useState([]);
  const [isAddNewsButtonActive, setIsAddNewsButtonActive] = useState(false);
  const [isEditingOnFor, setIsEditingOnFor] = useState("");

  const commonFormItem = () => {
    return (
      <>
        <Form.Item name="title">
          <BoslerInput placeholder="Title" />
        </Form.Item>
        <Form.Item name="description">
          <BoslerInput placeholder="Description" />
        </Form.Item>
        <Form.Item name="priority">
          <Select
            placeholder="Priority"
            options={NEWS_PRIORITY.map((priority) => {
              return {
                id: priority.value,
                label: (
                  <>
                    {priority.name} ({priority.value})
                  </>
                ),
                value: priority.value,
              };
            })}
          />
        </Form.Item>
        <Form.Item name="expiration">
          <BoslerDatePicker placeholder="Auto Removal Date" />
        </Form.Item>
      </>
    );
  };
  const getAllNews = () => {
    getNewsAPI().then(({ data }) => {
      setAllNews(data);
    });
  };
  useEffect(() => {
    getAllNews();
  }, []);
  return (
    <div className="settings-center-block">
      <Row justify={"space-between"} align={"middle"}>
        <Col>
          <Title level={3}>News</Title>
        </Col>
        {!isAddNewsButtonActive && !isEditingOnFor && platformAdmin && (
          <Col>
            <Tooltip title={getLanguageLabel("newAnnoucements")}>
              <Title level={3}>
                <BoslerButton
                  onClick={() => setIsAddNewsButtonActive(true)}
                  icon={<AddIcon size={22} />}
                  icononly
                  minimal
                  trimicononlypadding
                />
              </Title>
            </Tooltip>
          </Col>
        )}
      </Row>

      <Card
        style={{
          borderRadius: "1px",
          background: "none",
          minHeight: "50vh",
          //alignContent: "center",
          maxHeight: "50vh",
          overflowY: "scroll",
        }}
      >
        {isAddNewsButtonActive ? (
          <Form
            form={form}
            onFinish={() => {
              createLatestNewsAPI(form.getFieldsValue()).then(() => {
                setIsAddNewsButtonActive(false);
                getAllNews();
                form.resetFields();
              });
            }}
          >
            {commonFormItem()}
            <Row justify={"end"} align={"middle"} gutter={[16, 16]}>
              <Col>
                <BoslerButton
                  intent={"dangerous"}
                  icon={<CrossIcon />}
                  onClick={() => setIsAddNewsButtonActive(false)}
                >
                  {getLanguageLabel("cancel")}
                </BoslerButton>
              </Col>
              <Col>
                <BoslerButton
                  htmlType="submit"
                  intent="action"
                  textTransform="none"
                  icon={<TickIcon />}
                >
                  {getLanguageLabel("create")}
                </BoslerButton>
              </Col>
            </Row>
          </Form>
        ) : (
          <List>
            {allNews.length > 0 ? (
              allNews.map((news: any) =>
                isEditingOnFor == news.id ? (
                  <Form
                    form={editForm}
                    onFinish={() =>
                      updateLatestNewsAPI({
                        id: news.id,
                        ...editForm.getFieldsValue(),
                        status: NEWS_STATUS.ACTIVE,
                      }).then(({ data }) => {
                        setAllNews((allNews: any) => {
                          const newAllNews = allNews.map((_news: any) => {
                            if (news.id != _news.id) return _news;
                            return data;
                          });
                          return newAllNews;
                        });
                        setIsEditingOnFor("");
                      })
                    }
                  >
                    <br />
                    {commonFormItem()}
                    <Row justify={"end"} align={"middle"} gutter={[16, 16]}>
                      <Col>
                        <BoslerButton
                          intent={"dangerous"}
                          icon={<CrossIcon />}
                          onClick={() => setIsEditingOnFor("")}
                        >
                          {getLanguageLabel("cancel")}
                        </BoslerButton>
                      </Col>
                      <Col>
                        <BoslerButton
                          htmlType="submit"
                          intent="action"
                          textTransform="none"
                          icon={<TickIcon />}
                        >
                          {getLanguageLabel("update")}
                        </BoslerButton>
                      </Col>
                    </Row>
                  </Form>
                ) : (
                  <List.Item
                    extra={
                      <Row align="middle" gutter={[16, 16]}>
                        <Tooltip title={getLanguageLabel("edit")}>
                          <BoslerButton
                            onClick={() => {
                              editForm.setFieldsValue(news);
                              setIsEditingOnFor(news.id);
                            }}
                            icon={<EditIcon />}
                            icononly
                            minimal
                            trimicononlypadding
                          />
                        </Tooltip>
                        <Tooltip
                          title={
                            news.expiration ? (
                              <>
                                This will be automatically removed on{" "}
                                {timeConverter(news.expiration)}
                              </>
                            ) : (
                              getLanguageLabel("remove")
                            )
                          }
                        >
                          <BoslerButton
                            onClick={() => {
                              updateLatestNewsAPI({
                                ...news,
                                status: NEWS_STATUS.INACTIVE,
                              }).then(({ data }) => {
                                setAllNews((allNews: any) => {
                                  const newAllNews = allNews.filter(
                                    (_news: any) => {
                                      return news.id != _news.id;
                                    }
                                  );
                                  return newAllNews;
                                });
                              });
                            }}
                            icon={<TrashIcon />}
                            icononly
                            iconColor={"var(--bosler-intent-danger)"}
                            minimal
                            trimicononlypadding
                          />
                        </Tooltip>
                      </Row>
                    }
                  >
                    <List.Item.Meta
                      avatar={<PulseIcon style={{ cursor: "default" }} />}
                      title={
                        <>
                          {news.title}
                          {getSparkles(news.createdAt)}
                        </>
                      }
                      description={
                        <>
                          {news.description}{" "}
                          <div
                            style={{ color: "var(--bosler-font-color-muted)" }}
                          >
                            {timeConverter(news.createdAt)}
                          </div>
                        </>
                      }
                    />
                  </List.Item>
                )
              )
            ) : (
              <div className="centered-content">No News Available.</div>
            )}
          </List>
        )}
      </Card>
    </div>
  );
};
