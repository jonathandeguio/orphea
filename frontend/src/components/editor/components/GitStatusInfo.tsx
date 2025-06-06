import React from "react";

const GitStatusInfo = () => {
  const statuses = [
    { className: "git-clean", label: "Clean (No Changes)", color: "#8bbf8b" },
    { className: "git-removed", label: "Removed", color: "#e06c75" },
    { className: "git-modified", label: "Modified", color: "#e5c07b" },
    {
      className: "git-uncommitted",
      label: "Uncommitted Changes",
      color: "#61afef",
    },
    { className: "git-added", label: "Added", color: "#98c379" },
    { className: "git-changed", label: "Changed", color: "#d19a66" },
    { className: "git-conflicting", label: "Conflicting", color: "#c678dd" },
    { className: "git-ignored", label: "Ignored", color: "#7f848e" },
    { className: "git-missing", label: "Missing", color: "#e06c75" },
    { className: "git-untracked", label: "Untracked", color: "#61afef" },
    {
      className: "git-untracked-folder",
      label: "Untracked Folder",
      color: "#61afef",
    },
  ];

  console.log("GIt status info");
  return (
    <div className="git-status-info">
      <h3>Git Status Colors</h3>
      <ul>
        {statuses.map((status) => (
          <li key={status.className} className={status.className}>
            <span
              style={{ backgroundColor: status.color }}
              className="color-indicator"
            ></span>
            {status.label}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default GitStatusInfo;
