package by.vstu.isit.documentprocessor.services.impl;

import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.repositories.FuncRepository;
import by.vstu.isit.documentprocessor.services.interfaces.FuncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
//@Service
//@RequiredArgsConstructor
public class FuncServiceImpl /*implements FuncService*/ {

//    private final FuncRepository funcRepository;
//
//    @Override
//    @Transactional
//    public Func saveFunc(Func func) {
//        return funcRepository.save(func);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<Func> findAllFuncs() {
//        return funcRepository.findAll();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Optional<Func> findFuncById(Long id) {
//        return funcRepository.findById(id);
//    }
//
//    @Override
//    @Transactional
//    public Func updateFunc(Func func) {
//        return funcRepository.save(func);
//    }
//
//    @Override
//    @Transactional
//    public void deleteFunc(Long id) {
//        funcRepository.deleteById(id);
//    }
}