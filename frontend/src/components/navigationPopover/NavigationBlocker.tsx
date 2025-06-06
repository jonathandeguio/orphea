import {
  CrossIcon,
  SaveIcon,
  WarningIcon,
} from "assets/icons/boslerActionIcons";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import * as React from "react";
import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router";
import { unstable_useBlocker as useBlocker } from "react-router-dom";
import { getLanguageLabel } from "utils/utilities";

interface IProps {
  onSave: () => void;
  isBlocking: boolean;
  ignoreLastSegmentChange?: boolean;
  cancelButtonText?: string;
}

function useCallbackPrompt(when: any, ignoreLastSegmentChange: boolean) {
  const navigate = useNavigate();
  const location = useLocation();
  const [showPrompt, setShowPrompt] = useState(false);
  const [lastLocation, setLastLocation] = useState(null);
  const [confirmedNavigation, setConfirmedNavigation] = useState(false);

  const cancelNavigation = useCallback(() => {
    setShowPrompt(false);
    setConfirmedNavigation(false);
  }, []);

  const handleBlockedNavigation = useCallback(
    ({ nextLocation }: any) => {
      if (when && !confirmedNavigation) {
        if (ignoreLastSegmentChange) {
          const currentPathSegments = location.pathname.split("/");
          const nextPathSegments = nextLocation.pathname.split("/");

          // Check if all path segments except the last one are the same
          const isSameExceptLast =
            currentPathSegments.length === nextPathSegments.length &&
            currentPathSegments
              .slice(0, -1)
              .every((seg, i) => seg === nextPathSegments[i]);

          // If only the last segment has changed, skip blocking
          console.log(currentPathSegments);
          console.log(nextPathSegments);
          if (isSameExceptLast) {
            return false; // Allow navigation
          }
        }

        if (nextLocation.pathname !== location.pathname) {
          setShowPrompt(true);
          setLastLocation(nextLocation);
          return true; // Block the navigation
        }
      }
      return false; // Allow navigation
    },
    [confirmedNavigation, when, location.pathname, ignoreLastSegmentChange]
  );

  const confirmNavigation = useCallback(() => {
    setShowPrompt(false);
    setConfirmedNavigation(true);
  }, []);

  useEffect(() => {
    if (confirmedNavigation && lastLocation) {
      navigate((lastLocation as any).pathname);
      setConfirmedNavigation(false);
      setLastLocation(null);
    }
  }, [confirmedNavigation, lastLocation, navigate]);

  useBlocker(handleBlockedNavigation);

  return [showPrompt, confirmNavigation, cancelNavigation];
}

const NavigationBlocker = ({
  isBlocking,
  onSave = () => {},
  ignoreLastSegmentChange = false,
  cancelButtonText,
}: IProps) => {
  const [showPrompt, confirmNavigation, cancelNavigation] = useCallbackPrompt(
    isBlocking,
    ignoreLastSegmentChange
  );

  return (
    <BoslerModal
      headingIcon={<WarningIcon color="#FFA500" />}
      heading={getLanguageLabel("unSaved")}
      open={showPrompt as any}
      onCancel={cancelNavigation as any}
      okType="danger"
      extraActionHeading={
        <BoslerButton
          icon={<CrossIcon />}
          onClick={cancelNavigation}
          minimal
          icononly
          trimicononlypadding
        ></BoslerButton>
      }
      footerButtonArea={
        <>
          <BoslerButton
            icon={<SaveIcon />}
            intent="action"
            key="submit"
            onClick={() => {
              onSave();
              (confirmNavigation as any)();
            }}
            textTransform="capitalize"
          >
            {getLanguageLabel("save")}
          </BoslerButton>
          <BoslerButton
            icon={<TrashIcon />}
            intent="dangerous"
            key="submit"
            onClick={confirmNavigation as any}
          >
            {cancelButtonText ? cancelButtonText : getLanguageLabel("cancel")}
          </BoslerButton>
        </>
      }
    >
      {getLanguageLabel("unsavedMsg")}
    </BoslerModal>
  );
};
export default NavigationBlocker;
