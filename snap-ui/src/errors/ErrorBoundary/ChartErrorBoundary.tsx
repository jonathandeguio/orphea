import { WarningState } from "assets/Illustrations/EmptyState";
import axios, { AxiosResponse } from "axios";
import React, { Component, ErrorInfo, ReactNode } from "react";
import NoData from "components/NoData";

interface Props {
  children?: ReactNode;
}

interface State {
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

const ErrorType = {
  pageNotFound: "pageNotFound",
  retry: "retry",
  error: "error",
};

const logError = (body: any): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/errors`, body);
};

class ChartErrorBoundary extends Component<Props, State> {
  constructor(props: any) {
    super(props);
    this.state = { error: null, errorInfo: null };
  }

  public static getDerivedStateFromError(_: Error): Error {
    return _;
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.log("error", error.name, error.message, error.stack);
    console.log("errorInfo", errorInfo.componentStack);

    logError({
      name: error.name,
      message: error.message,
      stack: error.stack,
      componentStack: errorInfo.componentStack,
    });

    this.setState({
      error: error,
      errorInfo: errorInfo,
    });
  }

  public render() {
    if (this.state.errorInfo) {
      return (
        <NoData
          icon={<WarningState />}
          heading={"Oops chart failed to load!"}
        />
      );
    }
    return this.props.children;
  }
}

export default ChartErrorBoundary;
