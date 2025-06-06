import React from 'react';
import './BoslerModalWrapper.scss'
interface IBoslerModalWrapperProps {
  children: React.ReactNode;
}

const BoslerModalWrapper: React.FC<IBoslerModalWrapperProps> = ({ children }) => {
  return (
    <div
      className='BoslerModalWrapper'
    >
      {children}
    </div>
  );
};

export default BoslerModalWrapper;
