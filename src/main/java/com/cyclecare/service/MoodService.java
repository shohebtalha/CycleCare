package com.cyclecare.service;

import com.cyclecare.domain.Mood;
import com.cyclecare.domain.User;
import com.cyclecare.dto.MoodDto;
import com.cyclecare.repository.MoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MoodService {

    private final MoodRepository moodRepository;

    public MoodService(MoodRepository moodRepository) {
        this.moodRepository = moodRepository;
    }

    @Transactional
    public Mood save(User user, MoodDto dto) {
        Mood mood = new Mood();
        mood.setUser(user);
        mood.setEntryDate(dto.getEntryDate());
        mood.setType(dto.getType());
        mood.setIntensity(dto.getIntensity());
        mood.setNotes(dto.getNotes());
        return moodRepository.save(mood);
    }

    @Transactional(readOnly = true)
    public List<Mood> latest(User user) {
        return moodRepository.findTop8ByUserOrderByEntryDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Mood> history(User user) {
        return moodRepository.findByUserOrderByEntryDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Mood> between(User user, LocalDate start, LocalDate end) {
        return moodRepository.findByUserAndEntryDateBetweenOrderByEntryDateDesc(user, start, end);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> distribution(User user) {
        return history(user).stream()
                .collect(Collectors.groupingBy(mood -> mood.getType().getLabel(), LinkedHashMap::new, Collectors.counting()));
    }
}
