package com.notesbackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.notesbackend.dto.UpdateUserDto;
import com.notesbackend.model.User;

@Mapper(componentModel = "spring")
public interface UpdateUserMapper {
    UpdateUserMapper INSTANCE = Mappers.getMapper(UpdateUserMapper.class);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromDto(UpdateUserDto userDto, @MappingTarget User user);
}
