package io.movetodata.passport.controllers;

import io.movetodata.passport.library.models.Groups;
import io.movetodata.passport.library.repository.GroupsRepo;
import io.movetodata.passport.library.service.GroupService;
import io.movetodata.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api/passport/Groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;
    private final GroupsRepo groupsRepo;


    @Operation(summary = "It provides list of all groups")
    @GetMapping("/all")
    public ResponseEntity<List<Groups>> getGroups() {
        return ResponseEntity.ok().body(groupService.getGroups());
    }


    @Operation(summary = "It provides Groups by Id")
    @GetMapping("/GetById/{Id}")
    public ResponseEntity<Object> getById(@PathVariable("Id") UUID Id) {

        if (!groupsRepo.existsById(Id)) {
            return new ResponseEntity<>("groups with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }
        return new ResponseEntity<>(groupsRepo.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "It provides groups by Name")
    @GetMapping("/GetByName/{name}")
    public ResponseEntity<?> getByName(@PathVariable("name") String name) {

        if (!groupsRepo.existsByName(name)) {
            return new ResponseEntity<>("groups with name " + name + " does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(groupsRepo.getByName(name), HttpStatus.OK);
    }
}
