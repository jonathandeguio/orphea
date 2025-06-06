type TTag = {
    color: string;
    name: string;
    description: string;
    createdAt: string;
    createdBy: string;
    updatedAt: string | null;
    updatedBy: string | null;
  };
  
  type TCategoryDetails = {
    createdAt: string;
    createdBy: string;
    name: string;
    description: string;
    enabled: boolean;
    id: string;
    updatedAt: string | null;
    updatedBy: string | null;
    tags: TTag[];
  };
