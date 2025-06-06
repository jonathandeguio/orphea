import { User } from "global";
import { Group } from "pages/Settings/Groups/Group";

export const PLATFORM_ADMINISTRATOR = "platform-administrator";

export const getAllIdentityChoices = (users: User[], groups: Group[]) => {
    let identities = [];
    if (users) {
      const usersWithoutPlatformAdmin = users.filter(
        (user: User) => user.username != PLATFORM_ADMINISTRATOR
      );
      identities.push(...usersWithoutPlatformAdmin);
    }
    if (groups) identities.push(...groups);
    return identities;
};