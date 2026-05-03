package me.novoro.tutormoves.config;

import me.novoro.tutormoves.api.configuration.Configuration;

public record MoveOptions(
        boolean tutorMoves,
        boolean eggMoves,
        boolean tmMoves,
        boolean evolutionMoves,
        boolean levelUpMoves,
        boolean formChangeMoves,
        boolean legacyMoves,
        boolean specialMoves
) {

    public static MoveOptions fromGlobal() {
        return new MoveOptions(
                ConfigManager.isTutorMovesEnabled(),
                ConfigManager.isEggMovesEnabled(),
                ConfigManager.isTmMovesEnabled(),
                ConfigManager.isEvolutionMoves(),
                ConfigManager.isLevelUpMoves(),
                ConfigManager.isFormChangeMoves(),
                ConfigManager.isLegacyMoves(),
                ConfigManager.isSpecialMoves()
        );
    }

    public static MoveOptions fromConfig(Configuration section) {
        return new MoveOptions(
                section.getBoolean("tutorMoves"),
                section.getBoolean("eggMoves"),
                section.getBoolean("tmMoves"),
                section.getBoolean("evolutionMoves"),
                section.getBoolean("levelUpMoves"),
                section.getBoolean("formChangeMoves"),
                section.getBoolean("legacyMoves"),
                section.getBoolean("specialMoves")
        );
    }
}
