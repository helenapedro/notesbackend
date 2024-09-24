package com.notesbackend.repository;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.notesbackend.dto.RegisterUserDto;
import com.notesbackend.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "password", ignore = true)
    User toUser(RegisterUserDto userDto);
    
    void updateUserFromDto(RegisterUserDto userDto, @MappingTarget User user);
}