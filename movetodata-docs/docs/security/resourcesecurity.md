# Resource Security


## What is Resource Security

Resource security on a data platform refers to the measures and practices put in place to protect data resources (such as datasets, databases, files, and processing capabilities) from unauthorized access, misuse, corruption, or theft. Given the critical nature of data in modern enterprises, ensuring the security of resources on a data platform is paramount for maintaining data integrity, confidentiality, and availability.

## Access Ability on Platform

In our platform, a variety of resources are available, including Projects, folders, files, charts, dashboards, repositories, and more. To manage access and control over these resources, we have established three distinct levels of permissions: **Owner**, **Editor**, and **Viewer**.
1. **Owner**: An individual with Owner permissions possesses the highest level of control over a resource. This includes the ability to assign permissions to other users, modify the resource, delete it, and view its contents. Owners are empowered to manage all aspects of the resource, ensuring they can delegate responsibilities and control access as needed.
2. **Editor**: Users with Editor permissions have substantial control over the resource, allowing them to modify its contents and make necessary updates. However, they do not have the authority to assign permissions to others or to delete the resource. Editors are typically responsible for maintaining and updating the resource, while ownership responsibilities remain with the Owner.
3. **Viewer**: The Viewer permission level is the most restricted, allowing users to only view the resource without making any changes or modifications. This role is ideal for those who need access to the resource’s information but do not require the ability to alter it.
Additionally, our platform’s permission system is hierarchical, meaning that permissions are inherited from higher-level resources down to lower-level resources within the organizational structure. For example, if a folder is assigned certain permissions, all files and subfolders within that folder will inherit the same permissions. This top-down inheritance ensures consistent access control throughout the resource tree and simplifies the management of permissions across multiple resources.


| Access Type | Owner | Editor | Viewer |
|-------------|:-----:|:------:|:------:|
| <div align="center"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-eye" viewBox="0 0 16 16"> <path d="M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8M1.173 8a13 13 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5s3.879 1.168 5.168 2.457A13 13 0 0 1 14.828 8q-.086.13-.195.288c-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5s-3.879-1.168-5.168-2.457A13 13 0 0 1 1.172 8z"/> <path d="M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5M4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0"/> </svg></div> |   ✔️   |   ✔️    |   ✔️    |
| <div align="center"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pen" viewBox="0 0 16 16"> <path d="m13.498.795.149-.149a1.207 1.207 0 1 1 1.707 1.708l-.149.148a1.5 1.5 0 0 1-.059 2.059L4.854 14.854a.5.5 0 0 1-.233.131l-4 1a.5.5 0 0 1-.606-.606l1-4a.5.5 0 0 1 .131-.232l9.642-9.642a.5.5 0 0 0-.642.056L6.854 4.854a.5.5 0 1 1-.708-.708L9.44.854A1.5 1.5 0 0 1 11.5.796a1.5 1.5 0 0 1 1.998-.001m-.644.766a.5.5 0 0 0-.707 0L1.95 11.756l-.764 3.057 3.057-.764L14.44 3.854a.5.5 0 0 0 0-.708z"/></svg></div> |   ✔️   |   ✔️    |   ❌    |
| <div align="center"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16"><path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z"/><path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z"/></svg></div> |   ✔️   |   ❌    |   ❌    |
| <div align="center"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-folder-plus" viewBox="0 0 16 16"><path d="m.5 3 .04.87a2 2 0 0 0-.342 1.311l.637 7A2 2 0 0 0 2.826 14H9v-1H2.826a1 1 0 0 1-.995-.91l-.637-7A1 1 0 0 1 2.19 4h11.62a1 1 0 0 1 .996 1.09L14.54 8h1.005l.256-2.819A2 2 0 0 0 13.81 3H9.828a2 2 0 0 1-1.414-.586l-.828-.828A2 2 0 0 0 6.172 1H2.5a2 2 0 0 0-2 2m5.672-1a1 1 0 0 1 .707.293L7.586 3H2.19q-.362.002-.683.12L1.5 2.98a1 1 0 0 1 1-.98z"/><path d="M13.5 9a.5.5 0 0 1 .5.5V11h1.5a.5.5 0 1 1 0 1H14v1.5a.5.5 0 1 1-1 0V12h-1.5a.5.5 0 0 1 0-1H13V9.5a.5.5 0 0 1 .5-.5"/></svg></div> |   ✔️   |   ❌    |   ❌    |

## Adding Permission to Resource
- Permission can directly be added to Groups or Individual Users
- Permission can be requested by users for specific resource and access can be granted from Admin/Owner.
- Permission can be provided by Admin by adding to the specific resource access Group. 

![Resource Permission](../_media/docs_ss/Security/resource-Permssion.png)
