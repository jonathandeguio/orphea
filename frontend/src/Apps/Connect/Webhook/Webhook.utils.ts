import { IWebhook, IWebhookRequest } from "./Webhook.types";

const selectedKeys = ["queryParams", "headers", "formData"];

export const convertWebhookFieldsToString = (webhook: IWebhook) => {
  let newWebhook: any = { ...webhook };
  newWebhook.requests = webhook?.requests?.map((request) => {
    const newRequest = { ...request };

    for (const key of Object.keys(newRequest)) {
      if (
        selectedKeys.includes(key) &&
        Array.isArray((newRequest as any)[key])
      ) {
        (newRequest as any)[key] = JSON.stringify(
          (newRequest as any)[key]
        ) as string;
      }
    }

    return newRequest;
  });

  return newWebhook;
};

export const convertWebhookFieldsToJson = (webhook: IWebhook) => {
  let newWebhook: any = { ...webhook };

  newWebhook.requests = webhook?.requests?.map((request) => {
    const newRequest = { ...request };

    for (const key of Object.keys(newRequest)) {
      if (selectedKeys.includes(key)) {
        (newRequest as any)[key] = JSON.parse((newRequest as any)[key]);
      }
    }

    return newRequest;
  });

  return newWebhook;
};

export const convertWebhookRequestsFieldsToString = (
  requests: IWebhookRequest[] | undefined
) => {
  if (!requests) {
    return [];
  }
  return requests.map((request: any) => {
    const newRequest = { ...request };

    for (const key of Object.keys(newRequest)) {
      if (
        selectedKeys.includes(key) &&
        Array.isArray((newRequest as any)[key])
      ) {
        (newRequest as any)[key] = JSON.stringify(
          (newRequest as any)[key]
        ) as string;
      }
    }

    return newRequest;
  });
};

export const convertWebhookRequestsFieldsToJson = (
  requests: IWebhookRequest[]
) => {
  return requests.map((request) => {
    const newRequest = { ...request };

    for (const key of Object.keys(newRequest)) {
      if (selectedKeys.includes(key)) {
        (newRequest as any)[key] = JSON.parse((newRequest as any)[key]);
      }
    }

    return newRequest;
  });
};
