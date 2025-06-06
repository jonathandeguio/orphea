import { Select } from "antd";
import React, { useEffect, useState } from "react";
import { getAllBranch, getAllRegistry, getFilteredTriggers } from "../apis";
import styles from "./Filter.module.scss";

interface FilterProps {
  onFilterUpdate: (value: any) => void;
}
const Filters: React.FC<FilterProps> = ({ onFilterUpdate }) => {
  const [registryOptions, setRegistryOptions] = useState<string[]>([]);
  const [branchOptions, setBranchOptions] = useState<string[]>([]);
  const [selectedRegistry, setSelectedRegistry] = useState<string[]>([]);
  const [selectedBranch, setSelectedBranch] = useState<string[]>([]);

  useEffect(() => {
    const fetchFilterOptions = async () => {
      try {
        const [registryResponse, branchResponse] = await Promise.all([
          getAllRegistry(),
          getAllBranch(),
        ]);
        setRegistryOptions(registryResponse.data);
        setBranchOptions(branchResponse.data);
      } catch (error) {
        console.error("Error fetching filter options:", error);
      }
    };

    fetchFilterOptions();
  }, []);

  useEffect(() => {
    if (selectedRegistry || selectedBranch) {
      try {
        getFilteredTriggers(selectedRegistry, selectedBranch).then((res) => {
          onFilterUpdate(res.data);
        });
      } catch (error) {
        console.error("Error fetching table data:", error);
      }
    }
  }, [selectedRegistry, selectedBranch]);

  return (
    <div className={styles.filters} onClick={(e) => e.stopPropagation()}>
      <Select
        placeholder="Select Registry"
        options={registryOptions.map((option) => ({
          value: option,
          label: option,
        }))}
        onChange={(value) => setSelectedRegistry(value)}
        style={{ width: 200, marginRight: 8 }}
        mode="multiple"
      />
      <Select
        placeholder="Select Branch"
        options={branchOptions.map((option) => ({
          value: option,
          label: option,
        }))}
        onChange={(value) => setSelectedBranch(value)}
        style={{ width: 200 }}
        mode="multiple"
      />
    </div>
  );
};

export default Filters;
