package io.orphea.passport.library.service;

import io.orphea.passport.DTO.GroupDTO;
import io.orphea.passport.library.models.GroupWithUserDTO;
import io.orphea.passport.library.models.Groups;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.repository.GroupsRepository;
import io.orphea.passport.library.repository.UserRepository;
import io.orphea.passport.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GroupServiceImpl implements GroupService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final GroupsRepository groupsRepository;

    public Groups saveGroup(Groups groups) {
        log.info("Saving new group {} to the database", groups.getName());
        return groupsRepository.save(groups);
    }

    @Override
    public void addUsersToGroupMembers(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Adding user {} to group Members {}", groupId, userIds);
        Groups group = groupsRepository.getById(groupId);
        List<User> members = group.getMembers();
        for (UUID userId : userIds) {
            User user = userRepository.getById(userId);
            if (!members.contains(user)) {
                members.add(user);
                group.setMembers(members);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }
        groupsRepository.save(group);
    }

    @Override
    public void addUsersToGroupManagers(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Adding user {} to group Managers {}", groupId, userIds);
        Groups group = groupsRepository.getById(groupId);
        List<User> managers = group.getManagers();
        for (UUID userId : userIds) {
            User user = userRepository.getById(userId);
            if (!managers.contains(user)) {
                managers.add(user);
                group.setManagers(managers);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }
        groupsRepository.save(group);
    }

    @Override
    public void addUsersToGroupOwners(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Adding user {} to group Owners {}", groupId, userIds);
        Groups group = groupsRepository.getById(groupId);
        List<User> owners = group.getOwners();
        for (UUID userId : userIds) {
            User user = userRepository.getById(userId);
            if (!owners.contains(user)) {
                owners.add(user);
                group.setOwners(owners);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }
        groupsRepository.save(group);
    }

    @Override
    public void removeUsersToGroupMembers(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Removing user {} to group Members {}", groupId, userIds);
        Groups group = groupsRepository.getById(groupId);
        List<User> members = group.getMembers();
        for (UUID userId : userIds) {
            User user = userRepository.getById(userId);
            if (members.contains(user)) {
                members.remove(user);
                group.setMembers(members);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }
        groupsRepository.save(group);
    }

    @Override
    public void removeUsersToGroupManagers(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Removing user {} to group Managers {}", groupId, userIds);
        Groups group = groupsRepository.getById(groupId);
        List<User> managers = group.getManagers();
        for (UUID userId : userIds) {
            User user = userRepository.getById(userId);
            if (managers.contains(user)) {
                managers.remove(user);
                group.setManagers(managers);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }
        groupsRepository.save(group);
    }

    @Override
    public void removeUsersToGroupOwners(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Removing user {} to group Owners {}", groupId, userIds);
        Groups group = groupsRepository.getById(groupId);
        List<User> owners = group.getOwners();
        for (UUID userId : userIds) {
            User user = userRepository.getById(userId);
            if (owners.contains(user)) {
                owners.remove(user);
                group.setOwners(owners);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }
        groupsRepository.save(group);
    }

    @Override
    public List<Groups> getGroups() {
        log.info("Fetching all groups");
        return groupsRepository.findAllByOrderByNameAsc();
    }

    @Override
    public GroupDTO convertGroupIntoDTO(Groups groups) {
        if(groups == null)
            return null;
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(groups.getId());
        groupDTO.setName(groups.getName());
        groupDTO.setDescription(groups.getDescription());
        return groupDTO;
    }

    @Override
    public GroupWithUserDTO convertGroupIntoGroupWithUserDTO(Groups group) {
        return GroupWithUserDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .status(group.getStatus())
                .members(group.getMembers().stream().map(user -> {
                    try {
                        return userService.convertUserIntoDTO(user);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toUnmodifiableList()))
                .managers(group.getManagers().stream().map(user -> {
                    try {
                        return userService.convertUserIntoDTO(user);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toUnmodifiableList()))
                .owners(group.getOwners().stream().map(user -> {
                    try {
                        return userService.convertUserIntoDTO(user);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toUnmodifiableList()))
                .createdAt(group.getCreatedAt())
                .createdBy(group.getCreatedBy())
                .updatedAt(group.getUpdatedAt())
                .updatedBy(group.getUpdatedBy())
                .build();
    }

    @Override
    public boolean isGroupOneOfTheAdministrator(String groupName) {
        return CommonUtils.ADMINSTRATOR_GROUPS.contains(groupName);
    }

    @Override
    public List<GroupDTO> getGroupsByUserMembership(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Groups> groups = groupsRepository.findAllByMembersContains(user);
        return groups.stream().map(this::convertGroupIntoDTO).collect(Collectors.toList());
    }
}
