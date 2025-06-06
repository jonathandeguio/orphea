import { Checkbox } from "antd";
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { IDropdown } from "./HeaderSearch.types";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { getLanguageLabel } from "utils/utilities";

export const SelectUserFilter: React.FC<IDropdown> = ({ state, onChange }) => {
  const { user } = useSelector((state: RootState) => state.userDetails);
  const [createdBy, setCreatedBy] = useState<string[]>([user.id]);
  const { allusers } = useSelector((state: RootState) => state.allUserDetails);
  const [allUsersArr, setAllUsersArr] = useState(allusers);
  const handleUserChecked = (key: string, id: string) => {
    let newUsersArr: string[] = [];
    if (createdBy.includes(id)) {
      newUsersArr = createdBy.filter((e) => e != id);
    } else {
      newUsersArr = [...createdBy, id];
    }
    setCreatedBy(newUsersArr);
    onChange(key, id);
  };

  useEffect(() => {
    setCreatedBy(state);
  }, [state]);
  const handleSearchInputChange = (val: string) => {
    if (val.length > 0) {
      const filteredUsers = allusers.filter((e: any) =>
        e.name.toLowerCase().includes(val.toLowerCase())
      );
      setAllUsersArr(filteredUsers);
    } else {
      setAllUsersArr(allusers);
    }
  };
  return (
    <>
      <div>
        <BoslerInput
          placeholder={getLanguageLabel("searchByUser")}
          onChange={(e) => handleSearchInputChange(e?.target?.value)}
        />
      </div>
      {allUsersArr?.map((usr: any) => (
        <div>
          <Checkbox
            onChange={() => handleUserChecked("createdBy", usr.id)}
            checked={createdBy?.includes(usr.id)}
            value={usr.id}
          >
            {usr?.name}
          </Checkbox>
        </div>
      ))}
    </>
  );
};
