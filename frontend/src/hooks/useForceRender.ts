import { useState } from "react";

/*
Example Usage : 
function MyComponent = () => {
    const forceRender = useForceRender();

    const handleButtonClick = () => {
        // Call forceRender whenever you want to trigger a re-render
        forceRender();
    }

    return (
        <>
        ...
        </>
    );

};
*/

function useForceRender() {
  const [_, setForceRender] = useState(false);
  return () => setForceRender((prevState) => !prevState);
}

export default useForceRender;
