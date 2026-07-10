package io.movetodata.passport.library.service;

import io.movetodata.passport.library.models.Permissions;
import io.movetodata.passport.library.models.PermissionsMapping;
import io.movetodata.passport.library.models.Role;
import io.movetodata.passport.library.models.Users;

import java.util.List;

public interface UserService {

    Role saveRole(Role role);

    Permissions savePermissions(Permissions permissions);

    PermissionsMapping savePermissionsMapping(PermissionsMapping permissionsMapping);

    Users getUser(String username);
    List<Users> getUsers();
    Users saveUser(Users user);

    List<Role> getRole();

    List<Permissions> getPermissions();

    List<PermissionsMapping> getPermissionsMapping();

//    Groups saveGroup(Groups groups);
//    void addUserToGroupOwners(String username, String groupName);
//    void addUserToGroupAdministrators(String username, String groupName);
//    void addUserToGroupMembers(String username, String groupName);
//    List<Groups> getGroups();

//    List<TokenLongLived> getMyLongLivedTokens(UUID userId);
//    TokenLongLived saveLongLivedToken(TokenLongLived tokenLongLived);
}
