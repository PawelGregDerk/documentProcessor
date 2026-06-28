package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.mappers.db.TypeOperMapper;
import by.vstu.isit.documentprocessor.repositories.TypeOperRepository;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeOperServiceImpl implements TypeOperService {
    @Getter
    private final TypeOperRepository repository;

    private final TypeOperMapper typeOperMapper;
    
    @Override
    @Transactional(readOnly = true)
    public List<TypeOperDto> findAll() {
        List<TypeOper> typeOpers = repository.findAll();
        return typeOperMapper.toDtoList(typeOpers);
    }

    @Override
    @Transactional
    public TypeOper updateTypeOper(TypeOper typeOper) {
        return repository.save(typeOper);
    }
}