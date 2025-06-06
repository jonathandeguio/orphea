import cronstrue from "cronstrue/i18n";
export const getExpressionString = (expression: string, userLan: string) => {
  try {
    const res = cronstrue.toString(expression, {
      locale: userLan,
    });
    return res;
  } catch (e) {
    return "Not a valid cron Expression";
  }
};
