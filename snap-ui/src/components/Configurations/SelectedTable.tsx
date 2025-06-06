import React from "react";

function SelectedTable({ selectedRecord }: any) {
  return (
    <div style={{ overflowX: "auto" }}>
      <table
        style={{
          width: "100%",
          borderCollapse: "collapse",
          marginTop: "16px",
        }}
      >
        <thead>
          <tr
            style={{
              textAlign: "left",
              borderBottom: "2px solid #ddd",
            }}
          >
            <th style={{ padding: "8px" }}>Components</th>
            <th style={{ padding: "8px" }}>Versions</th>
          </tr>
        </thead>
        <tbody>
          {Object.keys(selectedRecord)
            .filter((key) => !["id", "state", "deployedAt"].includes(key))
            .map((key) => (
              <tr key={key} style={{ borderBottom: "1px solid #ddd" }}>
                <td style={{ padding: "8px" }}>{key}</td>
                <td style={{ padding: "8px" }}>{selectedRecord[key]}</td>
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
}

export default SelectedTable;
