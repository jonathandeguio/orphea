package io.orphea.passport.controllers;

import io.orphea.passport.library.models.Role;
import io.orphea.passport.library.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

import static io.orphea.sharedutils.Utils.copyNonNullProperties;

@CrossOrigin
@RestController
@RequestMapping("/api/passport/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class RoleController {
    private final RoleRepository roleRepository;


    @Operation(summary = "It provides list of all roles")
    @GetMapping("/all")
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok().body(roleRepository.findAll());
    }


    @Operation(summary = "It provides Roles by Id")
    @GetMapping("/getById/{Id}")
    public ResponseEntity<Object> getById(@PathVariable("Id") UUID Id) {

        if (!roleRepository.existsById(Id)) {
            return new ResponseEntity<>("role with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }
        return new ResponseEntity<>(roleRepository.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "It deletes Roles by Id")
    @DeleteMapping("/deleteById/{Id}")
    public ResponseEntity<Object> DeleteById(@PathVariable("Id") UUID Id) {

        if (!roleRepository.existsById(Id)) {
            return new ResponseEntity<>("role with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }
        roleRepository.deleteById(Id);

        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }

    @Operation(summary = "It updates Roles")
    @PostMapping("/update")
    public ResponseEntity<Object> update(@RequestBody Role role) {

        if (!roleRepository.existsById(role.getId())) {
            return new ResponseEntity<>("role with Id " + role.getId() + " does not exist", HttpStatus.NOT_FOUND);
        }

        Role roleExisting = roleRepository.getById(role.getId());

        copyNonNullProperties(role, roleExisting);

        return new ResponseEntity<>(roleRepository.save(roleExisting), HttpStatus.OK);
    }
}
