import React, { useState, useEffect } from "react";
import {
  Button,
  Tooltip,
  Typography,
  Form,
  Input,
  message,
  Radio,
  RadioChangeEvent,
  Switch,
} from "antd";
import { CopyIcon, EditIcon } from "assets/icons/boslerEditorIcons";
import axios from "axios";
import BoslerTimePicker from "../BoslerTimePicker";
import BoslerDatePicker from "../BoslerDatePicker";
import { useDispatch } from "react-redux";
import { copyToClipboard, getLanguageLabel } from "utils/utilities";

const { Text } = Typography;

interface IDeployment {
  details: {
    id: string;
    name: string;
    location: string;
    address: string;
    contactDetails: string;
    email: string;
    deploymentMethod: string;
    pausedUntil: string;
    timeWindowStart: number;
    timeWindowEnd: number;
    overRideTimeWindow: number;
    branch: string;
  };
  onDeploymentUpdated: () => void;
}

const DeploymentCard: React.FC<IDeployment> = ({
  details,
  onDeploymentUpdated,
}) => {
  const dispatch = useDispatch();
  const [isEditing, setIsEditing] = useState(false);
  const [form] = Form.useForm();
  const [radioValue, setRadioValue] = useState("");
  const [tooltipVisible, setTooltipVisible] = useState(false);
  const [dateSelected, setDateSelected] = useState(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [isPause, setIsPause] = useState(!!details.pausedUntil);
  const [deploymentMethod, setDeploymentMethod] = useState(
    details.deploymentMethod
  );

  useEffect(() => {
    form.setFieldsValue(details);
    setIsPause(!!details.pausedUntil);
    setDeploymentMethod(details.deploymentMethod); // Set deployment method on details change
  }, [details, form]);

  useEffect(() => {
    if (!details.pausedUntil) return;

    const intervalId = setInterval(async () => {
      const pausedUntilDate = new Date(details.pausedUntil);
      const now = new Date();
      if (now >= pausedUntilDate) {
        clearInterval(intervalId);
        await handleResume();
      }
    }, 1000);

    return () => clearInterval(intervalId);
  }, [details.pausedUntil]);

  const handleEditClick = () => {
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
  };

  const handlePause = async () => {
    if (!isPause) {
      await handlePauseAPI();
    }
  };

  const handleResume = async () => {
    setDateSelected(false);
    await handleResumeAPI();
  };

  const handleRadioChange = (e: RadioChangeEvent) => {
    setRadioValue(e.target.value);
  };

  const handleTooltipVisibleChange = (visible: boolean) => {
    setTooltipVisible(visible);
  };

  const closeTooltip = () => {
    setTooltipVisible(false);
  };

  const handleDateChange = (date: Date) => {
    setSelectedDate(date);
    setDateSelected(!!date);
  };

  const handleSave = async (values: any) => {
    try {
      console.log("Starttime " + values.timeWindowStart);
      console.log("Starttime " + values.timeWindowEnd);
      await axios.put(`/deployments/update/${details.id}`, values);
      message.success("Deployment details updated successfully");
      setIsEditing(false);
      onDeploymentUpdated();
      dispatch({ type: "UPDATE_DEPLOYMENT", payload: values });
    } catch (error) {
      message.error("Failed to update deployment details");
    }
  };

  const handlePauseAPI = async () => {
    const values = {
      ...details,
      pausedUntil: selectedDate ? selectedDate.toISOString() : null,
    };

    try {
      await axios.put(`/deployments/update/${details.id}`, values);
      message.success("Deployment paused successfully");
      onDeploymentUpdated();
      setIsPause(true);
      setTooltipVisible(false);
    } catch (error) {
      message.error("Failed to pause deployment");
    }
  };

  const handleResumeAPI = async () => {
    const values = {
      ...details,
      pausedUntil: null,
    };

    try {
      await axios.put(`/deployments/update/${details.id}`, values);
      message.success("Deployment resumed successfully");
      onDeploymentUpdated();
      setIsPause(false);
    } catch (error) {
      message.error("Failed to resume deployment");
    }
  };

  const handleSwitchChange = (checked: boolean) => {
    const newMethod = checked ? "AUTOMATIC" : "MANUAL";
    setDeploymentMethod(newMethod);
    form.setFieldsValue({ deploymentMethod: newMethod });
  };

  const tooltipContent = (
    <div style={{ background: "var(--background-color)" }}>
      <Radio.Group
        onChange={handleRadioChange}
        value={radioValue}
        style={{
          background: "var(--background-color)",
          display: "flex",
          flexDirection: "column",
        }}
      >
        <Radio value="Pause Until">Pause Until</Radio>
      </Radio.Group>
      {radioValue === "Pause Until" && (
        <BoslerDatePicker onChange={handleDateChange} />
      )}
      <Button
        onClick={handlePause}
        style={{ margin: "8px" }}
        disabled={!dateSelected}
      >
        Pause
      </Button>
      <Button onClick={closeTooltip} style={{ margin: "8px" }}>
        Close
      </Button>
    </div>
  );

  const formatTimestamp = (timestamp: any) => {
    if (!timestamp) return null;
    const date = new Date(parseInt(timestamp));
    return date.toLocaleString();
  };

  const formatTime = (timestamp: number) => {
    // Convert milliseconds since midnight to hours, minutes, and seconds
    const totalSeconds = Math.floor(timestamp / 1000); // Convert milliseconds to seconds
    const hours = Math.floor(totalSeconds / 3600); // Get hours
    const minutes = Math.floor((totalSeconds % 3600) / 60); // Get minutes
    const seconds = totalSeconds % 60; // Get seconds

    // Format hours, minutes, and seconds
    const formattedHours = hours.toString().padStart(2, "0");
    const formattedMinutes = minutes.toString().padStart(2, "0");
    const formattedSeconds = seconds.toString().padStart(2, "0");

    return `${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
  };

  return (
    <div
      className="deployment-card"
      style={{
        maxHeight: "70vh",
        overflowY: "auto",
        display: "flex",
        flexDirection: "column",
        paddingRight: "15px",
      }}
    >
      <div className="card-header" style={{ marginBottom: "5px" }}>
        <Button
          icon={<EditIcon />}
          onClick={handleEditClick}
          className="edit-button"
          style={{ display: "block", marginLeft: "0", marginBottom: "20px" }}
        />
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <strong style={{ width: "150px", margin: "0", textAlign: "left" }}>
            Id:
          </strong>
          <Input
            value={details.id}
            readOnly
            suffix={
              <Tooltip
                title={getLanguageLabel("clickToCopyIntoClipboard")}
                onVisibleChange={handleTooltipVisibleChange}
              >
                <div onClick={() => copyToClipboard(details.id)}>
                  <CopyIcon />
                </div>
              </Tooltip>
            }
            style={{ flex: "1", maxWidth: "330px" }}
          />
        </div>
      </div>
      {isEditing ? (
        <Form
          form={form}
          onFinish={(values) => {
            form.setFieldsValue({ deploymentMethod });
            handleSave({ ...values, deploymentMethod });
          }}
          layout="vertical"
        >
          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "120px" }}>Name</label>
            <Form.Item
              name="name"
              rules={[{ required: true, message: "Please input the name!" }]}
              style={{ width: "100%", maxWidth: "330px" }}
            >
              <Input style={{ width: "100%" }} />
            </Form.Item>
          </div>

          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "60px", whiteSpace: "nowrap" }}>
              Branch
            </label>
            <Form.Item
              name="branch"
              rules={[
                { required: true, message: "Please input the branch name!" },
              ]}
              style={{ width: "100%", maxWidth: "330px" }}
            >
              <Input style={{ width: "100%" }} />
            </Form.Item>
          </div>

          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "105px" }}>Location</label>
            <Form.Item
              name="location"
              rules={[
                { required: true, message: "Please input the location!" },
              ]}
              style={{ width: "100%", maxWidth: "330px" }}
            >
              <Input style={{ width: "100%" }} />
            </Form.Item>
          </div>

          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "105px" }}>Address</label>
            <Form.Item
              name="address"
              rules={[{ required: true, message: "Please input the address!" }]}
              style={{ width: "100%", maxWidth: "330px" }}
            >
              <Input style={{ width: "100%" }} />
            </Form.Item>
          </div>

          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "60px", whiteSpace: "nowrap" }}>
              Contact Details
            </label>
            <Form.Item
              name="contactDetails"
              rules={[
                {
                  required: true,
                  message: "Please input the contact details!",
                },
              ]}
              style={{ width: "100%", maxWidth: "330px" }}
            >
              <Input style={{ width: "100%" }} />
            </Form.Item>
          </div>

          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "120px" }}>Email</label>
            <Form.Item
              name="email"
              rules={[{ required: true, message: "Please input the email!" }]}
              style={{ width: "100%", maxWidth: "330px" }}
            >
              <Input style={{ width: "100%" }} />
            </Form.Item>
          </div>

          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "30px", whiteSpace: "nowrap" }}>
              Deployment Method
            </label>
            <Form.Item name="deploymentMethod">
              <Switch
                checked={deploymentMethod === "AUTOMATIC"}
                onChange={handleSwitchChange}
                checkedChildren="AUTOMATIC"
                unCheckedChildren="MANUAL"
              />
            </Form.Item>
          </div>

          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "90px", whiteSpace: "nowrap" }}>
              Start Time
            </label>
            <Form.Item
              name="timeWindowStart"
              rules={[{ required: true }]}
              style={{ width: "100%", maxWidth: "330px" }}
            >
              <BoslerTimePicker
                value={form.getFieldValue("timeWindowStart")}
                onChange={(value) =>
                  form.setFieldsValue({ timeWindowStart: value })
                }
              />
            </Form.Item>
          </div>

          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "97px", whiteSpace: "nowrap" }}>
              End Time
            </label>
            <Form.Item
              name="timeWindowEnd"
              rules={[{ required: true }]}
              style={{ width: "100%", maxWidth: "330px" }}
            >
              <BoslerTimePicker
                value={form.getFieldValue("timeWindowEnd")}
                onChange={(value) =>
                  form.setFieldsValue({ timeWindowEnd: value })
                }
              />
            </Form.Item>
          </div>

          <div style={{ display: "flex", alignItems: "baseline" }}>
            <label style={{ marginRight: "20px", whiteSpace: "nowrap" }}>
              Override Deployment
              <br />
              Time
            </label>
            <Form.Item
              name="overRideTimeWindow"
              rules={[{ required: false }]}
              style={{ width: "100%", maxWidth: "330px" }}
            >
              <Input style={{ width: "100%" }} />
            </Form.Item>
          </div>

          <Form.Item>
            <Button type="primary" htmlType="submit">
              Save
            </Button>
            <Button
              type="default"
              onClick={handleCancel}
              style={{ marginLeft: "8px" }}
            >
              Cancel
            </Button>
          </Form.Item>
        </Form>
      ) : (
        <div style={{ lineHeight: "2" }}>
          <p>
            <strong style={{ marginRight: "115px" }}>Name:</strong>{" "}
            {details.name}
          </p>
          <p>
            <strong style={{ marginRight: "100px" }}>Branch:</strong>{" "}
            {details.branch}
          </p>
          <p>
            <strong style={{ marginRight: "100px" }}>Location:</strong>{" "}
            {details.location}
          </p>
          <p>
            <strong style={{ marginRight: "100px" }}>Address:</strong>{" "}
            {details.address}
          </p>
          <p>
            <strong style={{ marginRight: "50px" }}>Contact Details:</strong>{" "}
            {details.contactDetails}
          </p>
          <p>
            <strong style={{ marginRight: "115px" }}>Email:</strong>{" "}
            {details.email}
          </p>
          <p>
            <strong style={{ marginRight: "20px" }}>Deployment Method:</strong>{" "}
            {details.deploymentMethod}
          </p>
          <p>
            <strong style={{ marginRight: "85px" }}>Start Time:</strong>{" "}
            {formatTime(details.timeWindowStart)}
          </p>
          <p>
            <strong style={{ marginRight: "90px" }}>End Time:</strong>{" "}
            {formatTime(details.timeWindowEnd)}
          </p>
          <p>
            <strong style={{ marginRight: "35px" }}>
              Override
              <br /> Deployment Time:
            </strong>{" "}
            {details.overRideTimeWindow}
          </p>
          {isPause && (
            <p>
              <strong>Paused Until:</strong>{" "}
              {formatTimestamp(details.pausedUntil)}
            </p>
          )}
          <p>
            {isPause ? (
              <Button onClick={handleResume} type="primary">
                Resume
              </Button>
            ) : (
              <Tooltip
                placement="topLeft"
                title={tooltipContent}
                overlayInnerStyle={{
                  backgroundColor: "var(--background-color)",
                  border: "black solid 1px",
                  boxShadow: "none",
                }}
                overlayStyle={{ borderRadius: "4px" }}
                trigger="click"
                visible={tooltipVisible}
                onVisibleChange={handleTooltipVisibleChange}
              >
                <Button type="primary">Pause</Button>
              </Tooltip>
            )}
          </p>
        </div>
      )}
    </div>
  );
};

export default DeploymentCard;
