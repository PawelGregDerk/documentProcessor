package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.entities.Oper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {FuncMapper.class, TypeOperMapper.class})
public interface OperMapper {

    @Mapping(target = "idDocPackage", source = "docpackage.id")
    @Mapping(target = "idTypeOper", source = "typeOper.id")
    @Mapping(target = "funcs", source = "funcs")
    OperDto toDto(Oper entity);

    @Mapping(target = "docpackage", ignore = true)
    @Mapping(target = "typeOper", ignore = true)
    @Mapping(target = "funcs", ignore = true)
    Oper toEntity(OperDto dto);

    List<OperDto> toDtoList(List<Oper> entities);
    List<Oper> toEntityList(List<OperDto> dtos);

    @AfterMapping
    default void setOperFk(@MappingTarget Oper entity, OperDto dto) {
        if (dto.idDocPackage() != null) {
            entity.setDocpackage(by.vstu.isit.documentprocessor.entities.Docpackage.builder().id(dto.idDocPackage()).build());
        }
        if (dto.idTypeOper() != null) {
            entity.setTypeOper(by.vstu.isit.documentprocessor.entities.TypeOper.builder().id(dto.idTypeOper()).build());
        }
    }
}
