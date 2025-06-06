import axios from "axios";

/**
 * fetches Mutliple user details
 * @param userIdList User Id list
 */
export const fetchUsersDetailsAPI = (userIdList: string[]) => {
  return axios.post(`/passport/users/byIds`, userIdList);
};

/**
 * fetch Comments with particular id and type(open,resolved)
 * @param id resource id
 * @param type type of comment(open,resolved)
 */
export const fetchCommentsAPI = (id: string, type: string) => {
  return axios.get(`/kitab/comments/${id}/${type}`);
};

/**
 * Add Comment(type:(comment or reply both))
 * @param body Comment Details
 */
export const addCommentAPI = (body: any) => {
  return axios.post(`/kitab/comments/newComment`, body);
};

/**
 * Open And Resolve Comment
 * @param commentId Comment ID
 * @param body Comment Details
 */
export const openAndResolveCommentAPI = (commentId: string, body: any) => {
  return axios.put(`/kitab/comments/editComment/${commentId}`, body);
};

/**
 * Edit Comment(comment Or reply both)
 * @param commentId Comment ID
 * @param body Comment Details
 */
export const editCommentAPI = (commentId: string, body: any) => {
  return axios.put(`/kitab/comments/editComment/${commentId}`, body);
};

/**
 * Delete Comment(comment Or reply both)
 * @param commentId Comment ID
 */
export const deleteCommentAPI = (commentId: string) => {
  return axios.delete(`/kitab/comments/${commentId}`);
};
