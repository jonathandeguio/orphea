package io.orphea.passport.library.service;

import io.orphea.passport.library.models.Groups;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GroupService {

    Groups saveGroup(Groups groups);

    void addUserToGroupOwners(String username, String groupName);

    void addUserToGroupMembers(String username, String groupName);

    void addUsersToGroupMembers(UUID adminId, List<UUID> userIds, UUID groupId);
    void addUsersToGroupManagers(UUID adminId, List<UUID> userIds, UUID groupId);
    void addUsersToGroupOwners(UUID adminId, List<UUID> userIds, UUID groupId);

    void removeUsersToGroupMembers(UUID adminId, List<UUID> userIds, UUID groupId);
    void removeUsersToGroupManagers(UUID adminId, List<UUID> userIds, UUID groupId);
    void removeUsersToGroupOwners(UUID adminId, List<UUID> userIds, UUID groupId);

    List<Groups> getGroups();
}
