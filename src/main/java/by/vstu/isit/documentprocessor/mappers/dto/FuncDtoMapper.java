package by.vstu.isit.documentprocessor.mappers.dto;

import by.vstu.isit.documentprocessor.controllers.form.FuncFormState;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import org.springframework.stereotype.Component;

@Component
public class FuncDtoMapper {

    public FuncDto toDto(FuncFormState f) {
        return new FuncDto(
                f.name(),
                f.param(),
                f.isProd(),
                f.spec()
        );
    }
}

