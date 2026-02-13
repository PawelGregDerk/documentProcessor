package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TypeOperFuncMapper {

    @Mapping(target = "idTypeOper", source = "typeOper.id")
    TypeOperFuncDto toDto(TypeOperFunc entity);

    @Mapping(target = "typeOper", ignore = true)
    TypeOperFunc toEntity(TypeOperFuncDto dto);
}
