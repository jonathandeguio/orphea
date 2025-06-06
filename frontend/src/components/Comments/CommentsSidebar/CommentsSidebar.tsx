import React from "react";
import { CommentsContent } from "../Content";
import { useCommentController } from "../Hooks/useCommentController";
import styles from "./CommentsSidebar.module.scss";

interface IProps {
  id: string;
}
const CommentsSidebar = ({ id }: IProps) => {
  const { allOpenComments, allResolvedComments, userMap } =
    useCommentController(id);
  return (
    <CommentsContent
      classes={{ root: styles.root }}
      id={id}
      allOpenComments={allOpenComments}
      allResolvedComments={allResolvedComments}
      userMap={userMap}
    />
  );
};

export default CommentsSidebar;
