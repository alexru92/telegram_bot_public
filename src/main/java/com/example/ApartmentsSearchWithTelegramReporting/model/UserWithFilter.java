package com.example.ApartmentsSearchWithTelegramReporting.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class UserWithFilter {

    private final String firstName;
    private final String lastName;
    private final String username;
    @Setter
    private int minPrice;
    @Setter
    private int maxPrice;
    @Setter
    private int minRooms;
    @Setter
    private int maxRooms;
    @Setter
    private List<String> exceptionsStrings;

    public UserWithFilter(String firstName, String lastName, String username) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.minPrice = 0;
        this.maxPrice = 100000;
        this.minRooms = 0;
        this.maxRooms = 100;
        this.exceptionsStrings = new ArrayList<>();
    }
}
