package io.movetodata.passport.controllers;

import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.DTO.PermissionMappingWithIdentityAndInheritance;
import io.movetodata.passport.library.models.*;
import io.movetodata.passport.library.repository.GroupsRepository;
import io.movetodata.passport.library.repository.PermissionMappingRepository;
import io.movetodata.passport.library.repository.RoleRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.GroupService;
import io.movetodata.passport.library.service.PermissionMappingService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static io.movetodata.passport.util.CommonUtils.ADMINSTRATOR_GROUPS;
import static io.movetodata.passport.util.CommonUtils.PLATFORM_ADMINISTRATOR;

@CrossOrigin
@RestController
@RequestMapping("/api/passport/permissionsMapping")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class PermissionsMappingController {
    private final GroupService groupService;
    private final UserService userService;
    private final GroupsRepository groupsRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ResourceService resourceService;
    private final PermissionMappingRepository permissionMappingRepository;
    private final PermissionMappingService permissionMappingService;


    @Operation(summary = "It provides list of all Permissions Mapping")
    @GetMapping("/all")
    public ResponseEntity<List<PermissionsMapping>> getRoles() {
        return ResponseEntity.ok().body(permissionMappingRepository.findAll());
    }


    @Operation(summary = "It provides Permissions Mapping by Id")
    @GetMapping("/getById/{Id}")
    public ResponseEntity<Object> getById(@PathVariable("Id") UUID Id) {

        if (!permissionMappingRepository.existsById(Id)) {
            return new ResponseEntity<>("permissions mapping with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }
        return new ResponseEntity<>(permissionMappingRepository.findById(Id), HttpStatus.OK);
    }

    @Operation(summary = "It deletes Permissions Mapping by Id")
    @DeleteMapping("/deleteById/{Id}")
    public ResponseEntity<Object> DeleteById(@PathVariable("Id") UUID Id) {

        // TODO : permission , only if you are owner of resource or platform-admin
        
        if (!permissionMappingRepository.existsById(Id)) {
            return new ResponseEntity<>("permissions mapping with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }
        permissionMappingRepository.deleteById(Id);

        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }

    @Operation(summary = "It deletes Permissions Mapping by resourceId and identityId and roleId")
    @PostMapping("/deleteByIdentityIdAndResourceIdAndRoleId")
    public ResponseEntity<Object> DeleteByIdentityIdAndResourceIdAndRoleId(@RequestBody PermissionsMapping permissionsMapping) {

        // TODO : permission , only if you are owner of resource or platform-admin


        if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(permissionsMapping.getIdentityId(), permissionsMapping.getResourceId(), permissionsMapping.getRole().getId())) {
            return new ResponseEntity<>("permissions mapping with IdentityId " + permissionsMapping.getIdentityId() + " and resourceID " + permissionsMapping.getResourceId() + " and roleId " + permissionsMapping.getRole().getId() + " does not exist", HttpStatus.NOT_FOUND);
        }
        PermissionsMapping permissionsMappingExisting = permissionMappingRepository.findByIdentityIdAndResourceIdAndRoleId(permissionsMapping.getIdentityId(), permissionsMapping.getResourceId(), permissionsMapping.getRole().getId());
        permissionMappingRepository.delete(permissionsMappingExisting);

        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }

    @Operation(summary = "It updates Permissions Mapping")
    @PostMapping("/update")
    public ResponseEntity<Object> Update(Principal principal, @RequestBody List<PermissionsMapping> permissionsMapping) {

        UUID userId = userService.getUser(principal.getName()).getId();

        // TODO : permission , only if you are owner of resource or platform-admin

        for (PermissionsMapping permissionsMapping1 : permissionsMapping) {

            if (!permissionMappingRepository.existsById(permissionsMapping1.getId())) {
                return new ResponseEntity<>("permission mapping " + permissionsMapping1.getId() + " does not exist", HttpStatus.NOT_FOUND);
            }

            PermissionsMapping permissionsMappingExisting = permissionMappingRepository.getById(permissionsMapping1.getId());
            permissionsMappingExisting.setUpdatedBy(userId);
            permissionsMappingExisting.setUpdatedAt(new Date());

            Role role = roleRepository.getById(permissionsMapping1.getRole().getId());

            permissionsMappingExisting.setRole(role);

            permissionMappingRepository.save(permissionsMappingExisting);

//            copyNonNullProperties(permissionsMapping1, permissionsMappingExisting);
        }

        return new ResponseEntity<>("Permissions updated", HttpStatus.OK);
    }

    @Operation(summary = "It provides permissions mapping by identityId and resourceId and roleId")
    @GetMapping("/{identityId}/{resourceId}/{roleId}")
    public ResponseEntity<?> getByIdentityIdAndResourceIdAndRoleId(@PathVariable("identityId") UUID identityId, @PathVariable("resourceId") UUID resourceId, @PathVariable("roleId") UUID roleId) {

        if (!permissionMappingRepository.existsByIdentityIdAndResourceIdAndRoleId(identityId, resourceId, roleId)) {
            return new ResponseEntity<>("permissions mapping with IdentityId " + identityId + " and resourceId " + resourceId + "and roleId" + roleId + " does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(permissionMappingRepository.findByIdentityIdAndResourceIdAndRoleId(identityId, resourceId, roleId), HttpStatus.OK);
    }

    @Operation(summary = "It provides permissions mapping by identityId")
    @GetMapping("/getByIdentityId/{identityId}")
    public ResponseEntity<?> getByIdentityId(@PathVariable("identityId") UUID identityId) {

        if (!permissionMappingRepository.existsByIdentityId(identityId)) {
            return new ResponseEntity<>("permissions mapping with IdentityId " + identityId + " does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(permissionMappingRepository.findByIdentityId(identityId), HttpStatus.OK);
    }

    @Operation(summary = "It provides permissions mapping by resourceId")
    @GetMapping("/getByResourceId/{resourceId}")
    public ResponseEntity<Object> getByResourceId(@PathVariable("resourceId") UUID resourceId) throws IOException {
        //{isInherited,List<Permissions>}
        HashMap<Boolean, List<PermissionsMapping>> permissionsMap = new HashMap<>();
        permissionsMap.put(false, permissionMappingRepository.findByResourceId(resourceId));
        permissionsMap.put(true, new ArrayList<>());

        UUID parentResourceId = resourceService.getResourceModel(resourceId).getParent();
        while (parentResourceId != null) {
            permissionsMap.get(true).addAll(permissionMappingRepository.findByResourceId(parentResourceId));
            parentResourceId = resourceService.getResourceModel(parentResourceId).getParent();

        }

        if (permissionsMap.isEmpty()) {
            return new ResponseEntity<>("permissions mapping with resourceId " + resourceId + " does not exist", HttpStatus.NOT_FOUND);
        }

        List<PermissionMappingWithIdentityAndInheritance> response = new ArrayList<>();
        for (Map.Entry<Boolean, List<PermissionsMapping>> entry : permissionsMap.entrySet()) {
            for (PermissionsMapping pmg : entry.getValue()) {
                PermissionMappingWithIdentityAndInheritance permissionMapping = new PermissionMappingWithIdentityAndInheritance();

                // Checking if identity is group or user
                Groups group = groupsRepository.findById(pmg.getIdentityId()).stream().findFirst().orElse(null);
                if (group == null) {
                    User user = userRepository.findById(pmg.getIdentityId()).stream().findFirst().orElse(null);
                    if(user == null || PLATFORM_ADMINISTRATOR.equals(user.getUsername()))
                        continue;
                    permissionMapping.setIdentity(userService.convertUserIntoDTO(user));
                } else {
                    if(ADMINSTRATOR_GROUPS.contains(group.getName()))
                        continue;
                    permissionMapping.setIdentity(groupService.convertGroupIntoDTO(group));
                }

                permissionMapping.setInherited(entry.getKey());
                permissionMapping.setResourceId(pmg.getResourceId());
                permissionMapping.setRole(pmg.getRole());
                permissionMapping.setId(pmg.getId());

                response.add(permissionMapping);
            }
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "It provides permissions mapping by roleId")
    @GetMapping("/getByRoleId/{roleId}")
    public ResponseEntity<?> getByRoleId(@PathVariable("roleId") UUID roleId) {

        if (!permissionMappingRepository.existsByRoleId(roleId)) {
            return new ResponseEntity<>("permissions mapping with roleId " + roleId + " does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(permissionMappingRepository.findByRoleId(roleId), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create permission Mapping.")
    @PostMapping("/create")
    public ResponseEntity<Object> newPermissionsMapping(@AuthenticationPrincipal AuthUser authUser,
                                                        @Valid @RequestBody PermissionsMappingSpecification permissionsMappingSpecification) {
        permissionMappingService.handleCreatePermissionMapping(authUser.getId(),
                permissionsMappingSpecification.getIdentityId(),
                permissionsMappingSpecification.getResourceId(),
                permissionsMappingSpecification.getRoleId());
        return new ResponseEntity<>("Permission mapping added successfully", HttpStatus.OK);
    }
}
