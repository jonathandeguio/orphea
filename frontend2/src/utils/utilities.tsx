
function getTheme(user: { [id: string]: any }, isDarkMode: boolean) {

  if (isDarkMode === undefined) {

    if (user === undefined) {
      const currTime = new Date().getHours();
      if (currTime >= 18 || currTime < 5) {
        document.getElementById("root")?.classList.add("dark");
      } else {
        document.getElementById("root")?.classList.remove("dark");
      }
    } else {
      if (user.mode === "auto") {

        if (user.mode === "dark") {
          document.getElementById("root")?.classList.add("dark");
        } else if (user.mode === "light") {
          document.getElementById("root")?.classList.remove("dark");
        }

        // const curentTime = new Date().getHours();
        // if (curentTime >= 18 || curentTime < 5) {
        //   document.getElementById("root")?.classList.add("dark");
        // } else {
        //   document.getElementById("root")?.classList.remove("dark");
        // }
      } else if (user.mode === "dark") {
        document.getElementById("root")?.classList.add("dark");
      } else if (user.mode === "light") {
        document.getElementById("root")?.classList.remove("dark");
      }
    }
  } else {

    if (user !== undefined) {
      if (user.mode === "dark") {
        document.getElementById("root")?.classList.add("dark");
      } else if (user.mode === "light") {
        document.getElementById("root")?.classList.remove("dark");
      }
    } else {
      if (isDarkMode) {
        document.getElementById("root")?.classList.add("dark");
      } else {
        document.getElementById("root")?.classList.remove("dark");
      }
    }
  }
  }

  const closeTab = () => {
    window.opener = null;
    window.open("", "_self");
    window.close();
  };



  const bytesToHumanReableSize = (size: number) => {
    const i = Math.floor(Math.log(size) / Math.log(1024));
    return (
      (size / Math.pow(1024, i)).toFixed(0) +
      " " +
      ["B", "kB", "MB", "GB", "TB"][i]
    );
  };

  const numbersToHumanReadable = (num: number) => {
    if (num >= 1000000000) {
      return (num / 1000000000).toFixed(1).replace(/\.0$/, '') + 'b';
    }
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1).replace(/\.0$/, '') + 'm';
    }
    if (num >= 1000) {
      return (num / 1000).toFixed(1).replace(/\.0$/, '') + ' thousand';
    }
    return num;
  }

  function timeConverter(UNIX_timestamp: any) {
    const date = new Date(UNIX_timestamp);

    return (
      date.getDate() +
      "/" +
      (date.getMonth() + 1) +
      "/" +
      date.getFullYear() +
      " " +
      date.getHours() +
      ":" +
      date.getMinutes() +
      ":" +
      date.getSeconds()
    );
  }

  //   function bytesToHumanReadable(bytes: number): string {
  //     const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  //     if (bytes === 0) return '0B';
  //     const i = Math.floor(Math.log(bytes) / Math.log(1024));
  //     return `${Math.round(bytes / Math.pow(1024, i))}${sizes[i]}`;
  //   }


  function capitalizeFirstLetter(string: string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  }

  const text_truncate = function (str: string, length: number, ending: string) {
    if (length == null) {
      length = 100;
    }
    if (ending == null) {
      ending = '...';
    }
    if (str.length > length) {
      return str.substring(0, length - ending.length) + ending;
    } else {
      return str;
    }
  };


  const millisecondsToStr = (milliseconds: number) => {
    // TIP: to find current time in milliseconds, use:
    // var  current_time_milliseconds = new Date().getTime();

    function numberEnding(number: number) {
      return number > 1 ? "s" : "";
    }

    let temp = Math.floor(milliseconds / 1000);
    const years = Math.floor(temp / 31536000);
    if (years) {
      return years + " year" + numberEnding(years);
    }
    //TODO: Months! Maybe weeks?
    const days = Math.floor((temp %= 31536000) / 86400);
    if (days) {
      return days + " day" + numberEnding(days);
    }
    const hours = Math.floor((temp %= 86400) / 3600);
    if (hours) {
      return hours + " hour" + numberEnding(hours);
    }
    const minutes = Math.floor((temp %= 3600) / 60);
    if (minutes) {
      return minutes + " minute" + numberEnding(minutes);
    }
    const seconds = temp % 60;
    if (seconds) {
      return seconds + " second" + numberEnding(seconds);
    }
    return "less than a second"; //'just now' //or other string you like;
  }

  export {
    getTheme, closeTab,
    bytesToHumanReableSize,
    numbersToHumanReadable,
    timeConverter,
    capitalizeFirstLetter,
    millisecondsToStr,
    text_truncate
  };