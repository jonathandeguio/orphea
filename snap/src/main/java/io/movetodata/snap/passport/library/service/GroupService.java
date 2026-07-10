package io.movetodata.snap.passport.library.service;


import io.movetodata.snap.passport.DTO.GroupDTO;
import io.movetodata.snap.passport.library.models.GroupWithUserDTO;
import io.movetodata.snap.passport.library.models.Groups;

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
}
