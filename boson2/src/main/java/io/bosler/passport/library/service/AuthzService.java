package io.bosler.passport.library.service;

import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.passport.library.models.Groups;
import io.bosler.passport.library.models.PermissionsMapping;
import io.bosler.passport.library.models.Role;
import io.bosler.passport.library.models.User;
import io.bosler.passport.library.repository.GroupsRepository;
import io.bosler.passport.library.repository.PermissionMappingRepository;
import io.bosler.passport.library.repository.RoleRepository;
import io.bosler.passport.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class AuthzService {
    private final PermissionMappingRepository permissionMappingRepository;
    private final RoleRepository roleRepository;
    private final GroupsRepository groupsRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;

    public boolean isViewer(UUID userID, UUID resourceId) {

        // Check for system-wide permissions first
        if (Objects.equals(resourceId, new UUID(0, 0))) {
            return checkSystemPermissions(userID, new UUID(0, 0), "project-administrators");
        } else if (Objects.equals(resourceId, new UUID(1, 1))) {
            return checkSystemPermissions(userID, new UUID(1, 1), "group-administrators");
        } else if (Objects.equals(resourceId, new UUID(2, 2))) {
            return checkSystemPermissions(userID, new UUID(2, 2), "user-administrators");
        }

        List<FolderModel> paths = getPathIds(resourceId, new ArrayList<>());

        for (FolderModel folderModel1 : paths) {
//            System.out.println("checking owners");
            if (checkPermissions(userID, folderModel1, "Owner")) {
                return true;
            }
//            System.out.println("checking editors");
            if (checkPermissions(userID, folderModel1, "Editor")) {
                return true;
            }
//            System.out.println("checking viewers");
            if (checkPermissions(userID, folderModel1, "Viewer")) {
                return true;
            }
        }

        return false;
    }

    public boolean isEditor(UUID userID, UUID resourceId) {

        // Check for system-wide permissions first

        if (Objects.equals(resourceId, new UUID(0, 0))) {
            return checkSystemPermissions(resourceId, new UUID(0, 0), "project-administrators");
        } else if (Objects.equals(resourceId, new UUID(1, 1))) {
            return checkSystemPermissions(resourceId, new UUID(1, 1), "group-administrators");
        } else if (Objects.equals(resourceId, new UUID(2, 2))) {
            return checkSystemPermissions(resourceId, new UUID(2, 2), "user-administrators");
        }

        List<FolderModel> paths = getPathIds(resourceId, new ArrayList<>());

        for (FolderModel folderModel1 : paths) {
            if (checkPermissions(userID, folderModel1, "Owner")) {
                return true;
            }
            if (checkPermissions(userID, folderModel1, "Editor")) {
                return true;
            }
        }

        return false;
    }

    public boolean isOwner(UUID userID, UUID resourceId) {

        // Check for system-wide permissions first
        if (Objects.equals(resourceId, new UUID(0, 0))) {  // projects
            return checkSystemPermissions(userID, new UUID(0, 0), "project-administrators");
        } else if (Objects.equals(resourceId, new UUID(1, 1))) {  // groups
            return checkSystemPermissions(userID, new UUID(0, 0), "group-administrators");
        } else if (Objects.equals(resourceId, new UUID(2, 2))) { // users
            return checkSystemPermissions(userID, new UUID(2, 2), "user-administrators");
        }

        List<FolderModel> paths = getPathIds(resourceId, new ArrayList<>());

        for (FolderModel folderModel1 : paths) {
            if (checkPermissions(userID, folderModel1, "Owner")) {
                return true;
            }
        }

        return false;
    }


    private List<FolderModel> getPathIds(UUID id, List<FolderModel> path) {

        FolderModel pathId = folderRepository.getById(id);
        path.add(pathId);

        if (!Objects.equals(pathId.getParent(), new UUID(0, 0))) {
            getPathIds(pathId.getParent(), path);
        }
        return path;
    }

    private boolean checkPermissions(UUID Identity, FolderModel folderModel, String permission) {

        if (!userRepository.existsById(Identity)) {
            return false;
        }

        Role role = roleRepository.findByName(permission);
        if (role != null) {
//            System.out.println("Role found " + Identity + " " + folderModel.getId() + " " + role.getId());
            PermissionsMapping permissionsMappings = permissionMappingRepository.findByIdentityIdAndResourceIdAndRoleId(Identity, folderModel.getId(), role.getId());
            if (permissionsMappings != null) {
//                System.out.println("give Perm found " + permission);
                return role.getDelete() || role.getWrite() || role.getRead();
            }

            User user = userRepository.getById(Identity);
//            System.out.println("User found " + user.getId());
            List<Groups> groups = groupsRepository.findByMembers(user);

            for (Groups groups1 : groups) {
//                System.out.println(groups1.getId() + " " + folderModel.getId() + " " + role.getId());
                if (permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(groups1.getId(), folderModel.getId(), role.getId())) {
//                    System.out.println(groups1.name);
                    return role.getDelete() || role.getWrite() || role.getRead();
                }
            }
        }

        return false;
    }


    public boolean checkSystemPermissions(UUID Identity, UUID resourceId, String groupName) {

//        if (!userRepository.existsById(Identity)) {
//            return false;
//        }

        Role role = roleRepository.findByName("Owner");
        if (role != null) {
//            System.out.println("Role found " + Identity + " project/owner " + role.getId());
            if (permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(Identity, resourceId, role.getId())) {

                System.out.println("give Perm found " + "Owner");
                return role.getDelete() || role.getWrite() || role.getRead();
            }

            User user = userRepository.getById(Identity);
//            System.out.println("User found " + user.getId());
            List<Groups> groups = groupsRepository.findByMembers(user);

//            System.out.println("all groups " + groups);

            for (Groups groups1 : groups) {

                // TODO : need to work on nested groups.

                if(Objects.equals(groups1.name, "platform-administrators")) {
                    return role.getDelete() || role.getWrite() || role.getRead();
                }
//                if(Objects.equals(groups1.name, "ignite-administrators")) {
//                    return true;
//                }

                if (Objects.equals(groups1.getName(), groupName)) {
                    if (permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(groups1.getId(), resourceId, role.getId())) {
//                        System.out.println("give Perm found Group " + "Owner");
//                        System.out.println(groups1.name);
//                        System.out.println(groups1.getId());
                        return role.getDelete() || role.getWrite() || role.getRead();
                    }
                }
            }
        }
        return false;
    }

}
