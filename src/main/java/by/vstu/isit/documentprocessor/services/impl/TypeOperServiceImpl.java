package by.vstu.isit.documentprocessor.services.impl;

import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.repositories.TypeOperRepository;
import by.vstu.isit.documentprocessor.services.interfaces.TypeOperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
//@Service
//@RequiredArgsConstructor
public class TypeOperServiceImpl /*implements TypeOperService*/ {

//    private final TypeOperRepository typeOperRepository;
//
//    @Override
//    @Transactional
//    public TypeOper saveTypeOper(TypeOper typeOper) {
//        return typeOperRepository.save(typeOper);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<TypeOper> findAllTypeOpers() {
//        return typeOperRepository.findAll();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Optional<TypeOper> findTypeOperById(Long id) {
//        return typeOperRepository.findById(id);
//    }
//
//    @Override
//    @Transactional
//    public TypeOper updateTypeOper(TypeOper typeOper) {
//        return typeOperRepository.save(typeOper);
//    }
//
//    @Override
//    @Transactional
//    public void deleteTypeOper(Long id) {
//        typeOperRepository.deleteById(id);
//    }
}