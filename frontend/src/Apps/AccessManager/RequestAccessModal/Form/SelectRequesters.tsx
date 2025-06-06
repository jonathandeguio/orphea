import { IRequestAccessReview } from "Apps/AccessManager/AccessManager";
import { Avatar, Col, Form, Row, Select } from "antd";
import { User } from "global";
import { Group } from "pages/Settings/Groups/Group";
import React, { Dispatch, SetStateAction, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel } from "utils/utilities";
import { getAllUserDetails } from "../../../../redux/actions/userActions";
import { RootState, ThunkAppDispatch } from "../../../../redux/types/store";
import styles from "../../AcccessManager.module.scss";
import { getAllResourceGroupsAPI } from "../RequestAccessModal.api";
import {
  REQUEST_ACCESS_TYPE,
  ROLES,
  getAllIdentityChoices,
} from "../RequestAccessModal.utils";

interface IProps {
  accessRequest: IRequestAccessReview;
  setAccessRequest: Dispatch<SetStateAction<IRequestAccessReview>>;
}

const { Item } = Form;
const { Option } = Select;

export const SelectRequesters = ({
  accessRequest,
  setAccessRequest,
}: IProps) => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const { allusers } = useSelector((state: RootState) => state.allUserDetails);
  const [resourceGroups, setResourceGroups] = useState([]);

  const isTypeProject = accessRequest.type == REQUEST_ACCESS_TYPE.PROJECT;

  return (
    <>
      <Item label={"Requesters"}>
        <Select
          mode="multiple"
          showSearch
          allowClear
          optionFilterProp="value"
          value={accessRequest.requesters}
          onClear={() =>
            setAccessRequest({
              ...accessRequest,
              requesters: [],
              requesterNames: [],
            })
          }
          onSelect={(value, option) =>
            setAccessRequest({
              ...accessRequest,
              requesters: [...accessRequest.requesters, value],
              requesterNames: [...accessRequest.requesterNames, option.name],
            })
          }
          onDeselect={(value, option) =>
            setAccessRequest({
              ...accessRequest,
              requesters: accessRequest.requesters.filter(
                (_value) => _value != value
              ),
              requesterNames: accessRequest.requesterNames.filter(
                (_value) => _value != option.name
              ),
            })
          }
          style={{ width: "100%" }}
          placeholder={getLanguageLabel("addPeopleOrGroups")}
          onFocus={() => {
            dispatch(getAllUserDetails());
            getAllResourceGroupsAPI().then(({ data }) =>
              setResourceGroups(data)
            );
          }}
          filterOption={(input, option) =>
            option?.name.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          {getAllIdentityChoices(allusers, resourceGroups, isTypeProject).map(
            (i: User | Group | any) => {
              return (
                <Option value={i.id} key={i.id} name={i.name}>
                  <Row gutter={[8, 8]} align={"middle"}>
                    <Col>
                      <Avatar
                        className={styles.avatar}
                        src={i?.profileImage != "" ? i.profileImage : null}
                      >
                        {i.name ? i.name.charAt(0).toUpperCase() : "B"}
                      </Avatar>
                    </Col>
                    <Col>
                      {i.name || i.id} {i.username && `(${i.username})`}
                    </Col>
                  </Row>
                </Option>
              );
            }
          )}
        </Select>
      </Item>
      <Item label={"Role"}>
        <Select
          value={accessRequest.role}
          onChange={(value) =>
            setAccessRequest({ ...accessRequest, role: value })
          }
        >
          {Object.values(ROLES).map((role) => {
            return (
              <Option value={role.name} key={role.id}>
                <div className="text-and-icon-center">
                  {accessRequest.type == REQUEST_ACCESS_TYPE.ADMINISTRATOR
                    ? role.administratorName
                    : role.name}{" "}
                  {role.icon}
                </div>
              </Option>
            );
          })}
        </Select>
      </Item>
    </>
  );
};
