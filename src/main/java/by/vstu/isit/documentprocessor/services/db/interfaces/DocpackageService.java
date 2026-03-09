package by.vstu.isit.documentprocessor.services.db.interfaces;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;

import java.util.List;

public interface DocpackageService extends Service<Docpackage, Long, DocpackageRepository> {
    Docpackage updateDocpackage(Docpackage docpackage);
    DockPackageDto getLastPackageDto();
    DockPackageDto saveFullPackage(DockPackageDto dto);
    
    // Методы поиска
    List<DockPackageDto> searchByOboznach(String oboznach);
    List<DockPackageDto> searchByPackageName(String packageName);
}