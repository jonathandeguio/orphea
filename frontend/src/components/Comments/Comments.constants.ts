interface Comment {
  id: string;
  message: string;
  resourceId: string;
  status: string;
  createdAt: number;
  createdBy: string;
  updatedAt: number;
  updatedBy: string;
  replies: Comment[];
}
