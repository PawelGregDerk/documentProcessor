package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.repos.DocpackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.isit.documentprocessor.entities.db.Docpackage;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DocpackageServiceImpl implements DocpackageService {
    @Setter(onMethod_ = {@Autowired})
    @Getter
    private  DocpackageRepository repository;

    @Override
    @Transactional
    public by.vstu.isit.documentprocessor.entities.db.Docpackage updateDocpackage(Docpackage docpackage) {
        return repository.save(docpackage);
    }
}