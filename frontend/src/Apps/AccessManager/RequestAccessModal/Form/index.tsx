import { IRequestAccessReview } from "Apps/AccessManager/AccessManager";
import React, { Dispatch, SetStateAction } from "react";
import { AddDetails } from "./AddDetails";
import { AddJustification } from "./AddJustification";
import { ReviewForm } from "./ReviewForm";
import { SelectRequesters } from "./SelectRequesters";

interface IProps {
  currentStep: number;
  accessRequest: IRequestAccessReview;
  setAccessRequest: Dispatch<SetStateAction<IRequestAccessReview>>;
}

export const RequestAccessForm = ({
  accessRequest,
  setAccessRequest,
  currentStep,
}: IProps) => {
  switch (currentStep) {
    case 0:
      return (
        <AddDetails
          accessRequest={accessRequest}
          setAccessRequest={setAccessRequest}
        />
      );
    case 1:
      return (
        <SelectRequesters
          accessRequest={accessRequest}
          setAccessRequest={setAccessRequest}
        />
      );
    case 2:
      return (
        <AddJustification
          accessRequest={accessRequest}
          setAccessRequest={setAccessRequest}
        />
      );
    case 3:
      return <ReviewForm accessRequest={accessRequest} />;
    default:
      return <></>;
  }
};
