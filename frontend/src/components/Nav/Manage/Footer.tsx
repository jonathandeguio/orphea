import React from "react";
import { useParams } from "react-router-dom";

const Footer = () => {
  // --------------Router---------------
  const { id, branch } = useParams();
  return <></>;
  // return (
  //   <div className="footer" style={{ width: "100%" }}>
  //     <div className="footer-left">
  //       <div className="footer-left-item">
  //         <Dropdown overlay={menu} placement="topLeft">
  //           <div style={{ display: "inline-block" }}>
  //             <AddIcon />
  //             &nbsp;
  //             {branch}
  //             &nbsp;
  //             <SingleChevronDownIcon />
  //           </div>
  //         </Dropdown>
  //       </div>
  //       {/* <div className="footer-left-item">
  //                   <EditIcon />
  //                   &nbsp;
  //                   Rename
  //               </div>
  //               <div className="footer-left-item">
  //                   Create
  //               </div> */}
  //       <div className="footer-left-item">
  //         {getLanguageLabel("menu")} &nbsp;
  //         <SingleChevronDownIcon />
  //       </div>
  //     </div>
  //     <div className="footer-right">
  //       <div className="footer-right-item">
  //         <SingleChevronDownIcon />
  //         &nbsp; {getLanguageLabel("notification")}
  //       </div>
  //     </div>
  //   </div>
  // );
};

export default Footer;
