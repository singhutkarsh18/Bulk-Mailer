package com.example.bulkmailer.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter@Setter @EqualsAndHashCode
@Entity
public class AppUser implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String username;
    private String password;
    private Boolean locked=true;
    private Boolean enabled=false;
    private String role="USER";
    private int otp;

    @JsonIgnore
    @OneToMany(mappedBy = "appUser",cascade = CascadeType.ALL)
    private Set<Groups> groups=new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "appUser",cascade = CascadeType.ALL)
    private Set<PreviousMail> previousMails = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "appUser",cascade = CascadeType.ALL)
    private Set<Template> templates = new HashSet<>();

    public AppUser(String name, String username, String password,int otp) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.otp=otp;
    }

    public AppUser() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return locked;
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
