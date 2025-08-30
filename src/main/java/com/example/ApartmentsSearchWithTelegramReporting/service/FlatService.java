package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import java.util.Map;
import java.util.List;

public interface FlatService {

    Map<Long, Flat> getAllFlats();

    List<String> getLastFlats(int limit);

    void replaceFlats(Map<Long, Flat> newFlatsWithDetails);

}
