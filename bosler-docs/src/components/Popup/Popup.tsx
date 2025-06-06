import React, { useState, useRef, useEffect } from "react";
import styles from "./styles.module.css";
import ReactPlayer from "react-player"

const Popup = ({ children, heading, subheading, videoUrl }) => {
  const [isOpen, setIsOpen] = useState(false);
  const popupRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (popupRef.current && !popupRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };

    if(isOpen) document.addEventListener("click", handleClickOutside);

    return () => {
      document.removeEventListener("click", handleClickOutside);
    };
  }, [isOpen]);

  return (
    <>
      <div onClick={() => setIsOpen(true)} className={styles.popupTrigger}>
        {children}
      </div>

      {isOpen && (
        <div className={styles.popup}>
          <div ref={popupRef} className={styles.popupContent}>
          <div className={styles.info}>
            <h3>{heading}</h3>
            <span>{subheading}</span>
          </div>
            <button className={styles.closeButton} onClick={() => setIsOpen(false)}>
              &times;
            </button>
            <br/>
            <div className="video__wrapper">
              {/* @ts-ignore */}
              <ReactPlayer className="video__player" controls height="100%" url={videoUrl} width="100%" />
            </div>
          </div>
        </div>
        

      )}
    </>
  );
};

export default Popup;