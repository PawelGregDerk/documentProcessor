package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OperMapper.class, SborEdMapper.class})
public interface DocpackageMapper {

    @Mapping(target = "opers", source = "opers")
    @Mapping(target = "sborEds", source = "sborEds")
    DockPackageDto toDto(Docpackage entity);

    @Mapping(target = "opers", source = "opers")
    @Mapping(target = "sborEds", source = "sborEds")
    Docpackage toEntity(DockPackageDto dto);
}