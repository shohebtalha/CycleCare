package com.cyclecare.service;

import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.SymptomType;
import com.cyclecare.domain.User;
import com.cyclecare.dto.SymptomDto;
import com.cyclecare.repository.LabelCount;
import com.cyclecare.repository.SymptomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SymptomService {

    private final SymptomRepository symptomRepository;

    public SymptomService(SymptomRepository symptomRepository) {
        this.symptomRepository = symptomRepository;
    }

    @Transactional
    public Symptom save(User user, SymptomDto dto) {
        Symptom symptom = new Symptom();
        symptom.setUser(user);
        symptom.setEntryDate(dto.getEntryDate());
        symptom.setType(dto.getType());
        symptom.setCustomSymptom(dto.getCustomSymptom());
        symptom.setSeverity(dto.getSeverity());
        symptom.setNotes(dto.getNotes());
        return symptomRepository.save(symptom);
    }

    @Transactional(readOnly = true)
    public List<Symptom> latest(User user) {
        return symptomRepository.findTop8ByUserOrderByEntryDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Symptom> history(User user) {
        return symptomRepository.findByUserOrderByEntryDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Symptom> between(User user, LocalDate start, LocalDate end) {
        return symptomRepository.findByUserAndEntryDateBetweenOrderByEntryDateDesc(user, start, end);
    }

    @Transactional(readOnly = true)
    public long severeCountSince(User user, LocalDate after) {
        return symptomRepository.countByUserAndSeverityGreaterThanEqualAndEntryDateAfter(user, 4, after);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> frequency(User user) {
        return symptomRepository.countBySymptomLabel(user).stream()
                .collect(Collectors.toMap(
                        count -> symptomLabel(count.getLabel()),
                        LabelCount::getCount,
                        (left, right) -> left,
                        LinkedHashMap::new))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (left, right) -> left, LinkedHashMap::new));
    }

    @Transactional
    public void deleteAll(User user) {
        symptomRepository.deleteByUser(user);
    }

    @Transactional
    public void delete(User user, Long id) {
        symptomRepository.findByIdAndUser(id, user).ifPresent(symptomRepository::delete);
    }

    private String symptomLabel(Symptom symptom) {
        if (symptom.getType() == SymptomType.OTHER && symptom.getCustomSymptom() != null
                && !symptom.getCustomSymptom().isBlank()) {
            return symptom.getCustomSymptom();
        }
        return symptom.getType().getLabel();
    }

    private String symptomLabel(String label) {
        try {
            return SymptomType.valueOf(label).getLabel();
        } catch (IllegalArgumentException ex) {
            return label;
        }
    }
}
