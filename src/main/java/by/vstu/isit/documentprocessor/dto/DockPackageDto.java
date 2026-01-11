package by.vstu.isit.documentprocessor.dto;

import lombok.experimental.Delegate;

import java.util.List;

public record DockPackageDto(
        String packageName,
        String puName,
        String spuName,
        String kpName,
        String fmeaName,
        String vedIName,
        String extra,
        @Delegate
        List<OperDto> opers
) {
}
