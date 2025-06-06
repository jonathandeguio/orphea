import { Select, Typography, message } from "antd";
import axios from "axios";
import useEffectOnlyOnDependencyUpdate from "hooks/useEffectOnlyOnDependencyUpdate";
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useSearchParams } from "react-router-dom";
import { RootState } from "redux/types/store";
import { getLanguageLabel, notEmpty } from "utils/utilities";
import { isValidScript } from "../editor.api";
const { Option } = Select;

const { Text, Title } = Typography;

interface TProps {
  id: string;
  branch: string;
}

const HardwareSpecs = ({ id, branch }: TProps) => {
  const [searchParams] = useSearchParams();
  const [cores, setCores] = useState(1);
  const [memory, setMemory] = useState("512M");
  const [numberOfExecutors, setNumberOfExecutors] = useState(1);
  const [failureRetries, setFailureRetries] = useState(0);
  const { editorPanes, activeId } = useSelector(
    (state: RootState) => state.repositoryEditor
  );

  const getHardwareSpecifications = async () => {
    if (!(branch && id && searchParams.get("f"))) {
      return;
    }

    const payload = {
      branch: branch,
      repository: id,
      scriptPath: searchParams.get("f"),
    };

    try {
      const { data } = await axios.post(`/build/hardwareSpecs`, payload);
      if (data) {
        setCores(data.cores);
        setMemory(data.memory);
        setNumberOfExecutors(data.numberOfExecutors);
        setFailureRetries(data.failureRetries);
      }
    } catch (error) {
      message.error("Failed to get hardware specs");
    }
  };

  const updateHardwareSpecifications = async () => {
    if (!(id && searchParams.get("f") && branch)) {
      message.error("Reopen the file, incomplete params");
      return;
    }
    const payload = {
      branch: branch,
      repository: id,
      scriptPath: searchParams.get("f"),
      cores: cores,
      memory: memory,
      numberOfExecutors: numberOfExecutors,
      failureRetries: failureRetries,
    };

    try {
      await axios.put(`/build/hardwareSpecs`, payload);
    } catch (error) {
      message.error("Failed to update hardware specs");
    }
  };

  useEffectOnlyOnDependencyUpdate(() => {
    updateHardwareSpecifications();
  }, [cores, memory, failureRetries, numberOfExecutors]);

  useEffect(() => {
    if (notEmpty(activeId) && notEmpty(editorPanes[activeId])) {
      // navigate(location.pathname + "?f=" + `${editorPanes[activeId].path}`, {
      //   replace: true,
      // });
      if (isValidScript(editorPanes[activeId].path))
        getHardwareSpecifications();
    }
  }, [activeId]);

  return (
    <>
      {getLanguageLabel("buildScript")}:&nbsp;&nbsp;&nbsp;
      {notEmpty(activeId) &&
      notEmpty(editorPanes[activeId]) &&
      isValidScript(editorPanes[activeId].path) ? (
        <b>{editorPanes[activeId].path}</b>
      ) : (
        <p
          style={{
            display: "inline",
            color: "red",
          }}
        >
          {getLanguageLabel("noScriptSpecified")}
        </p>
      )}
      <br />
      <table>
        <tr>
          <td>
            {" "}
            <Text type="secondary">{getLanguageLabel("sparkCPUCores")}</Text>
          </td>
          <td>
            <Select defaultValue={cores} value={cores} onChange={setCores}>
              <Option value={1}>1</Option>
              <Option value={2}>2</Option>
              <Option value={3}>3</Option>
              <Option value={4}>4</Option>
            </Select>
          </td>
        </tr>
        <tr>
          <td>
            <Text type="secondary">{getLanguageLabel("sparkMemory")}</Text>
          </td>
          <td>
            <Select defaultValue={memory} value={memory} onChange={setMemory}>
              <Option value={"512M"}>512M</Option>
              <Option value={"1024M"}>1024M</Option>
              <Option value={"2048M"}>2048M</Option>
              <Option value={"4096M"}>4096M</Option>
            </Select>
          </td>
        </tr>
        <tr>
          <td>
            <Text type="secondary">
              {getLanguageLabel("numberOfExecutors")}
            </Text>
          </td>
          <td>
            <Select
              defaultValue={numberOfExecutors}
              value={numberOfExecutors}
              onChange={setNumberOfExecutors}
            >
              <Option value={1}>1</Option>
              <Option value={2}>2</Option>
              <Option value={4}>4</Option>
              <Option value={8}>8</Option>
              <Option value={16}>16</Option>
            </Select>
          </td>
        </tr>
        <tr>
          <td>
            {" "}
            <Text type="secondary">Failure Retries</Text>
          </td>
          <td>
            <Select
              defaultValue={failureRetries}
              value={failureRetries}
              onChange={setFailureRetries}
            >
              <Option value={0}>0</Option>
              <Option value={1}>1</Option>
              <Option value={2}>2</Option>
              <Option value={3}>3</Option>
              <Option value={4}>4</Option>
            </Select>
          </td>
        </tr>
      </table>
    </>
  );
};

export default HardwareSpecs;
