const ColsSearch = (
  searchInput: $TSFixMe,
  data: $TSFixMe,
  columns: $TSFixMe
) => {
  const filteredCols = columns.filter((obj: $TSFixMe) => {
    obj = obj.dataIndex;
    if (obj.toString().toLowerCase().includes(searchInput.toLowerCase()))
      return true;
  });
  return filteredCols;
};

export default ColsSearch;
