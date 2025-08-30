package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.provider.AbstractFlatProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class UrlMonitorService {

    private final FlatServiceImpl flatServiceImpl;
    private final List<AbstractFlatProvider> providers;
    private final RestService restService;

    private static final long FIXED_RATE_MS = 210000;
    private final FilterService filterService;
    private volatile long lastExecutionTime = 0;

    @Autowired
    public UrlMonitorService(
            FlatServiceImpl flatServiceImpl,
            List<AbstractFlatProvider> providers,
            RestService restService,
            FilterService filterService) {
        this.flatServiceImpl = flatServiceImpl;
        this.providers = providers;
        this.restService = restService;
        this.filterService = filterService;
    }

    @Scheduled(cron = "*/30 * 6-19 * * MON-SAT")
    public void monitorUrl() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastExecutionTime < FIXED_RATE_MS) {
            return;
        }
        lastExecutionTime = currentTime;

        Map<Long, Flat> newFlatsWithDetails = new HashMap<>();
        restService.clearCache();

        for (AbstractFlatProvider provider : providers) {
            newFlatsWithDetails.putAll(provider.fetchFlats());

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sleep interrupted between providers", e);
            }
        }

        Map<Long, Flat> knownFlats = flatServiceImpl.getAllFlats();
        Map<Long, Flat> trulyNewFlats = newFlatsWithDetails.entrySet().stream()
                .filter(entry -> !knownFlats.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!trulyNewFlats.isEmpty()) {
            trulyNewFlats.forEach((id, flat) -> {
                filterService.sendMessageToAll(flat);
                log.info("id:{}; {}", id, flat.getShortMessage());
            });
            flatServiceImpl.replaceFlats(newFlatsWithDetails);
        } else {
            log.info("Nothing changed");
        }
    }
}