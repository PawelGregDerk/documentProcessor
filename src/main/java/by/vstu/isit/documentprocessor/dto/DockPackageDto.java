package by.vstu.isit.documentprocessor.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record DockPackageDto(
        Long id,
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
    public DockPackageDto {
        opers = (opers == null) ? new ArrayList<>() : new ArrayList<>(opers);
        sborEds = (sborEds == null) ? new ArrayList<>() : new ArrayList<>(sborEds);

        opers.sort(Comparator.comparing(
                OperDto::id,
                Comparator.nullsLast(Long::compareTo)
        ));

        sborEds.sort(Comparator.comparing(
                SborEdDto::id,
                Comparator.nullsLast(Long::compareTo)
        ));
    }
}
