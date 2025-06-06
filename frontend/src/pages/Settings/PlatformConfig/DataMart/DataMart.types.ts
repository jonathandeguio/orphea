interface IDataMartModel {
  id: string;
  name: string;
  description: string;
  isEnabled: boolean;
  projects: string[];

  server: string;
  port: string;
  username: string;
  password: string;
  limitRows: BigInteger;
  limitSize: BigInteger;

  updatedBy: string;
  createdBy: string;
  updatedAt: string;
  createdAt: string;
}

interface IDMConfig {
  config: string;
  dataMartModels: IDataMartModel[];
}
