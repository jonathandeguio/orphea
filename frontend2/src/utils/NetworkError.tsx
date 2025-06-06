// import { ReloadOutlined } from "@ant-design/icons";
import ReplayIcon from '@mui/icons-material/Replay';
import React from "react";

const NetworkError = () => {
	return (
		<div className="notfound">
			<section className="page_404">
				<div className="container">
					<div className="row">
						<div className="col-sm-12 ">
							<div className="col-sm-10 col-sm-offset-1  text-center">
								<div className="four_zero_four_bg">
									<h1 className="text-center ">
										Oops!
									</h1>
								</div>
								<div className="contant_box_404">
									<h3 className="h2">
										Look like there is network error.
									</h3>
									Note : if the backend is down, we are working hard to fix it.

									<br/><br/><br/>

									<p>Trying to connect...</p>

									<a href="/" className="link_404">
									<ReplayIcon /> Reload 
									</a>
								</div>
							</div>
						</div>
					</div>
				</div>
			</section>
		</div>
	);
};

export default NetworkError;
