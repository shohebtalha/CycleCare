package com.cyclecare.service;

import com.cyclecare.domain.JournalEntry;
import com.cyclecare.domain.User;
import com.cyclecare.dto.JournalEntryDto;
import com.cyclecare.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class JournalService {

    private final JournalEntryRepository journalEntryRepository;

    public JournalService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Transactional
    public JournalEntry save(User user, JournalEntryDto dto) {
        JournalEntry entry = new JournalEntry();
        entry.setUser(user);
        entry.setEntryDate(dto.getEntryDate());
        entry.setPhysicalSymptoms(dto.getPhysicalSymptoms());
        entry.setEmotionalState(dto.getEmotionalState());
        entry.setObservations(dto.getObservations());
        return journalEntryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> latest(User user) {
        return journalEntryRepository.findTop10ByUserOrderByEntryDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> history(User user) {
        return journalEntryRepository.findByUserOrderByEntryDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> between(User user, LocalDate start, LocalDate end) {
        return journalEntryRepository.findByUserAndEntryDateBetweenOrderByEntryDateDesc(user, start, end);
    }
}
