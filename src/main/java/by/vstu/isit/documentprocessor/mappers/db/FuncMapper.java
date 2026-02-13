package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.entities.Func;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FuncMapper {

    @Mapping(target = "idOper", source = "oper.id")
    FuncDto toDto(Func entity);

    @Mapping(target = "oper", ignore = true)
    Func toEntity(FuncDto dto);

    List<FuncDto> toDtoList(List<Func> entities);
    List<Func> toEntityList(List<FuncDto> dtos);
}
