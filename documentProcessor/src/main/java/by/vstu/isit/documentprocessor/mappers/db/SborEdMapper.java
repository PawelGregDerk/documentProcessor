package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.entities.SborEd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SborEdMapper {

    @Mapping(target = "idDocpackage", source = "docpackage.id")
    SborEdDto toDto(SborEd entity);

    @Mapping(target = "docpackage", ignore = true)
    SborEd toEntity(SborEdDto dto);

    List<SborEdDto> toDtoList(List<SborEd> entities);
    List<SborEd> toEntityList(List<SborEdDto> dtos);
}
