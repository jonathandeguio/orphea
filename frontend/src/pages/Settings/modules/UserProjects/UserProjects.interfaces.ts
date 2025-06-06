import { Interface } from "readline";
import UserProjects from "./UserProjects";

export namespace IUserProjects {
  export interface IUserProjectsDTO {
    projectList: IProjectWithUserRole[];
    projectOrPlatformAdmin: boolean;
  }
  export interface IProjectWithUserRole {
    id: string;
    name: string;
    description: string;
    userRole: any;
  }
}
