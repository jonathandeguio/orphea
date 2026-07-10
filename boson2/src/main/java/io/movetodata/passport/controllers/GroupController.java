package io.movetodata.passport.controllers;

import io.movetodata.passport.library.models.GroupManagementSpecification;
import io.movetodata.passport.library.models.Groups;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.repository.GroupsRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.GroupService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/api/passport/groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;
    private final GroupsRepository groupsRepository;
    private final UserRepository userRepository;


    @Operation(summary = "It provides list of all groups")
    @GetMapping("/all")
    public ResponseEntity<List<Groups>> getGroups() {
        return ResponseEntity.ok().body(groupService.getGroups());
    }

    @Operation(summary = "To add new groups")
    @PostMapping("/add")
    public ResponseEntity<Groups> saveGroup(Principal principal, @RequestBody Groups groups) {
        UUID userId = userService.getUser(principal.getName()).id;

        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/passport/group/add").toUriString());

        List<User> user1 = userRepository.findAllById(Collections.singleton(userId));

        groups.setCreatedAt(new Date());
        groups.setCreatedBy(userId);

        groups.setOwners(user1);

        Groups createdGroup = groupService.saveGroup(groups);

        return ResponseEntity.created(uri).body(createdGroup);
    }


    @Operation(summary = "It provides Groups by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getById(@PathVariable("Id") UUID Id) {

        if (!groupsRepository.existsById(Id)) {
            return new ResponseEntity<>("groups with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }

        return new ResponseEntity<>(groupsRepository.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "It deletes Groups by Id")
    @PostMapping("/deleteById")
    public ResponseEntity<Object> deleteById(@RequestBody List<UUID> Ids) {

        for(UUID Id : Ids) {
            if (!groupsRepository.existsById(Id)) {
                return new ResponseEntity<>("groups with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);
            }
            groupsRepository.deleteById(Id);
        }

        return new ResponseEntity<>("Group deleted successfully.", HttpStatus.OK);
    }

    @Operation(summary = "It provides groups by Name")
    @GetMapping("/getByName/{name}")
    public ResponseEntity<?> getByName(@PathVariable("name") String name) {

        if (!groupsRepository.existsByName(name)) {
            return new ResponseEntity<>("groups with name " + name + " does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(groupsRepository.getByName(name), HttpStatus.OK);
    }

    @Operation(summary = "Manage members or administrators to group")
    @PostMapping("/manageGroups")
    public ResponseEntity<?> addMembers(Principal principal, @RequestBody GroupManagementSpecification groupManagementSpecification) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!groupsRepository.existsById(groupManagementSpecification.getId())) {
            return new ResponseEntity<>("groups with Id " + groupManagementSpecification.getId() + " does not exist", HttpStatus.NOT_FOUND);
        }

        System.out.println(groupManagementSpecification.getType());
        System.out.println(groupManagementSpecification.getAction());
        System.out.println(groupManagementSpecification.getId());

        String action = groupManagementSpecification.getAction();
        if (!"add".equals(action) && !"remove".equals(action)) {
            return ResponseEntity.badRequest().body("Action has to be 'add' or 'remove'");
        }


//        if(!Objects.equals(groupManagementSpecification.getType(), "members") || !Objects.equals(groupManagementSpecification.getType(), "administrators")) {
//            return new ResponseEntity<>("Type has to be 'members' or 'administrators'", HttpStatus.BAD_REQUEST);
//        }

        for (UUID groupUserId : groupManagementSpecification.getUserIds()) {

            if (!userRepository.existsById(groupUserId)) {
                return new ResponseEntity<>("User not found " + groupUserId, HttpStatus.NOT_FOUND);
            }
        }

        String type = groupManagementSpecification.getType();
        List<UUID> userIds = groupManagementSpecification.getUserIds();
        UUID groupId = groupManagementSpecification.getId();

        if ("add".equals(action)) {
            switch (type) {
                case "members":
                    groupService.removeUsersToGroupMembers(userId, userIds, groupId);
                    groupService.addUsersToGroupMembers(userId, userIds, groupId);
                    return ResponseEntity.ok("Member added to group successfully.");
                case "managers":
                    groupService.removeUsersToGroupManagers(userId, userIds, groupId);
                    groupService.addUsersToGroupManagers(userId, userIds, groupId);
                    return ResponseEntity.ok("Administrator added to group successfully.");
                case "owners":
                    groupService.removeUsersToGroupOwners(userId, userIds, groupId);
                    groupService.addUsersToGroupOwners(userId, userIds, groupId);
                    return ResponseEntity.ok("Owners added to group successfully.");
                default:
                    throw new IllegalArgumentException("Invalid group type: " + type);
            }
        } else if ("remove".equals(action)) {
            switch (type) {
                case "members":
                    groupService.removeUsersToGroupMembers(userId, userIds, groupId);
                    return ResponseEntity.ok("Member removed from group successfully.");
                case "managers":
                    groupService.removeUsersToGroupManagers(userId, userIds, groupId);
                    return ResponseEntity.ok("Managers removed from group successfully.");
                case "owners":
                    groupService.removeUsersToGroupOwners(userId, userIds, groupId);
                    return ResponseEntity.ok("Owners removed from group successfully.");
                default:
                    throw new IllegalArgumentException("Invalid group type: " + type);
            }
        } else {
            throw new IllegalArgumentException("Invalid group action: " + action);
        }

//
//        return new ResponseEntity<>("There was some problem in updating the group.", HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "This can be used to rename groups.")
    @GetMapping("/{Id}/{newName}/rename")
    public ResponseEntity<Object> renameGroup(Principal principal, @PathVariable("Id") UUID id, @PathVariable("newName") String newName) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!groupsRepository.existsById(id)) {
            return new ResponseEntity<>("No group found for given Id", HttpStatus.NOT_FOUND);
        }

        Groups groups = groupsRepository.getById(id);

        groups.setName(newName);

        groupsRepository.save(groups);

        return new ResponseEntity<>("Group renamed successfully", HttpStatus.OK);

    }

    @Operation(summary = "This can be used to change description of group.")
    @GetMapping("/{Id}/{newDescription}/renameDescription")
    public ResponseEntity<Object> renameDescription(Principal principal, @PathVariable("Id") UUID id, @PathVariable("renameDescription") String renameDescription) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!groupsRepository.existsById(id)) {
            return new ResponseEntity<>("No group found for given Id", HttpStatus.NOT_FOUND);
        }

        Groups groups = groupsRepository.getById(id);

        groups.setDescription(renameDescription);

        groupsRepository.save(groups);

        return new ResponseEntity<>("Chart Description changed successfully", HttpStatus.OK);
    }
}
