package by.vstu.isit.documentprocessor.services.impl;

import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import by.vstu.isit.documentprocessor.repositories.TypeOperFuncRepository;
import by.vstu.isit.documentprocessor.services.interfaces.TypeOperFuncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
//@Service
//@RequiredArgsConstructor
public class TypeOperFuncServiceImpl /*implements TypeOperFuncService*/ {

//    private final TypeOperFuncRepository typeOperFuncRepository;
//
//    @Override
//    @Transactional
//    public TypeOperFunc saveTypeOperFunc(TypeOperFunc typeOperFunc) {
//        return typeOperFuncRepository.save(typeOperFunc);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<TypeOperFunc> findAllTypeOperFuncs() {
//        return typeOperFuncRepository.findAll();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Optional<TypeOperFunc> findTypeOperFuncById(Long id) {
//        return typeOperFuncRepository.findById(id);
//    }
//
//    @Override
//    @Transactional
//    public TypeOperFunc updateTypeOperFunc(TypeOperFunc typeOperFunc) {
//        return typeOperFuncRepository.save(typeOperFunc);
//    }
//
//    @Override
//    @Transactional
//    public void deleteTypeOperFunc(Long id) {
//        typeOperFuncRepository.deleteById(id);
//    }
}