package com.example.appearanceRater.user;

import com.example.appearanceRater.token.Token;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Entity
@Table(name = "_user")
@RequiredArgsConstructor
@AllArgsConstructor
public class UserEntity extends User implements UserDetails {
    @Id
    @GeneratedValue
    private Integer id;
    @Enumerated(EnumType.ORDINAL)
    private Role role;
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<Token> token;
    private boolean enabled;
    private boolean accountNonLocked;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Role.USER.name()));
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
