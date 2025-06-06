// import axios, { AxiosResponse } from "axios";
// import React, { Component, ErrorInfo, ReactNode } from "react";
// import ErrorPage from "../pages/Errors/ErrorPage";

// interface Props {
//   children?: ReactNode;
// }

// interface State {
//   error: Error | null;
//   errorInfo: ErrorInfo | null;
// }

// const ErrorType = {
//   pageNotFound: "pageNotFound",
//   retry: "retry",
//   error: "error",
// };

// const logError = (body: any): Promise<AxiosResponse<any, any>> => {
//   return axios.post(`/errors`, body);
// };

// class ErrorBoundary extends Component<Props, State> {
//   constructor(props: any) {
//     super(props);
//     this.state = { error: null, errorInfo: null };
//   }

//   // Updates the state to reflect that an error has occurred.
//   public static getDerivedStateFromError(error: Error): Partial<State> {
//     return { error, errorInfo: null }; // Ensure you return a partial state object
//   }
//   public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
//     console.error("error", error.name, error.message, error.stack);
//     console.error("errorInfo", errorInfo.componentStack);

//     logError({
//       name: error.name,
//       message: error.message,
//       stack: error.stack,
//       componentStack: errorInfo.componentStack,
//     });

//     this.setState({
//       error: error,
//       errorInfo: errorInfo,
//     });
//   }

//   public render() {
//     if (this.state.errorInfo) {
//       return <ErrorPage type={ErrorType.error} />;
//     }
//     return this.props.children;
//   }
// }

// export default ErrorBoundary;
