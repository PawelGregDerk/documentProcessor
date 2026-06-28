package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.entities.TypeOper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TypeOperFuncMapper.class})
public interface TypeOperMapper {

    @Mapping(target = "funcs", source = "typeOperFuncs")
    TypeOperDto toDto(TypeOper entity);

    @Mapping(target = "typeOperFuncs", ignore = true)
    TypeOper toEntity(TypeOperDto dto);

    // Добавить эти методы:
    List<TypeOperDto> toDtoList(List<TypeOper> entities);
    List<TypeOper> toEntityList(List<TypeOperDto> dtos);
}