import {
  ALL_ARTIFACT_DETAILS_FAIL,
  ALL_ARTIFACT_DETAILS_REQUEST,
  ALL_ARTIFACT_DETAILS_RESET,
  ALL_ARTIFACT_DETAILS_SUCCESS,
} from "../constants/artifactConstants";

export const allArtifactDetailsReducer = (
  state = { allArtifacts: undefined, loading: true, error: false },
  action: $TSFixMe
) => {
  switch (action.type) {
    case ALL_ARTIFACT_DETAILS_REQUEST:
      return { allArtifacts: state.allArtifacts, loading: true, error: false };

    case ALL_ARTIFACT_DETAILS_SUCCESS:
      return {
        allArtifacts: action.payload,
        loading: false,
        error: false,
      };

    case ALL_ARTIFACT_DETAILS_FAIL:
      return { allArtifacts: undefined, loading: false, error: action.payload };

    case ALL_ARTIFACT_DETAILS_RESET:
      return { allArtifacts: undefined, loading: false, error: false };

    default:
      return state;
  }
};
