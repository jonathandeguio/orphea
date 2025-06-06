import axios from "axios";

const createCancelablePromise = (promiseFunction: any) => {
  const source = axios.CancelToken.source();
  const wrappedPromise = promiseFunction(source.token);

  return {
    promise: wrappedPromise,
    cancel: () => source.cancel("API request cancelled"),
  };
};

export default createCancelablePromise;
