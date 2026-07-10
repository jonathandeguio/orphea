package io.movetodata.passport.security;

import io.movetodata.passport.library.models.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class AuthUser implements OAuth2User, UserDetails {
    @Getter
    private final UUID id;

    @Getter
    private final String username;

    @Getter
    @Setter
    private String language;

    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    @Setter
    private Map<String, Object> attributes;

    public AuthUser(UUID id, String username, String language, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.language = language;
        this.password = password;
        this.authorities = authorities;
    }

    public static AuthUser create(User user) {
        List<GrantedAuthority> authorities = Collections.
                singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new AuthUser(
                user.getId(),
                user.getUsername(),
                user.getPreferences().getLanguage(),
                user.getPassword(),
                authorities);
    }

    public static AuthUser create(User user, Map<String, Object> attributes) {
        AuthUser authUser = AuthUser.create(user);
        authUser.setAttributes(attributes);
        return authUser;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}