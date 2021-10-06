package com.biometricsystem.security;
import java.util.*;
import com.biometricsystem.entity.employee.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public class EmployeeDetails implements UserDetails {

    private final Employee employee;

    public EmployeeDetails(Employee employee) {
        this.employee = employee;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<EmployeeType> authorities = new ArrayList<>();
        authorities.add(employee.getEmployeeType());
        return authorities;
    }

    @Override
    public String getPassword() {
        return String.valueOf(employee.getId());
    }

    @Override
    public String getUsername() {
        return String.valueOf(employee.getEmployeeNumber());
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

}