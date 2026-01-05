package by.vstu.isit.documentprocessor.services.impl;

import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.repositories.OperRepository;
import by.vstu.isit.documentprocessor.services.interfaces.OperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
//@Service
//@RequiredArgsConstructor
public class OperServiceImpl /*implements OperService*/ {

//    private final OperRepository operRepository;
//
//    @Override
//    @Transactional
//    public Oper saveOper(Oper oper) {
//        return operRepository.save(oper);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<Oper> findAllOpers() {
//        return operRepository.findAll();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Optional<Oper> findOperById(Long id) {
//        return operRepository.findById(id);
//    }
//
//    @Override
//    @Transactional
//    public Oper updateOper(Oper oper) {
//        return operRepository.save(oper);
//    }
//
//    @Override
//    @Transactional
//    public void deleteOper(Long id) {
//        operRepository.deleteById(id);
//    }
}