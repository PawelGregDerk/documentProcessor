package by.vstu.isit.documentprocessor.dto;

import java.util.List;

public record DockPackageDto(
        String packageName,
        String path,
        String puName,
        String spuName,
        String kpName,
        String fmeaName,
        String vedIName,
        List<OperDto> opers,
        List<SborEdDto> sborEds
) {
}
