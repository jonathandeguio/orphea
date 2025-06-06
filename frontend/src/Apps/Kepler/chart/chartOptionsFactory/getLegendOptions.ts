export const getLegendOptions = (chartCustomization: any) => {
  return {
    show: chartCustomization.legend,
    type: chartCustomization.legendType,
    orient: "horizontal",
    icon: "rect",
    itemWidth: 15,
    itemHeight: 15,
    ...(chartCustomization.legendPosition === "right"
      ? {
          orient: "vertical",
          right: 30,
          top: 10,
          textStyle: {
            width: 120,
            overflow: "truncate",
          },
        }
      : {}),
  };
};
