import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { BASE_URL } from "./constants";
import { useSelector, useDispatch } from "react-redux";
import { RootState, ThunkAppDispatch } from "../redux/types/store";
import { setTokenInvalid, setTokenValid } from "../redux/actions/tokenActions";
import axios from "axios";
import Loading from "../utils/Loading";

import LoginIcon from '@mui/icons-material/Login';
import { Button } from "@mui/material";


const LoginError = () => {
	const dispatch = useDispatch<ThunkAppDispatch>();
	const navigate = useNavigate();
	const { isTokenValid, loading: tokenStatusLoading } = useSelector((state: RootState) => state.tokenStatus);

	async function Ping() {
		const BASE_URL = process.env.REACT_APP_BASE_URL_API;
		const movetodataToken = localStorage.getItem("movetodataToken");

		const config = {
			headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${movetodataToken}`,
			},
		};

		try {
			const { data } = await axios.get(`${BASE_URL}/ping`, config);

			if (data.message === "pong") {
				if (tokenStatusLoading || !isTokenValid) {
					dispatch(setTokenValid());
				}
			}
		} catch (error) {
			if (
				(error as { [id: string]: any }).response?.data.error === "Unauthorized"
			) {
				if (tokenStatusLoading || isTokenValid) {
					dispatch(setTokenInvalid());
				}
			}
		}
	}

	useEffect(() => {
		const login_refresh = setInterval(function () {
			if (localStorage.getItem("login-refresh") === "0") {
				navigate(-1);
				Ping();
				localStorage.removeItem("login-refresh");
			}
		}, 1000);
		if (!tokenStatusLoading && isTokenValid)
			navigate(-1);

		return () => {
			clearInterval(login_refresh);
		}
	}, [Ping, isTokenValid, tokenStatusLoading])

	return (
		tokenStatusLoading ? <Loading /> :
			<div className="relogin">
				<div className="login-container">
					<div className="login-icon">
						<svg
							className="login-icon-outer"
							width="25vw"
							height="25vh"
							viewBox="0 0 300 300"
							fill="none"
							xmlns="http://www.w3.org/2000/svg"
						>
							<path
								className="login-icon-outer-inner"
								d="M25.0962 77.8867L150 5.7735L274.904 77.8867V222.113L150 294.226L25.0962 222.113L25.0962 77.8867Z"
								stroke="#5DACBD"
								strokeWidth="10"
							/>
							<path
								d="M117.648 69.4577L150.723 85.8257M147.757 85.566L180.527 69.198M150.163 85.74V118.74M148.863 53.4L181.863 69.9V102.9L148.863 119.4L115.863 102.9V69.9L148.863 53.4Z"
								stroke="#5DACBD"
								strokeWidth="8"
							/>
							<path
								d="M64.2473 161.858L97.3225 178.226M94.3566 177.966L127.127 161.598M96.7629 178.14V211.14M95.4629 145.8L128.463 162.3V195.3L95.4629 211.8L62.4629 195.3V162.3L95.4629 145.8Z"
								stroke="#5DACBD"
								strokeWidth="8"
							/>
							<path
								d="M173.447 161.858L206.523 178.226M203.557 177.966L236.327 161.598M205.963 178.14V211.14M204.663 145.8L237.663 162.3V195.3L204.663 211.8L171.663 195.3V162.3L204.663 145.8Z"
								stroke="#5DACBD"
								strokeWidth="8"
							/>
							<path
								fillRule="evenodd"
								clipRule="evenodd"
								d="M115.864 85.7088L77.2539 108V157.638L77.4004 157.5L84.3004 153.9L85.2539 154.109V112.619L115.864 94.9464V85.7088ZM150 234L105.633 208.385L106.2 207.9L113.535 203.71L150 224.763L182.782 205.836L192 206.1L193.927 208.639L150 234ZM222.746 157.218V108L181.864 84.3973V93.6349L214.746 112.619V155.007L220.2 154.5L222.746 157.218Z"
								fill="#5DACBD"
							/>
						</svg>

						{/* <div className="login-icon-movetodata">MOVETODATA</div> */}
						<h3 style={{ "color": "#C5CBD3" }}>Your session has expired.</h3>
					</div>


					<Button
                variant="contained"
                onClick={() => {
					localStorage.setItem('tokenRefresh', "0");
					window.open(BASE_URL + '/login?tokenRefresh=1',
						'mypopup', 'directories=no,titlebar=no,toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width=700,height=550');
					return false;
				}}
                style={{ "background": "#1d4262" }}
              >

                Login
                <LoginIcon />
              </Button>

				</div>


			</div>
	);
};

export default LoginError;