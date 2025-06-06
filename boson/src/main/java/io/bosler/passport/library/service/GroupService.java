package io.bosler.passport.library.service;

import io.bosler.passport.DTO.GroupDTO;
import io.bosler.passport.library.models.GroupWithUserDTO;
import io.bosler.passport.library.models.Groups;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    Groups saveGroup(Groups groups);

    void addUsersToGroupMembers(UUID adminId, List<UUID> userIds, UUID groupId);
    void addUsersToGroupManagers(UUID adminId, List<UUID> userIds, UUID groupId);
    void addUsersToGroupOwners(UUID adminId, List<UUID> userIds, UUID groupId);
    void removeUsersToGroupMembers(UUID adminId, List<UUID> userIds, UUID groupId);
    void removeUsersToGroupManagers(UUID adminId, List<UUID> userIds, UUID groupId);
    void removeUsersToGroupOwners(UUID adminId, List<UUID> userIds, UUID groupId);


    List<Groups> getGroups();

    GroupDTO convertGroupIntoDTO(Groups groups);

    GroupWithUserDTO convertGroupIntoGroupWithUserDTO(Groups groups);

    boolean isGroupOneOfTheAdministrator(String groupName);

    public List<GroupDTO> getGroupsByUserMembership(UUID userId);

}
