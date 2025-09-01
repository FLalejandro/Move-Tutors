package me.novoro.TutorMoves.config.versions;

import me.novoro.TutorMoves.config.Configuration;

import java.util.List;

public class Ver110 {
    public void update(Configuration config, String fileName) {
        // Implement the update logic from 1.0.0 to 1.1.0 here
        if (fileName.equals("lang.yml")) {
            String errorNoPokemon = config.getString("Error-No-Pokemon");
            if (errorNoPokemon != null) {
                errorNoPokemon = errorNoPokemon.replace("<dark_red>{slot}", "<dark_red>{slot}<");
                config.set("Error-No-Pokemon", errorNoPokemon);
            }
        }
        if (fileName.startsWith("tutors/")) {
            List<String> blacklistedPokemon = config.getStringList("SpecificTutor.Blacklisted-Pokemon");
            if (blacklistedPokemon.contains("dragapult")) {
                blacklistedPokemon.remove("dragapult");
                blacklistedPokemon.add("jirachi");
                config.set("SpecificTutor.Blacklisted-Pokemon", blacklistedPokemon);
            }
        }
        config.set("Config-Version", "1.1.0");
    }
}
