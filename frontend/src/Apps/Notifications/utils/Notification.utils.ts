export const getNotificationPrefix = (type: string) => {
  switch (type) {
    case NOTIFICATION_TYPES.ACCESS_REQUEST:
      return "has requested access";
    case NOTIFICATION_TYPES.MENTION:
      return "has mentioned you in a comment";
    default:
      return "";
  }
};

export const NOTIFICATION_TYPES = {
  ACCESS_REQUEST: "ACCESS_REQUEST",
  MENTION: "MENTION",
};
