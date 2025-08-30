package com.example.ApartmentsSearchWithTelegramReporting.repository;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.ApartmentsSearchWithTelegramReporting.utils.FileUtils.*;

@Log4j2
@Repository
public class FlatRepositoryImpl implements FlatRepository {

    private static String FLATS_FILE = "apartments.txt";

    @Override
    public Map<Long, Flat> readFlats() {
        Map<Long, Flat> flatsWithDetails = new HashMap<>();
        Set<String> lines = readFromFile(FLATS_FILE, reader -> reader.lines().collect(Collectors.toSet()));
        if (lines != null && !lines.isEmpty()) {
            for (String line : lines) {
                String[] s = line.split(";");
                if (s.length == 6) {
                    Flat flat = new Flat();

                    flat.setRooms(s[1]);
                    flat.setSize(s[2]);
                    flat.setPrice(s[3]);
                    flat.setAddress(s[4]);
                    flat.setLink(s[5]);

                    flatsWithDetails.put(Long.valueOf(s[0]), flat);
                } else log.error("Wrong row format. Cannot read: {}", line);
            }
        }
        return flatsWithDetails;
    }

    @Override
    public void saveFlat(Long id, Flat flat) {
        String s = String.join(";",
                id.toString(),
                String.valueOf(flat.getRooms()),
                String.valueOf(flat.getSize()),
                String.valueOf(flat.getPrice()),
                flat.getAddress(),
                flat.getLink()
        );
        writeToFile(FLATS_FILE, s, true);
    }

    public void emptyFlats() {
        emptyFile(FLATS_FILE);
    }
}
