package TutorMoves.util;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.types.ElementalType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import dev.roanoke.rib.utils.ItemBuilder;
import dev.roanoke.rib.utils.LoreLike;

import java.util.*;
import java.util.stream.Collectors;

public class MoveUtil {

    private static final Map<String, String> TYPE_GEM_MAP = new HashMap<>();
    private static final Map<String, String> TYPE_COLOR_MAP = new HashMap<>();

    static {
        TYPE_GEM_MAP.put("BUG", "cobblemon:bug_gem");
        TYPE_GEM_MAP.put("DARK", "cobblemon:dark_gem");
        TYPE_GEM_MAP.put("DRAGON", "cobblemon:dragon_gem");
        TYPE_GEM_MAP.put("ELECTRIC", "cobblemon:electric_gem");
        TYPE_GEM_MAP.put("FAIRY", "cobblemon:fairy_gem");
        TYPE_GEM_MAP.put("FIGHTING", "cobblemon:fighting_gem");
        TYPE_GEM_MAP.put("FIRE", "cobblemon:fire_gem");
        TYPE_GEM_MAP.put("FLYING", "cobblemon:flying_gem");
        TYPE_GEM_MAP.put("GHOST", "cobblemon:ghost_gem");
        TYPE_GEM_MAP.put("GRASS", "cobblemon:grass_gem");
        TYPE_GEM_MAP.put("GROUND", "cobblemon:ground_gem");
        TYPE_GEM_MAP.put("ICE", "cobblemon:ice_gem");
        TYPE_GEM_MAP.put("NORMAL", "cobblemon:normal_gem");
        TYPE_GEM_MAP.put("POISON", "cobblemon:poison_gem");
        TYPE_GEM_MAP.put("PSYCHIC", "cobblemon:psychic_gem");
        TYPE_GEM_MAP.put("ROCK", "cobblemon:rock_gem");
        TYPE_GEM_MAP.put("STEEL", "cobblemon:steel_gem");
        TYPE_GEM_MAP.put("WATER", "cobblemon:water_gem");

        TYPE_COLOR_MAP.put("BUG", "#A6B91A");
        TYPE_COLOR_MAP.put("DARK", "#705746");
        TYPE_COLOR_MAP.put("DRAGON", "#6F35FC");
        TYPE_COLOR_MAP.put("ELECTRIC", "#F7D02C");
        TYPE_COLOR_MAP.put("FAIRY", "#D685AD");
        TYPE_COLOR_MAP.put("FIGHTING", "#C22E28");
        TYPE_COLOR_MAP.put("FIRE", "#EE8130");
        TYPE_COLOR_MAP.put("FLYING", "#A98FF3");
        TYPE_COLOR_MAP.put("GHOST", "#735797");
        TYPE_COLOR_MAP.put("GRASS", "#7AC74C");
        TYPE_COLOR_MAP.put("GROUND", "#E2BF65");
        TYPE_COLOR_MAP.put("ICE", "#96D9D6");
        TYPE_COLOR_MAP.put("NORMAL", "#A8A77A");
        TYPE_COLOR_MAP.put("POISON", "#A33EA1");
        TYPE_COLOR_MAP.put("PSYCHIC", "#F95587");
        TYPE_COLOR_MAP.put("ROCK", "#B6A136");
        TYPE_COLOR_MAP.put("STEEL", "#B7B7CE");
        TYPE_COLOR_MAP.put("WATER", "#6390F0");
    }

    private final Moves moves;

    public MoveUtil(Moves moves) {
        this.moves = moves;
    }

    public ItemStack getGemForMove(MoveTemplate move) {
        String gemId = TYPE_GEM_MAP.getOrDefault(move.getElementalType().getName().toUpperCase(), "cobblemon:normal_gem");
        ItemStack itemStack = new ItemStack(Registries.ITEM.get(new Identifier(gemId)));

        // Get the color for the type
        String type = capitalize(move.getElementalType().getName());
        String typeColor = TYPE_COLOR_MAP.getOrDefault(move.getElementalType().getName().toUpperCase(), "#A8A77A");

        // Format power and accuracy
        int power = (int) move.getPower();
        int accuracy = (int) move.getAccuracy();

        // Collect all lore entries
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Type: ").append(Text.literal(type).styled(style -> style.withColor(TextColor.parse(typeColor)))));
        lore.add(Text.literal("Category: " + capitalize(move.getDamageCategory().getName())));
        lore.add(Text.literal("Power: " + power));
        lore.add(Text.literal("Accuracy: " + accuracy + "%"));

        // Split description for better readability and add to lore
        List<String> descriptionLines = splitDescription(move.getDescription().getString(), 40);
        descriptionLines.forEach(line -> lore.add(Text.literal(line)));

        // Set the custom name and lore using ItemBuilder
        ItemBuilder itemBuilder = new ItemBuilder(itemStack)
                .setCustomName(Text.literal(move.getDisplayName().getString()).styled(style -> style.withColor(TextColor.parse(typeColor)).withBold(true)))
                .addLore(new LoreLike(lore));

        return itemBuilder.build();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private List<String> splitDescription(String description, int maxLength) {
        List<String> lines = new ArrayList<>();
        while (description.length() > maxLength) {
            int breakPoint = description.lastIndexOf(' ', maxLength);
            if (breakPoint == -1) {
                breakPoint = maxLength;
            }
            lines.add(description.substring(0, breakPoint));
            description = description.substring(breakPoint).trim();
        }
        lines.add(description);
        return lines;
    }

    public ElementalType getMoveType(String moveName) {
        MoveTemplate template = moves.getByNameOrDummy(moveName);
        return template.getElementalType();
    }
}
