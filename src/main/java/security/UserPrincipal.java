package security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import model.User.User; // Đảm bảo import User entity của bạn
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
    private static final long serialVersionUID = 1L; // Good practice for Serializable classes

    private Long id;
    private String username;
    private String email;
    @JsonIgnore // Không bao giờ serialize password ra ngoài
    private String password;
    private boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String email, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                authorities
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Bạn có thể thêm logic nếu có trường accountExpiryDate
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Bạn có thể thêm logic nếu có trường accountLocked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Bạn có thể thêm logic nếu có trường credentialsExpiryDate
    }

    @Override
    public boolean isEnabled() {
        return this.enabled; // Sử dụng trường enabled từ User entity
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
