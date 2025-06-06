interface Group {
  id: string;
  name: string;
  description: string;
  status: string;
  owners: User[];
  managers: User[];
  members: User[];
  createdBy: User;
  createdAt: number;
  updatedBy: User;
  updatedAt: number;
}
