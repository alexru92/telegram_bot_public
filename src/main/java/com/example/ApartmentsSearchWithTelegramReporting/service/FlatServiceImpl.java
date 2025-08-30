package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.repository.FlatRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlatServiceImpl implements FlatService {

    FlatRepository flatRepository;

    private Map<Long, Flat> flatsWithDetails = new HashMap<>();

    public FlatServiceImpl(FlatRepository flatRepository) {
        this.flatRepository = flatRepository;
        flatsWithDetails = flatRepository.readFlats();
    }

    @Override
    public Map<Long, Flat> getAllFlats() {
        return flatsWithDetails;
    }

    @Override
    public List<String> getLastFlats(int limit) {
        List<Long> lastIds = flatsWithDetails.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .limit(limit)
                .toList();
        return lastIds.stream()
                .map(flatsWithDetails::get)
                .map(Flat::getMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void replaceFlats(Map<Long, Flat> newFlatsWithDetails) {
        flatsWithDetails = newFlatsWithDetails;

        flatRepository.emptyFlats();
        for (Long flatId : newFlatsWithDetails.keySet()) {
            flatRepository.saveFlat(flatId, newFlatsWithDetails.get(flatId));
        }
    }
}
