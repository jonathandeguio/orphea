// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docs: [
    {
      type: "html",
      value: "Getting Started",
    },
    "getting_started/intro",
    "getting_started/projects",
    "getting_started/architecture",

    {
      type: "html",
      value: "Ingestion",
    },
    "ingestion/overview",
    "ingestion/installagent",
    "ingestion/datasources",
    "ingestion/datasetlinks",
    "ingestion/scheduling",

    {
      type: "html",
      value: "Transformation",
    },
    "transform/overview",
    {
      type: "category",
      label: "Code Repository",
      collapsed: false,
      items: [
        "transform/code_repository/code_repository",
        "transform/code_repository/pyspark_basics",
        "transform/code_repository/sql_basics",
        "transform/code_repository/notebook",
      ],
    },
    "transform/builds",
    "transform/datalineage",
    "transform/scheduling",
    "transform/branches",

    {
      type: "html",
      value: "Data Health",
    },
    "data_health/overview",
    "data_health/status",
    "data_health/time",
    "data_health/size",
    "data_health/content",
    "data_health/schema",

    {
      type: "html",
      value: "Analyse",
    },
    "analyse/overview",
    "analyse/dashboard",
    "analyse/dataset",
    "analyse/stats",

    {
      type: "category",
      label: "Charts",
      items: [
        "analyse/charts/chart",
        "analyse/charts/piechart",
        "analyse/charts/barchart",
        "analyse/charts/linechart",
        "analyse/charts/areachart",
        "analyse/charts/scattererchart",
        "analyse/charts/horizontalaxischart",
        "analyse/charts/bignumberchart",
        "analyse/charts/tablechart",
        "analyse/charts/gaugechart",
        "analyse/charts/radarchart",
        "analyse/charts/sunburstchart",
        "analyse/charts/waterfallchart",
        "analyse/charts/treemapchart",
        "analyse/charts/mapchart",
        "analyse/charts/wordcloudchart",
        "analyse/charts/parameterchart",
      ],
    },

    {
      type: "html",
      value: "Security",
    },
    "security/overview",
    "security/tokens",
    "security/usermanagement",
    "security/groupmanagement",
    "security/resourcesecurity",

    {
      type: "html",
      value: "API Documentation",
    },
    "apidocs/overview",
    "apidocs/documentation",
  ],
  tutorials: [
    { type: "ref", label: "Overview", id: "tutorials/overview" },
    {
      type: "html",
      value: "Connect Data",
    },

    "tutorials/connect_data/upload",
    "tutorials/connect_data/upload_xls",
    "tutorials/connect_data/ignite",

    {
      type: "html",
      value: "Transform",
    },

    "tutorials/transform/SQL_transform",
    "tutorials/transform/Python_transform",
    "tutorials/transform/data_mart",
    {
      type: "html",
      value: "Visualize",
    },
    "tutorials/visualize/chart",
    "tutorials/visualize/dashboard",
    "tutorials/visualize/column_stats",
  ],
};

module.exports = sidebars;
