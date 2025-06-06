import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Outlet, useParams } from "react-router";
import { headerLinks } from "../../redux/actions/projectActions";
import { ThunkAppDispatch } from "../../redux/types/store";
import Forbidden from "../Errors/Forbidden";

export default () => {
  const { id, branch } = useParams();
  const dispatch = useDispatch<ThunkAppDispatch>();
  const data = useSelector((state) => (state as $TSFixMe).headerLink);

  // TODO: REMOVE THIS API CALL
  useEffect(() => {
    if (id) dispatch(headerLinks(id));
  }, [id]);
  return (
    <>
      {/* <HeadProject /> */}
      {id ? (
        data.loading ? (
          <Outlet />
        ) : data.error &&
          data.errorContent === "Request failed with status code 403" ? (
          <Forbidden id={id ? id : "hidden-id"} />
        ) : (
          <Outlet />
        )
      ) : (
        <Outlet />
      )}
    </>
  );
};
