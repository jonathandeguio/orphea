package io.movetodata.snap.passport.library.service;

import io.movetodata.snap.passport.library.models.Groups;
import io.movetodata.snap.passport.library.models.User;
import io.movetodata.snap.passport.library.repository.GroupsRepository;
import io.movetodata.snap.passport.library.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import java.util.*;

import static io.movetodata.snap.passport.enums.PlatformUsers.PlatformInternal;

@RequiredArgsConstructor
@Component
public class AuthzService {
    private final GroupsRepository groupsRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public boolean isViewer(UUID userID, UUID resourceId) {
        return permissionChecker("Viewer", userID, resourceId);
    }

    public boolean isEditor(UUID userID, UUID resourceId) {
        return permissionChecker("Editor", userID, resourceId);
    }

    public boolean isOwner(UUID userID, UUID resourceId) {
        return permissionChecker("Owner", userID, resourceId);
    }

    public boolean isPlatformAdmin(UUID id) {
        User user = userRepository.getReferenceById(id);
        if (Objects.equals(user.getUsername(), "platform-administrator")) {
            return true;
        }

        Groups adminGroup = groupsRepository.findByName("platform-administrators");
        return adminGroup.getMembers().contains(user);
    }

    public boolean isGroupAdmin(UUID id) {
        if (isPlatformAdmin(id)) return true;

        Groups adminGroup = groupsRepository.findByName("group-administrators");

        for (User user : adminGroup.getMembers()) {
            if (Objects.equals(user.getId(), id)) return true;
        }
        return false;
    }

    public boolean isUserAdmin(UUID id) {
        if (isPlatformAdmin(id)) return true;

        Groups adminGroup = groupsRepository.findByName("user-administrators");

        for (User user : adminGroup.getMembers()) {
            if (Objects.equals(user.getId(), id)) return true;
        }
        return false;
    }

    public boolean isConnectAdmin(UUID id) {
        if (isPlatformAdmin(id)) return true;

        Groups adminGroup = groupsRepository.findByName("connect-administrators");

        for (User user : adminGroup.getMembers()) {
            if (Objects.equals(user.getId(), id)) return true;
        }
        return false;
    }

    public boolean isProjectAdmin(UUID id) {
        if (isPlatformAdmin(id)) return true;

        Groups adminGroup = groupsRepository.findByName("project-administrators");

        for (User user : adminGroup.getMembers()) {
            if (Objects.equals(user.getId(), id)) return true;
        }
        return false;
    }

    private boolean permissionChecker(String role, UUID userId, UUID resourceId) {
        User platformInternal = userRepository.findByUsername(PlatformInternal.toString()).orElseThrow();
        if (userId == platformInternal.getId()) { // This is for builds by schedules
            return true;
        }

        if (!userRepository.existsById(userId)) {
            return false;
        }

        if (isPlatformAdmin(userId)) {
            return true;
        }

        List<PermissionDTO> ancestorPermissions = getAncestorPermissions(resourceId, userId);

        List<String> ownerList = new ArrayList<>();
        ownerList.add("Owner");
        List<String> editorList = new ArrayList<>(ownerList);
        editorList.add("Editor");
        List<String> viewerList = new ArrayList<>(editorList);
        viewerList.add("Viewer");

        Map<String, List<String>> roles = new HashMap<>();
        roles.put("Owner", ownerList);
        roles.put("Editor", editorList);
        roles.put("Viewer", viewerList);

        for (PermissionDTO permission : ancestorPermissions) {
            if (roles.get(role).contains(permission.getRole())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Fetches the ancestors for which permission is defined of current user profile/group.
     * Does not return all the parents!
     * Returns the minimalist data.
     * Avoid usage of this function
     *
     * @param role       Owner | Editor | Viewer
     * @param userId     UUID
     * @param resourceId UUID
     * @return Set<UUID>
     */
//    public Set<UUID> ancestorsByRole(String role, UUID userId, UUID resourceId) {
//        Set<UUID> permissionSet = new HashSet<>();
//
//        if (isProjectAdmin(userId)) {
//            ResourceModel resource = resourceRepository.findById(resourceId).orElseThrow();
//            permissionSet.add(resource.getParent());
//            return permissionSet;
//        }
//
//        if (!userRepository.existsById(userId)) {
//            return permissionSet;
//        }
//
//        List<PermissionDTO> ancestorPermissions = getAncestorPermissions(resourceId, userId);
//
//        List<String> ownerList = new ArrayList<>();
//        ownerList.add("Owner");
//        List<String> editorList = new ArrayList<>(ownerList);
//        editorList.add("Editor");
//        List<String> viewerList = new ArrayList<>(editorList);
//        viewerList.add("Viewer");
//
//        Map<String, List<String>> roles = new HashMap<>();
//        roles.put("Owner", ownerList);
//        roles.put("Editor", editorList);
//        roles.put("Viewer", viewerList);
//
//        int i = ancestorPermissions.size() - 1;
//
//        if (!isPlatformAdmin(userId)) {
//            for (; i >= 0; i--) {
//                if (roles.get(role).contains(ancestorPermissions.get(i).getRole())) {
//                    break;
//                }
//            }
//        }
//
//        for (; i >= 0; i--) {
//            permissionSet.add(ancestorPermissions.get(i).getResource_id());
//        }
//        return permissionSet;
//    }

    /**
     * Gen Ancestor permissions is a combination of five tables
     * kitab_resource: to get the resource parent hierarchy
     * passport_groups: to get all the groups of a user
     * passport_user: to get the user
     * passport_permission_mapping: to get the permissions of any group/user
     * passport_role: to get the string role of the permission
     * ---
     * Any rows in the response of this query means user got at least viewer access
     * to check special access iterate over the items
     * --
     * The result is in increasing hierarchy that means Child first then parent, then its parent.
     *
     * @param resourceId the resource you want the hierarchy for
     * @param userId     user that is trying to access the data
     * @return List<PermissionDTO>
     */
    public List<PermissionDTO> getAncestorPermissions(UUID resourceId, UUID userId) {
        String queryString = "WITH " +
                "RECURSIVE ancestors AS ( " +
                "SELECT kf.id, kf.parent_id " +
                "FROM kitab_resource kf " +
                "WHERE id = '" + resourceId.toString() + "' " +
                "UNION " +
                "SELECT e.id, e.parent_id " +
                "FROM kitab_resource e " +
                "JOIN ancestors eh ON e.id = eh.parent_id)," +
                "users_groups AS (" +
                "SELECT pg.id, pg.name " +
                "FROM passport_groups_members pgm " +
                "JOIN passport_groups pg ON pg.id=pgm.groups_id " +
                "WHERE pgm.members_id='" + userId.toString() + "' " +
                "UNION " +
                "SELECT pu.id, pu.name " +
                "FROM passport_users pu " +
                "WHERE pu.id='" + userId + "')" +
                "SELECT ug.name, ppm.id, pr.name as role, ppm.resource_id, ppm.identity_id " +
                "FROM ancestors h " +
                "JOIN passport_permissions_mapping ppm ON h.id = ppm.resource_id " +
                "JOIN passport_role pr ON ppm.rg_fk = pr.id " +
                "JOIN users_groups ug ON ppm.identity_id=ug.id;";

        try {
            return entityManager.createNativeQuery(queryString, PermissionDTO.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch ancestor permissions");
        }
    }

    @Getter
    @Entity
    @Setter
    public static class PermissionDTO {
        @Id
        private UUID id;
        private UUID resource_id;
        private UUID identity_id;
        private String name;
        private String role;

        @Override
        public String toString() {
            return "PermissionDTO{" +
                    "resource_id=" + resource_id +
                    ", name='" + name + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }
    }
}
