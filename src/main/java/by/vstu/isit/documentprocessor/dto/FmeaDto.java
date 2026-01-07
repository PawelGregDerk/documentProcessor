package by.vstu.isit.documentprocessor.dto;

import java.util.List;

public record FmeaDto(
        String packageName,
        String puName,
        String spuName,
        String kpName,
        String fmeaName,
        String vedInstrName,
        List<FmeaOperDto> operations
) {
}
