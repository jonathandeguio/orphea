package io.orphea.passport.library.service;

import io.orphea.passport.library.models.PermissionsMapping;
import io.orphea.passport.library.models.Role;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final GroupsRepository groupsRepository;
    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;
    private final PermissionMappingRepository permissionMappingRepository;
//    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username : " + username)
        );
        if (user == null) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException(("User not found in the database"));
        } else {
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
    public User saveUser(User user) {
        log.info("Saving new user {} to the database", user.getUsername());
//        user.setPassword(passwordEncoder.encode(user.getPassword()));  // TODO : springboot 3
        return userRepository.save(user);
    }

    @Override
    public Role saveRole(Role role) {
        log.info("Saving new role {} to the database", role.getName());
        return roleRepository.save(role);
    }


    @Override
    public PermissionsMapping savePermissionsMapping(PermissionsMapping permissionsMapping) {
        log.info("Saving new role {} to the database", permissionsMapping.getId());
        return permissionMappingRepository.save(permissionsMapping);
    }


    @Override
    public User getUser(String username) {
//        log.info("Fetching user {} ", username);
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username : " + username)
        );
    }

    @Override
    public List<User> getUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public List<Role> getRole() {
        log.info("Fetching all users");
        return roleRepository.findAll();
    }


    @Override
    public List<PermissionsMapping> getPermissionsMapping() {
        log.info("Fetching all users");
        return permissionMappingRepository.findAll();
    }

}
