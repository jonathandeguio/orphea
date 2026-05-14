import React from "react";
import { CommentsContent } from "./Content";
import { useCommentController } from "./Hooks/useCommentController";

interface IProps {
  id: string;
}
const CommentsV2 = ({ id }: IProps) => {
  const { allOpenComments, allResolvedComments, userMap } =
    useCommentController(id);
  return (
    <CommentsContent
      id={id}
      allOpenComments={allOpenComments}
      allResolvedComments={allResolvedComments}
      userMap={userMap}
    />
  );
};

export default CommentsV2;
