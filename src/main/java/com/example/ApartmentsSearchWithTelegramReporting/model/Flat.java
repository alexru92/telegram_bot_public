package com.example.ApartmentsSearchWithTelegramReporting.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Flat {

    private double rooms;
    private double size;
    private double price;
    @Setter
    private String address;
    @Setter
    private String link;

    public void setRooms(String rooms) {
        this.rooms = Double.parseDouble(cleanNumber(rooms));
    }

    public void setSize(String size) {
        this.size = Double.parseDouble(cleanNumber(size));
    }

    public void setPrice(String price) {
        this.price = Double.parseDouble(cleanNumber(price));
    }

    public int getIntPrice(){
        return (int) price;
    }

    public int getIntRooms(){
        return (int) rooms;
    }

    public String getMessage() {
        return "Price and Address: " + rooms + " Zimmer, " + size + " m², " + price + " € | " + address +
                "\nDetails URL: " + link;
    }

    public String getShortMessage() {
        return rooms + " r., " + size + "m², " + price + "€" + address + ", " + link;
    }

    private String cleanNumber(String text) {
        if (text == null || text.isEmpty()) return "0";
        String cleaned = text.trim();
        if (cleaned.contains(".") && cleaned.contains(",")) {
            cleaned = cleaned.replace(".", "");
            cleaned = cleaned.replace(",", ".");
        } else if (cleaned.contains(",")) {
            cleaned = cleaned.replace(",", ".");
        }
        cleaned = cleaned.replaceAll("[^0-9.,]", "");
        return cleaned.isEmpty() ? "0" : cleaned;
    }
}
