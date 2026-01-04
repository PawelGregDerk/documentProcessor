package by.vstu.isit.documentprocessor.services.impl;

import by.vstu.isit.documentprocessor.entities.SborEd;
import by.vstu.isit.documentprocessor.repositories.SborEdRepository;
import by.vstu.isit.documentprocessor.services.interfaces.SborEdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SborEdServiceImpl implements SborEdService {

    private final SborEdRepository sborEdRepository;

    @Override
    @Transactional
    public SborEd saveSborEd(SborEd sborEd) {
        return sborEdRepository.save(sborEd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SborEd> findAllSborEds() {
        return sborEdRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SborEd> findSborEdById(Long id) {
        return sborEdRepository.findById(id);
    }

    @Override
    @Transactional
    public SborEd updateSborEd(SborEd sborEd) {
        return sborEdRepository.save(sborEd);
    }

    @Override
    @Transactional
    public void deleteSborEd(Long id) {
        sborEdRepository.deleteById(id);
    }
}