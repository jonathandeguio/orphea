import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { ThunkAppDispatch } from "redux/types/store";
import { getSocketClient } from "utils/utilities";
import { getAllUserDetails } from "../../../redux/actions/userActions";
import { fetchCommentsAPI, fetchUsersDetailsAPI } from "../Comments.api";

export const useCommentController = (id: string) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [allOpenComments, setAllOpenComments] = useState<Comment[]>([]);
  const [allResolvedComments, setAllResolvedComments] = useState<Comment[]>([]);

  const [userMap, setUserMap] = useState(undefined);

  const getAllData = () => {
    fetchCommentsAPI(id, "open").then(({ data: data1 }) => {
      setAllOpenComments(data1);
      fetchCommentsAPI(id, "resolved").then(({ data: data2 }) => {
        setAllResolvedComments(data2);
        const combinedData = [...data1, ...data2];
        getUsersDetails(combinedData);
      });
      dispatch(getAllUserDetails());
    });
  };

  const getUsersDetails = (data: Comment[]) => {
    const user_list: any[] = [];
    for (let i = 0; i < data.length; i++) {
      user_list.push(data[i].createdBy);
      for (let j = 0; j < data[i].replies.length; j++)
        user_list.push(data[i].replies[j].createdBy);
    }

    const uniqueUSERIDList: string[] = user_list.filter(
      (item, index) => user_list.indexOf(item) === index
    );
    fetchUsersDetailsAPI(uniqueUSERIDList).then(({ data: usersDetails }) =>
      setUserMap(usersDetails)
    );
  };

  const totalOpenComments = allOpenComments?.length;

  useEffect(() => {
    getAllData();

    const client = getSocketClient();

    client.activate();

    client.onConnect = (frame) => {
      client.subscribe(`/topic/comment/${id}`, async function (mail) {
        const msg = JSON.parse(mail.body).message;
        if (msg === "commentsUpdated") getAllData();
      });
    };

    return () => {
      client.deactivate();
    };
  }, [id]);

  return {
    allOpenComments,
    allResolvedComments,
    totalOpenComments,
    userMap,
  };
};
