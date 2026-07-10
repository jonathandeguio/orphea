package io.movetodata.passport.library.service;

import io.movetodata.kitab.library.models.ProjectWithUserRoleDTO;
import io.movetodata.passport.DTO.UserDTO;
import io.movetodata.passport.library.models.PermissionsMapping;
import io.movetodata.passport.library.models.Role;
import io.movetodata.passport.library.models.User;
import io.movetodata.sharedutils.models.PageSettings;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface UserService {

    Role saveRole(Role role);

    PermissionsMapping savePermissionsMapping(PermissionsMapping permissionsMapping);

    User getUser(String username);
    User getUserById(UUID id);

    List<User> getUsers();

    User saveUser(User user);

    User changePassword(User user, String newPassword);

    List<Role> getRole();

    List<PermissionsMapping> getPermissionsMapping();

    UserDTO convertUserIntoDTO(User user) throws IOException;

    String compressProfileImage(String profileImage, float compressionQuality) throws IOException;

    Boolean isUsernameAlreadyAssigned(String username);

    List<ProjectWithUserRoleDTO> getUserProjectWithUserRole(UUID userId);
//    Groups saveGroup(Groups groups);
//    void addUserToGroupOwners(String username, String groupName);
//    void addUserToGroupAdministrators(String username, String groupName);
//    void addUserToGroupMembers(String username, String groupName);
//    List<Groups> getGroups();

//    List<TokenLongLived> getMyLongLivedTokens(UUID userId);
//    TokenLongLived saveLongLivedToken(TokenLongLived tokenLongLived);
}
