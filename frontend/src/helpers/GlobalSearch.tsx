const GlobalSearch = (
  searchInput: $TSFixMe,
  data: $TSFixMe,
  columns: $TSFixMe
) => {
  const filteredData = data.filter((value: $TSFixMe) => {
    let rows = false;
    columns !== undefined &&
      columns.forEach(({ dataIndex }: $TSFixMe) => {
        const obj = value[dataIndex];

        if (obj !== undefined && obj !== null) {
          rows =
            rows ||
            obj.toString().toLowerCase().includes(searchInput.toLowerCase());
          if (rows) {
            return true;
          }
        }
      });

    return rows;
  });

  return filteredData;
};

export default GlobalSearch;
