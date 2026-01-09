package by.vstu.isit.documentprocessor.mappers.dto;

import by.vstu.isit.documentprocessor.controllers.form.DockPackageFormState;
import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DockPackageDtoMapper {

    private final OperDtoMapper operMapper;

    public DockPackageDto toDto(DockPackageFormState form) {
        return new DockPackageDto(
                form.packageName(),
                form.pu(),
                form.spu(),
                form.kp(),
                form.fmea(),
                form.vedInstr(),
                form.operations().stream()
                        .map(operMapper::toDto)
                        .toList()
        );
    }
}
