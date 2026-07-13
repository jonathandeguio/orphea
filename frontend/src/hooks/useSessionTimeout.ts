import axios from "axios";
import { useCallback, useEffect, useRef, useState } from "react";

const ACTIVITY_EVENTS = ["mousemove", "mousedown", "keydown", "scroll", "touchstart", "click"];
const PING_INTERVAL_MS = 60_000;
const WARNING_BEFORE_MS = 120_000;
const BC_CHANNEL = "session-activity";

interface SessionTimeoutOptions {
  onTimeout: () => void;
  enabled?: boolean;
}

interface SessionTimeoutState {
  warningVisible: boolean;
  secondsLeft: number;
  extendSession: () => void;
}

export function useSessionTimeout({ onTimeout, enabled = true }: SessionTimeoutOptions): SessionTimeoutState {
  const [warningVisible, setWarningVisible] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(120);

  const timeoutMinutesRef = useRef(30);
  const lastActivityRef = useRef(Date.now());
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const pingTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const countdownRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const warningShownAtRef = useRef<number | null>(null);

  const clearTimers = useCallback(() => {
    if (timerRef.current) clearTimeout(timerRef.current);
    if (pingTimerRef.current) clearInterval(pingTimerRef.current);
    if (countdownRef.current) clearInterval(countdownRef.current);
  }, []);

  const ping = useCallback(async () => {
    try {
      const { data } = await axios.post("/session/ping");
      if (data?.sessionTimeoutMinutes) {
        timeoutMinutesRef.current = data.sessionTimeoutMinutes;
      }
    } catch {
      // ignore ping failures
    }
  }, []);

  const scheduleTimeout = useCallback(() => {
    if (timerRef.current) clearTimeout(timerRef.current);
    if (countdownRef.current) clearInterval(countdownRef.current);

    const timeoutMs = timeoutMinutesRef.current * 60_000;
    const warnAt = timeoutMs - WARNING_BEFORE_MS;

    timerRef.current = setTimeout(() => {
      setWarningVisible(true);
      warningShownAtRef.current = Date.now();
      setSecondsLeft(Math.floor(WARNING_BEFORE_MS / 1000));

      countdownRef.current = setInterval(() => {
        const elapsed = Date.now() - (warningShownAtRef.current ?? Date.now());
        const remaining = Math.max(0, Math.floor((WARNING_BEFORE_MS - elapsed) / 1000));
        setSecondsLeft(remaining);
        if (remaining <= 0) {
          if (countdownRef.current) clearInterval(countdownRef.current);
          setWarningVisible(false);
          onTimeout();
        }
      }, 1000);
    }, warnAt);
  }, [onTimeout]);

  const resetActivity = useCallback(() => {
    lastActivityRef.current = Date.now();
    setWarningVisible(false);
    warningShownAtRef.current = null;
    scheduleTimeout();
  }, [scheduleTimeout]);

  const extendSession = useCallback(async () => {
    await ping();
    resetActivity();
  }, [ping, resetActivity]);

  useEffect(() => {
    if (!enabled) return;

    // Load initial timeout config
    axios.get("/session/config")
      .then(({ data }) => {
        if (data?.sessionTimeoutMinutes) {
          timeoutMinutesRef.current = data.sessionTimeoutMinutes;
        }
      })
      .catch(() => {})
      .finally(() => {
        scheduleTimeout();
      });

    // Activity listeners
    const handler = () => {
      resetActivity();

      // Broadcast to other tabs
      try {
        const bc = new BroadcastChannel(BC_CHANNEL);
        bc.postMessage({ type: "activity", at: Date.now() });
        bc.close();
      } catch {
        // BroadcastChannel not available in some environments
      }
    };

    ACTIVITY_EVENTS.forEach(e => window.addEventListener(e, handler, { passive: true }));

    // Listen to other tabs
    let bc: BroadcastChannel | null = null;
    try {
      bc = new BroadcastChannel(BC_CHANNEL);
      bc.onmessage = (event) => {
        if (event.data?.type === "activity") {
          resetActivity();
        } else if (event.data?.type === "logout") {
          onTimeout();
        }
      };
    } catch {
      // ignore
    }

    // Periodic ping to keep server-side lastActivityAt fresh
    pingTimerRef.current = setInterval(ping, PING_INTERVAL_MS);

    return () => {
      clearTimers();
      ACTIVITY_EVENTS.forEach(e => window.removeEventListener(e, handler));
      bc?.close();
    };
  }, []);

  return { warningVisible, secondsLeft, extendSession };
}
