import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { login } from "../redux/actions/userActions";
import { refreshTokenStatus } from "../redux/actions/tokenActions";
import { ThunkAppDispatch } from "../redux/types/store";

import { GOOGLE_AUTH_URL, GITHUB_AUTH_URL } from './constants';
import Loading from "../utils/Loading";

import GitHubIcon from '@mui/icons-material/GitHub';
import FacebookIcon from '@mui/icons-material/Facebook';
import GoogleIcon from '@mui/icons-material/Google';
import LoginIcon from '@mui/icons-material/Login';
import { Alert, Box, Button, Snackbar, Typography } from "@mui/material";
import IconButton from '@mui/material/IconButton';
import OutlinedInput from '@mui/material/OutlinedInput';
import InputLabel from '@mui/material/InputLabel';
import InputAdornment from '@mui/material/InputAdornment';
import FormControl from '@mui/material/FormControl';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { AccountCircle, Key } from "@mui/icons-material";
import React from "react";


const Login = () => {

  const dispatch = useDispatch<ThunkAppDispatch>();
  const navigate = useNavigate();

  const [showPassword, setShowPassword] = React.useState(false);

  const handleClickShowPassword = () => setShowPassword((show) => !show);

  const handleMouseDownPassword = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
  };

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const { userInfo, error, loading } = useSelector((state) => (state as any).userLogin);
  const { isTokenValid, loading: tokenStatusLoading } = useSelector((state) => (state as any).tokenStatus);

  const submitHandler = (e: any) => {
    if (username !== "" && password !== "") {
      dispatch(login(username, password)).then((data: any) => {
        dispatch(refreshTokenStatus());
        // getTheme(data, isDarkMode);
      });
    }
  };

  useEffect(() => {
    if (!tokenStatusLoading && isTokenValid) {
      navigate('/');
    }
    if (!loading && error) {
      let toast_msg = "";
      if (error.response !== undefined) {
        toast_msg = "Wrong username or password";
      } else {
        toast_msg = error.message + " from our side";
      }
      console.log(toast_msg);

      console.log("toast_msg");

      setOpen(true);
    }
  }, [userInfo, loading, error, isTokenValid, tokenStatusLoading, navigate]);

  const [open, setOpen] = React.useState(false);

  const handleClose = (event?: React.SyntheticEvent | Event, reason?: string) => {
    if (reason === 'clickaway') {
      return;
    }

    setOpen(false);
  };

  return (
    tokenStatusLoading ? <Loading /> :
      <div className="login-container">
        <div className="login-icon">
          <svg
            className="login-icon-outer"
            width="30vw"
            height="30vh"
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

          <div className="login-icon-movetodata">MOVETODATA</div>
        </div>

        <div className="form-container">

          <Box>

            <Typography align="center">
              <h2>Login to MoveToData</h2>
            </Typography>


            <div style={{ display: "flex", alignItems: "center", justifyContent: "center", margin: "1rem", "gap": '0.5rem' }}>
              <a style={{ margin: "0 5px" }} href={GITHUB_AUTH_URL}>
                <GitHubIcon style={{ fontSize: '32px', color: '#cad8e5' }} />
              </a>
              <a style={{ margin: "0 5px" }}
                href={GOOGLE_AUTH_URL}
              >
                <GoogleIcon style={{ fontSize: '32px', color: '#cad8e5' }} />
              </a>
              <a style={{ margin: "0 5px" }} href="#.">
                <FacebookIcon style={{ fontSize: '32px', color: '#cad8e5' }} />
              </a>

            </div>

            <Typography align="center">
              <div style={{ display: "flex", color: "#5dacbd", alignItems: "center", justifyContent: "center", margin: "1rem" }}>
                <hr color="#5dacbd" style={{ width: "10vw" }} />
                Or
                <hr color="#5dacbd" style={{ width: "10vw" }} />
              </div>
            </Typography>

            <Box sx={{ '& > :not(style)': { m: 1 } }}>
              <Box sx={{ display: 'flex', alignItems: 'flex-end' }}>

                <FormControl fullWidth sx={{ m: 1 }} variant="outlined">
                  <InputLabel htmlFor="outlined-adornment-password">Username</InputLabel>
                  <OutlinedInput
                    id="outlined-adornment-username"
                    type='text'
                    onChange={(e) => setUsername(e.target.value)}
                    label="Username"
                    style={{ "color": "#F6F7F9" }}
                    placeholder="Enter Username here..."
                    startAdornment={
                      <InputAdornment position="start">
                        <IconButton
                          aria-label="Enter password"
                          onClick={handleClickShowPassword}
                          onMouseDown={handleMouseDownPassword}
                          edge="start"
                          style={{ "color": "#ABB3BF" }}
                        >
                          <AccountCircle />
                        </IconButton>
                      </InputAdornment>
                    }
                  />
                </FormControl>


              </Box>
            </Box>
            <Box sx={{ '& > :not(style)': { m: 1 } }}>
              <Box sx={{ display: 'flex', alignItems: 'flex-end' }}>

                <FormControl fullWidth sx={{ m: 1 }} variant="outlined">
                  <InputLabel htmlFor="outlined-adornment-password">Password</InputLabel>
                  <OutlinedInput
                    id="outlined-adornment-password"
                    type={showPassword ? 'text' : 'password'}
                    style={{ "color": "#F6F7F9" }}
                    placeholder="Enter Password here..."
                    startAdornment={
                      <InputAdornment position="start">
                        <IconButton
                          aria-label="Enter password"
                          onClick={handleClickShowPassword}
                          onMouseDown={handleMouseDownPassword}
                          edge="start"
                          style={{ "color": "#ABB3BF" }}
                        >
                          <Key />
                        </IconButton>
                      </InputAdornment>
                    }
                    endAdornment={
                      <InputAdornment position="end">
                        <IconButton
                          aria-label="toggle password visibility"
                          onClick={handleClickShowPassword}
                          onMouseDown={handleMouseDownPassword}
                          edge="end"
                          style={{ "color": "#ABB3BF" }}
                        >
                          {showPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    }
                    onChange={(e) => setPassword(e.target.value)}
                    label="Password"

                    onKeyPress={(e) => {
                      if (e.key === 'Enter') {
                        submitHandler("");
                      }
                    }}

                  />
                </FormControl>

              </Box>
              <Button
                variant="contained"
                onClick={submitHandler}
                style={{ "background": "#24527a" }}
              >

                Login
                <LoginIcon />
              </Button>

            </Box>




          </Box>

        </div>
        <Snackbar open={open} autoHideDuration={6000} onClose={handleClose} anchorOrigin={{ vertical: 'top', horizontal: 'right' }}>
          <Alert onClose={handleClose} severity="error" sx={{ width: '100%' }}>
            Not able to login
          </Alert>
        </Snackbar>
      </div >
  );
};

export default Login;
