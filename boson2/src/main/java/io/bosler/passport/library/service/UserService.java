package io.bosler.passport.library.service;

import io.bosler.passport.library.models.PermissionsMapping;
import io.bosler.passport.library.models.Role;
import io.bosler.passport.library.models.User;

import java.util.List;

public interface UserService {

    Role saveRole(Role role);

    PermissionsMapping savePermissionsMapping(PermissionsMapping permissionsMapping);

    User getUser(String username);

    List<User> getUsers();

    User saveUser(User user);

    List<Role> getRole();

    List<PermissionsMapping> getPermissionsMapping();

//    Groups saveGroup(Groups groups);
//    void addUserToGroupOwners(String username, String groupName);
//    void addUserToGroupAdministrators(String username, String groupName);
//    void addUserToGroupMembers(String username, String groupName);
//    List<Groups> getGroups();

//    List<TokenLongLived> getMyLongLivedTokens(UUID userId);
//    TokenLongLived saveLongLivedToken(TokenLongLived tokenLongLived);
}
