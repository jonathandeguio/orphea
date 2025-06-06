import axios from "axios";

export const getUserDetails =
    () => async () => {
        try {
            const cookies = document.cookie.split('; ');
            let token = '';

            for (const cookie of cookies) {
                if (cookie.startsWith('bAT=')) {
                    token = cookie.split('=')[1];
                    break;
                }
            }
            const BASE_URL = location.protocol + '//' + location.host;
            const config = {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            };

            const { data } = await axios.get(
                `${BASE_URL}/api/passport/users/me`,
                config
            );

            return data;
        } catch (error) {
            console.log(error);
        }
    };


function getTheme(isDarkMode: boolean) {

    const user = getUserDetails();

    if (isDarkMode === undefined) {
        if (user === undefined) {
            const currTime = new Date().getHours();
            if (currTime >= 18 || currTime < 5) {
                // document.getElementById("root")?.classList.add("dark");
                document.documentElement.setAttribute('data-theme', 'dark');
            } else {
                // document.getElementById("root")?.classList.remove("dark");
                document.documentElement.setAttribute('data-theme', 'light');
            }
        } else {
            if (user["mode"] === "auto") {
                if (user["mode"] === "dark") {
                    // document.getElementById("root")?.classList.add("dark");
                    document.documentElement.setAttribute('data-theme', 'dark');
                } else if (user["mode"] === "light") {
                    // document.getElementById("root")?.classList.remove("dark");
                    document.documentElement.setAttribute('data-theme', 'light');
                }

                // const curentTime = new Date().getHours();
                // if (curentTime >= 18 || curentTime < 5) {
                //   document.getElementById("root")?.classList.add("dark");
                // } else {
                //   document.getElementById("root")?.classList.remove("dark");
                // }
            } else if (user["mode"] === "dark") {
                // document.getElementById("root")?.classList.add("dark");
                document.documentElement.setAttribute('data-theme', 'dark');
            } else if (user["mode"] === "light") {
                // document.getElementById("root")?.classList.remove("dark");
                document.documentElement.setAttribute('data-theme', 'light');
            }
        }
    } else {
        if (user !== undefined) {
            if (user["mode"] === "dark") {
                // document.getElementById("root")?.classList.add("dark");
                document.documentElement.setAttribute('data-theme', 'dark');
            } else if (user["mode"] === "light") {
                // document.getElementById("root")?.classList.remove("dark");
                document.documentElement.setAttribute('data-theme', 'light');
            }
        } else {
            if (isDarkMode) {
                // document.getElementById("root")?.classList.add("dark");
                document.documentElement.setAttribute('data-theme', 'dark');
            } else {
                // document.getElementById("root")?.classList.remove("dark");
                document.documentElement.setAttribute('data-theme', 'light');
            }
        }
    }
}

export {
    getTheme
}