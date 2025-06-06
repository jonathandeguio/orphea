export type cacheCallbacks = {
  resolveCallback: (resource: Resource) => void;
  errorCallback?: (error: any) => void;
  finallyCallback?: () => void;
};
