package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.entity.AdEntity;

@Mapper(componentModel = "spring")
public interface AdMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "author", ignore = true)
    AdEntity toEntity(CreateOrUpdateAd dto);

    @Mapping(source = "id", target = "pk", qualifiedByName = "longToInt")
    @Mapping(source = "author.id", target = "author", qualifiedByName = "longToInt")
    Ad toDto(AdEntity entity);

    @Mapping(source = "id", target = "pk", qualifiedByName = "longToInt")
    @Mapping(source = "author.firstName", target = "authorFirstName")
    @Mapping(source = "author.lastName", target = "authorLastName")
    @Mapping(source = "author.email", target = "email")
    @Mapping(source = "author.phone", target = "phone")
    ExtendedAd toExtendedDto(AdEntity entity);

    void updateEntityFromDto(CreateOrUpdateAd dto, @MappingTarget AdEntity entity);

    @Named("longToInt")
    default Integer longToInt(Long value) {
        return value != null ? value.intValue() : null;
    }
}
