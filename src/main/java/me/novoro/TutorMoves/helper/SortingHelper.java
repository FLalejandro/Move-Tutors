package me.novoro.TutorMoves.helper;

import com.cobblemon.mod.common.api.moves.MoveTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SortingHelper {

    private static final List<String> TYPE_ORDER = List.of(
            "BUG", "DARK", "DRAGON", "ELECTRIC", "FAIRY", "FIGHTING", "FIRE",
            "FLYING", "GHOST", "GRASS", "GROUND", "ICE", "NORMAL", "POISON",
            "PSYCHIC", "ROCK", "STEEL", "WATER"
    );

    /**
     * Sorts a list of MoveTemplate objects alphabetically by their display name.
     *
     * @param moves The list of MoveTemplate objects to sort.
     * @return A sorted list of MoveTemplate objects.
     */
    public static List<MoveTemplate> sortAlphabetically(List<MoveTemplate> moves) {
        return moves.stream()
                .sorted(Comparator.comparing(move -> move.getDisplayName().getString()))
                .collect(Collectors.toList());
    }

    /**
     * Sorts a list of MoveTemplate objects by their category and then alphabetically.
     *
     * @param moves The list of MoveTemplate objects to sort.
     * @return A sorted list of MoveTemplate objects.
     */
    public static List<MoveTemplate> sortByCategory(List<MoveTemplate> moves) {
        return moves.stream()
                .sorted(Comparator.comparing((MoveTemplate move) -> move.getDamageCategory().getName())
                        .thenComparing(move -> move.getDisplayName().getString()))
                .collect(Collectors.toList());
    }

    /**
     * Sorts a list of MoveTemplate objects by their type and then alphabetically.
     *
     * @param moves The list of MoveTemplate objects to sort.
     * @return A sorted list of MoveTemplate objects.
     */
    public static List<MoveTemplate> sortByType(List<MoveTemplate> moves) {
        return moves.stream()
                .sorted(Comparator.comparing((MoveTemplate move) -> TYPE_ORDER.indexOf(move.getElementalType().getName().toUpperCase()))
                        .thenComparing(move -> move.getDisplayName().getString()))
                .collect(Collectors.toList());
    }

    /**
     * Sorts a list of MoveTemplate objects based on the provided sort option.
     *
     * @param moves The list of MoveTemplate objects to sort.
     * @param sortOption The sort option to use for sorting.
     * @return A sorted list of MoveTemplate objects.
     */
    public static List<MoveTemplate> sortByOption(List<MoveTemplate> moves, SortOption sortOption) {
        switch (sortOption) {
            case CATEGORY:
                return sortByCategory(moves);
            case TYPE:
                return sortByType(moves);
            case ALPHABETICAL:
            default:
                return sortAlphabetically(moves);
        }
    }

    public enum SortOption {
        ALPHABETICAL,
        CATEGORY,
        TYPE
    }
}
