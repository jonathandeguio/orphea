package io.bosler.passport.controllers;

import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.passport.library.models.*;
import io.bosler.passport.library.repository.GroupsRepository;
import io.bosler.passport.library.repository.PermissionMappingRepository;
import io.bosler.passport.library.repository.RoleRepository;
import io.bosler.passport.library.repository.UserRepository;
import io.bosler.passport.library.service.GroupService;
import io.bosler.passport.library.service.UserService;
import io.bosler.passport.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;

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
    private final FolderRepository folderRepository;
    private final PermissionMappingRepository permissionMappingRepository;


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

        if (!permissionMappingRepository.existsById(Id)) {
            return new ResponseEntity<>("permissions mapping with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }
        permissionMappingRepository.deleteById(Id);

        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }

    @Operation(summary = "It deletes Permissions Mapping by resourceId and identityId and roleId")
    @PostMapping("/deleteByIdentityIdAndResourceIdAndRoleId")
    public ResponseEntity<Object> DeleteByIdentityIdAndResourceIdAndRoleId(@RequestBody PermissionsMapping permissionsMapping) {

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

        UUID userId = userService.getUser(principal.getName()).id;

        for(PermissionsMapping permissionsMapping1 : permissionsMapping) {

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
    public ResponseEntity<?> getByResourceId(@PathVariable("resourceId") UUID resourceId) {

        if (!permissionMappingRepository.existsByResourceId(resourceId)) {
            return new ResponseEntity<>("permissions mapping with resourceId " + resourceId + " does not exist", HttpStatus.NOT_FOUND);
        }

        /*
         * { identity : group or user,
         *  resource : resource dataset, folder, repo
         * role: role
         * }
         *
         *
         *
         * */

        List<PermissionsMapping> permissionsMapping = permissionMappingRepository.findByResourceId(resourceId);

        if(permissionsMapping.isEmpty()) {
            return new ResponseEntity<>("permissions mapping with resourceId " + resourceId + " does not exist", HttpStatus.NOT_FOUND);
        }

        List<Object> result = new ArrayList<>();

        for (PermissionsMapping permissionsMapping1 : permissionsMapping) {

            HashMap<String, Object> result_dict = new HashMap<>();

            // search into group or users to fill identity

            FolderModel folderModel = folderRepository.findById(permissionsMapping1.getResourceId()).stream().findFirst().orElse(null);

            Groups groups = groupsRepository.findById(permissionsMapping1.getIdentityId()).stream().findFirst().orElse(null);

            if (groups == null) {
                User user = userRepository.findById(permissionsMapping1.getIdentityId()).stream().findFirst().orElse(null);
                result_dict.put("identity", user);
            } else {
                result_dict.put("identity", groups);
            }

            result_dict.put("id", permissionsMapping1.getId());

            result_dict.put("resourceId", folderModel);  // replace with resource
            result_dict.put("role", permissionsMapping1.getRole());

            result.add(result_dict);

        }

        return new ResponseEntity<>(result, HttpStatus.OK);
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
    public ResponseEntity<Object> newPermissionsMapping(Principal principal, @Valid @RequestBody PermissionsMappingSpecification permissionsMappingSpecification) {

        UUID userId = userService.getUser(principal.getName()).id;

        // TODO : add validations on three fields

        for (UUID identity : permissionsMappingSpecification.getIdentityId()) {

            PermissionsMapping newPermissionsMapping = new PermissionsMapping();

            newPermissionsMapping.setIdentityId(identity);
            newPermissionsMapping.setResourceId(permissionsMappingSpecification.getResourceId());
            newPermissionsMapping.setRole(roleRepository.getById(permissionsMappingSpecification.getRoleId()));

            newPermissionsMapping.setStatus("active");
            newPermissionsMapping.setCreatedBy(userId);
            newPermissionsMapping.setCreatedAt(new Date());

            permissionMappingRepository.save(newPermissionsMapping);
        }

        return new ResponseEntity<>("Permission mapping added successfully", HttpStatus.OK);

    }
}
