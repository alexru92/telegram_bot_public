package com.example.ApartmentsSearchWithTelegramReporting.repository;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import java.util.Map;

public interface FlatRepository {

    Map<Long, Flat> readFlats();

    void saveFlat(Long id, Flat flat);

    void emptyFlats();
}
