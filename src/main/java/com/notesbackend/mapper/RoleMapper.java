package com.notesbackend.mapper;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.notesbackend.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {
    public List<Role> map(String roles) {
        return Arrays.stream(roles.split(","))
                     .map(roleName -> {
                         Role role = new Role();
                         role.setName(roleName.trim());
                         return role;
                     })
                     .collect(Collectors.toList());
    }
}
