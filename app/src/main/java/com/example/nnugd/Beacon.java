package com.example.nnugd;

public class Beacon {
    public String uuid;
    public String plate_id;
    public String tile_code;
    public String level_code;
    public String name;
    public boolean searchable;
    public String speakout;
    public String room_id;
    public String room_name;
    public String building_id;
    public String building_name;
    public long type;
    public long m_power;
    public double latitude;
    public double longitude;
    public boolean indoor;
    public String postal_code;

    public static String formatUUID(String uuid) {
        String formated = String.format("%s-%s-%s-%s-%s",
                uuid.substring(0, 8),
                uuid.substring(8, 12),
                uuid.substring(12, 16),
                uuid.substring(16, 20),
                uuid.substring(20, 32));
        return formated.toUpperCase();
    }
}
