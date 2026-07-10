package io.movetodata.passport.library.service;

import io.movetodata.passport.library.models.Permissions;
import io.movetodata.passport.library.models.PermissionsMapping;
import io.movetodata.passport.library.models.Role;
import io.movetodata.passport.library.models.Users;
import io.movetodata.passport.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepo userRepo;
    private final GroupsRepo groupsRepo;
    private final TokenRepo tokenRepo;
    private final RoleRepo roleRepo;
    private final PermissionRepo permissionRepo;
    private final PermissionMappingRepo permissionMappingRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepo.findByUsername(username);
        if(user == null) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException(("User not found in the database"));
        }
        else {
            log.info("User found in the database: {}", username);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
//        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }


    @Override
    //saving users by saving raw passwords
    //public AppUser saveUser(AppUser user) {
      //  log.info("Saving new user {} to the database", user.getName());
        //return userRepo.save(user);
    public Users saveUser(Users user) {
        log.info("Saving new user {} to the database", user.getName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    @Override
    public Role saveRole(Role role) {
        log.info("Saving new role {} to the database", role.getName());
        return roleRepo.save(role);
    }

    @Override
    public Permissions savePermissions(Permissions permissions) {
        log.info("Saving new role {} to the database", permissions.getId());
        return permissionRepo.save(permissions);
    }


    @Override
    public PermissionsMapping savePermissionsMapping(PermissionsMapping permissionsMapping) {
        log.info("Saving new role {} to the database", permissionsMapping.getId());
        return permissionMappingRepo.save(permissionsMapping);
    }




//    @Override
//    public void addRoleToUser(String username, String roleName) {
//        log.info("Adding role {} to user {}", roleName, username);
//        AppUser user = userRepo.findByUsername(username);
//        Role role = roleRepo.findByName(roleName);
//        user.getRoles().add(role);
//
//    }

//    public Groups saveGroup(Groups groups) {
//        log.info("Saving new group {} to the database", groups.getName());
//        return groupsRepo.save(groups);
//    }
//
//    @Override
//    public void addUserToGroupOwners(String username, String groupName) {
//        log.info("Adding user {} to group Owners {}", groupName, username);
//        Users user = userRepo.findByUsername(username);
//        Groups group = groupsRepo.findByName(groupName);
//
//        group.getGroups_owners().add(user);
//
//    }
//
//    @Override
//    public void addUserToGroupAdministrators(String username, String groupName) {
//        log.info("Adding user {} to group Administrators {}", groupName, username);
//        Users user = userRepo.findByUsername(username);
//        Groups group = groupsRepo.findByName(groupName);
//
//        group.getGroups_administrators().add(user);
//
//    }
//
//    @Override
//    public void addUserToGroupMembers(String username, String groupName) {
//        log.info("Adding user {} to group Members {}", groupName, username);
//        Users user = userRepo.findByUsername(username);
//        Groups group = groupsRepo.findByName(groupName);
//
//        group.getGroups_members().add(user);
//
//    }

    @Override
    public Users getUser(String username) {
        log.info("Fetching user {} ", username);
        return userRepo.findByUsername(username);
    }

    @Override
    public List<Users> getUsers() {
        log.info("Fetching all users");
        return userRepo.findAll();
    }

    @Override
    public List<Role> getRole() {
        log.info("Fetching all users");
        return roleRepo.findAll();
    }

    @Override
    public List<Permissions> getPermissions() {
        log.info("Fetching all users");
        return permissionRepo.findAll();
    }

    @Override
    public List<PermissionsMapping> getPermissionsMapping() {
        log.info("Fetching all users");
        return permissionMappingRepo.findAll();
    }



//    @Override
//    public List<Groups> getGroups() {
//        log.info("Fetching all groups");
//        return groupsRepo.findAll();
//    }

//    @Override
//    public TokenLongLived saveLongLivedToken(TokenLongLived tokenLongLived) {
//        log.info("Saving new longlived token {} to the database", tokenLongLived.getName());
//        return tokenRepo.save(tokenLongLived);
//    }
//
//    @Override
//    public List<TokenLongLived> getMyLongLivedTokens(UUID userId) {
//        log.info("Fetching all token for the user");
//        return tokenRepo.getByUserId(userId);
//    }
}
