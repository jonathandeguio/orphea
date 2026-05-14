package io.orphea.passport.library.service;

import io.orphea.passport.library.models.Groups;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.repository.GroupsRepository;
import io.orphea.passport.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    public void addUserToGroupOwners(String username, String groupName) {
        log.info("Adding user {} to group Administrators {}", groupName, username);
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username : " + username)
        );
        Groups group = groupsRepository.findByName(groupName);

        group.getOwners().add(user);

    }

    @Override
    public void addUserToGroupMembers(String username, String groupName) {
        log.info("Adding user {} to group Members {}", groupName, username);
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username : " + username)
        );
        Groups group = groupsRepository.findByName(groupName);

        group.getMembers().add(user);

    }

    @Override
    public void addUsersToGroupMembers(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Adding user {} to group Members {}", groupId, userIds);

        for (UUID userId : userIds) {
            System.out.println(userId);

//            if(!userRepository.existsById(userId)){
//                return;
//            }
            User user = userRepository.getById(userId);
            Groups group = groupsRepository.getById(groupId);

            if (!group.getMembers().contains(userId)) {
                group.getMembers().add(user);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }

    }

    @Override
    public void addUsersToGroupManagers(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Adding user {} to group Managers {}", groupId, userIds);


        for (UUID userId : userIds) {

            User user = userRepository.getById(userId);
            Groups group = groupsRepository.getById(groupId);

            if (!group.getManagers().contains(userId)) {
                group.getManagers().add(user);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }

    }

    @Override
    public void addUsersToGroupOwners(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Adding user {} to group Owners {}", groupId, userIds);


        for (UUID userId : userIds) {

            User user = userRepository.getById(userId);
            Groups group = groupsRepository.getById(groupId);

            if (!group.getOwners().contains(userId)) {
                group.getOwners().add(user);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }

    }

    @Override
    public void removeUsersToGroupMembers(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Removing user {} to group Members {}", groupId, userIds);

        for (UUID userId : userIds) {

            User user = userRepository.getById(userId);
            Groups group = groupsRepository.getById(groupId);

            if (group.getMembers().contains(userId)) {
                group.getMembers().remove(user);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }

    }

    @Override
    public void removeUsersToGroupManagers(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Removing user {} to group Managers {}", groupId, userIds);

        for (UUID userId : userIds) {
            User user = userRepository.getById(userId);
            Groups group = groupsRepository.getById(groupId);

            if (group.getManagers().contains(userId)) {
                group.getManagers().remove(user);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }

    }

    @Override
    public void removeUsersToGroupOwners(UUID adminId, List<UUID> userIds, UUID groupId) {
        log.info("Removing user {} to group Owners {}", groupId, userIds);

        for (UUID userId : userIds) {

            User user = userRepository.getById(userId);
            Groups group = groupsRepository.getById(groupId);

            if (group.getOwners().contains(userId)) {
                group.getOwners().remove(user);
                group.setUpdatedAt(new Date());
                group.setUpdatedBy(adminId);
            }
        }

    }

    @Override
    public List<Groups> getGroups() {
        log.info("Fetching all groups");
        return groupsRepository.findAll();
    }
}
