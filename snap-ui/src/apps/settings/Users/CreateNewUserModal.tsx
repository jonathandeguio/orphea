import { Input, Radio, Select, Typography, Upload } from "antd";
import { RcFile } from "antd/es/upload";
import axios from "axios";
import React, { Dispatch, SetStateAction, useState } from "react";
import ReactCountryFlag from "react-country-flag";
import { useDispatch } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import {
  AddUserIcon,
  UploadIcon,
} from "assets/icons/orpheaInterfaceIcons";
import { TickIcon } from "assets/icons/orpheaNavigationIcon";
import { getAllUserDetails } from "redux/actions/userActions";
import { ThunkAppDispatch } from "redux/types/store";


import { ErrorResponse } from "global";
import OrpheaModal from "components/OrpheaModalContainer";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
import OrpheaInput from "components/InputComponent/OrpheaInput";

const { Text } = Typography;
const { Option } = Select;

interface Props {
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
}

const CreateNewUserModal = ({ isOpen, setIsOpen }: Props) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [isUploaded, setIsUploaded] = useState(false);
  const initialUserDetails = {
    name: "",
    username: "",
    password: "",
    givenName: "",
    familyName: "",
    location: "",
    profileImage: "",
    email: "",
    mode: "",
    language: "",
  };

  const [newUserDetails, setNewUserDetails] = useState({
    ...initialUserDetails,
  });

  const handleOk = async () => {
    if (
      !(
        newUserDetails.name &&
        newUserDetails.username &&
        newUserDetails.password
      )
    ) {
      openNotification(
        "Details Incomplete",
        "Please enter the complete details",
        "warning"
      );
      return;
    }
    try {
      await axios.post(
        `/passport/users/add`,
        JSON.stringify({
          ...newUserDetails,
        })
      );
      //
    } catch (error) {
      if (axios.isAxiosError(error) && isDefined(error.response)) {
        const errorMessage = (error?.response?.data as ErrorResponse).error;
        openNotification(
          errorMessage,
          "Please contact platform admin.",
          "error"
        );
      }
    }
    setIsOpen(false);
    dispatch(getAllUserDetails());
  };

  const getBase64 = (img: RcFile, callback: (url: string) => void) => {
    const reader = new FileReader();
    reader.addEventListener("load", () => callback(reader.result as string));
    reader.readAsDataURL(img);
  };

  const props = {
    action: "https://www.mocky.io/v2/5cc8019d300000980a055e76",
    headers: {
      authorization: "authorization-text",
    },
    accept: ".jpeg, .jpg, .png",
    name: "avatar",
    showUploadList: false,
    beforeUpload(file: RcFile) {
      const isJpgOrPng =
        file.type === "image/jpeg" || file.type === "image/png";
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isJpgOrPng) {
        openNotification(
          "You can only upload JPG/PNG file!",
          "JPG/PNG files are only supported for the upload file type",
          "error"
        );
      } else if (!isLt2M) {
        openNotification(
          "Image must smaller than 2MB!",
          "Upload image file of less than 2MB",
          "error"
        );
      } else {
        getBase64(file as RcFile, (url) => {
          setNewUserDetails({ ...newUserDetails, profileImage: url });
          setIsUploaded(true);
        });
      }
    },
  };

  return (
    <>
      <OrpheaModal
        headingIcon={<AddUserIcon />}
        heading={getLanguageLabel("addNewUserDetails")}
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        onOk={handleOk}
        footerButtonArea={
          <OrpheaButton
            icon={<TickIcon />}
            intent="action"
            key="submit"
            onClick={handleOk}
          >
            {getLanguageLabel("create")}
          </OrpheaButton>
        }
        width={600}
      >
        <div
          style={{
            height: "60vh",
            overflow: "scoll",
          }}
        >
          <div className="OrpheaHeader1">{getLanguageLabel("userName")}</div>
          <OrpheaInput
            autofocus
            onChange={(e) =>
              setNewUserDetails({
                ...newUserDetails,
                username: e.target.value,
                name: e.target.value,
              })
            }
            value={newUserDetails.username}
            name="username"
            required
          />
          <div className="OrpheaHeader1">{getLanguageLabel("password")}</div>
          <Input.Password
            onChange={(e) =>
              setNewUserDetails({
                ...newUserDetails,
                password: e.target.value,
              })
            }
            value={newUserDetails.password}
            name="password"
            required
          />
          <div className="OrpheaHeader1">{getLanguageLabel("givenName")}</div>
          <OrpheaInput
            onChange={(e) =>
              setNewUserDetails({
                ...newUserDetails,
                givenName: e.target.value,
              })
            }
            value={newUserDetails.givenName}
            name="fname"
            required
          />
          <div className="OrpheaHeader1">{getLanguageLabel("familyName")}</div>
          <OrpheaInput
            onChange={(e) =>
              setNewUserDetails({
                ...newUserDetails,
                familyName: e.target.value,
              })
            }
            value={newUserDetails.familyName}
            name="lname"
            required
          />
          {/* <div className="OrpheaHeader1">{getLanguageLabel("name")}</div>
          <OrpheaInput
            bordered
            onChange={(e) =>
              setNewUserDetails({
                ...newUserDetails,
                name: e.target.value,
              })
            }
            value={newUserDetails.name}
            name="Uname"
            required
          /> */}
          <div className="OrpheaHeader1">{getLanguageLabel("location")}</div>
          <OrpheaInput
            onChange={(e) =>
              setNewUserDetails({
                ...newUserDetails,
                location: e.target.value,
              })
            }
            value={newUserDetails.location}
            name="loc"
            required
          />
          <div className="OrpheaHeader1">{getLanguageLabel("email")}</div>
          <OrpheaInput
            onChange={(e) =>
              setNewUserDetails({
                ...newUserDetails,
                email: e.target.value,
              })
            }
            value={newUserDetails.email}
            name="emailid"
            required
          />

          <div className="OrpheaHeader1">
            {getLanguageLabel("languagePreference")}
          </div>

          <Select
            defaultValue={newUserDetails.language}
            style={{ width: "100%" }}
            onChange={(selected) => {
              setNewUserDetails({
                ...newUserDetails,
                language: selected,
              });
            }}
          >
            <Option value="auto">
              {getLanguageLabel("auto")}{" "}
              <Text type="secondary">(Automatic)</Text>
              <br />
              <Text type="secondary">
                {getLanguageLabel("autoLanguageSubtitle")}
              </Text>
            </Option>
            <Option value="fr">
              <ReactCountryFlag countryCode="FR" svg />{" "}
              {getLanguageLabel("french")}{" "}
              <Text type="secondary">(French)</Text>
            </Option>
            <Option value="en">
              <ReactCountryFlag countryCode="GB" svg />{" "}
              {getLanguageLabel("english")}{" "}
              <Text type="secondary">(English)</Text>
            </Option>
            <Option value="de">
              <ReactCountryFlag countryCode="DE" svg />{" "}
              {getLanguageLabel("german")}{" "}
              <Text type="secondary">(German)</Text>
            </Option>
            <Option value="nl">
              <ReactCountryFlag countryCode="NL" svg />{" "}
              {getLanguageLabel("dutch")} <Text type="secondary">(Dutch)</Text>
            </Option>
            <Option value="hi">
              <ReactCountryFlag countryCode="IN" svg />{" "}
              {getLanguageLabel("hindi")} <Text type="secondary">(Hindi)</Text>
            </Option>
          </Select>

          <div>
            <div className="OrpheaHeader1">
              {getLanguageLabel("themePreference")}
            </div>
            <Radio.Group
              name="mode"
              onChange={(e) => {
                setNewUserDetails({
                  ...newUserDetails,
                  mode: e.target.value,
                });
              }}
              defaultValue={newUserDetails.mode}
              size="small"
            >
              <Radio.Button name="mode" value="auto">
                {getLanguageLabel("auto")}
              </Radio.Button>
              <Radio.Button name="mode" value="dark">
                {getLanguageLabel("dark")}
              </Radio.Button>
              <Radio.Button name="mode" value="light">
                {getLanguageLabel("light")}
              </Radio.Button>
            </Radio.Group>
          </div>

          <br />
          <Upload {...props}>
            <OrpheaButton icon={<UploadIcon />}>
              {getLanguageLabel("uploadProfilePicture")}
            </OrpheaButton>
          </Upload>
          {isUploaded && <TickIcon color="green" />}
        </div>
      </OrpheaModal>
    </>
  );
};

export default CreateNewUserModal;
