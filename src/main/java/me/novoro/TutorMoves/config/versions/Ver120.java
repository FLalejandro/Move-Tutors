package me.novoro.TutorMoves.config.versions;

import me.novoro.TutorMoves.config.Configuration;

public class Ver120 {

    public void update(Configuration config, String fileName) {
        // Update config.yml
        if (fileName.equals("config.yml")) {
            updateOrCreateSection(config, "me.novoro.TutorMoves.GUI.title", "<red><bold>General Tutor");
            updateOrCreateSection(config, "me.novoro.TutorMoves.GUI.size", 6);
            updateOrCreateSection(config, "me.novoro.TutorMoves.GUI.filler-item", "minecraft:gray_stained_glass_pane");
        }

        // Update lang.yml
        if (fileName.equals("lang.yml")) {
            config.set("Success-Learned", "<dark_aqua>{pokemon} <aqua>was taught <dark_aqua>{move}!");
            config.set("Insufficient-Items", "<red>You must have <dark_red>{itemcost} <red>to purchase this move.");
            config.set("Successful-Tutor", null); // Remove this key
        }

        // Update specific tutor configs
        if (fileName.startsWith("tutors/")) {
            config.set("SpecificTutor.currencyKey", "ITEMS:minecraft:diamond");
            updateOrCreateSection(config, "SpecificTutor.GUI.title", "<blue><bold>Dragon Master");
            updateOrCreateSection(config, "SpecificTutor.GUI.size", 6);
            updateOrCreateSection(config, "SpecificTutor.GUI.filler-item", "minecraft:gray_stained_glass_pane");
        }

        // Update version
        config.set("Config-Version", "1.2.0");
    }

    private void updateOrCreateSection(Configuration config, String key, Object value) {
        if (!config.contains(key)) {
            config.set(key, value);
        }
    }
}
