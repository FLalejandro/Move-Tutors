package me.novoro.TutorMoves.util;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveUtil {

    private static final Map<String, String> TYPE_GEM_MAP = new HashMap<>();
    private static final Map<String, Integer> TYPE_COLOR_MAP = new HashMap<>();

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

        TYPE_COLOR_MAP.put("BUG", 0xA6B91A);
        TYPE_COLOR_MAP.put("DARK", 0x705746);
        TYPE_COLOR_MAP.put("DRAGON", 0x6F35FC);
        TYPE_COLOR_MAP.put("ELECTRIC", 0xF7D02C);
        TYPE_COLOR_MAP.put("FAIRY", 0xD685AD);
        TYPE_COLOR_MAP.put("FIGHTING", 0xC22E28);
        TYPE_COLOR_MAP.put("FIRE", 0xEE8130);
        TYPE_COLOR_MAP.put("FLYING", 0xA98FF3);
        TYPE_COLOR_MAP.put("GHOST", 0x735797);
        TYPE_COLOR_MAP.put("GRASS", 0x7AC74C);
        TYPE_COLOR_MAP.put("GROUND", 0xE2BF65);
        TYPE_COLOR_MAP.put("ICE", 0x96D9D6);
        TYPE_COLOR_MAP.put("NORMAL", 0xA8A77A);
        TYPE_COLOR_MAP.put("POISON", 0xA33EA1);
        TYPE_COLOR_MAP.put("PSYCHIC", 0xF95587);
        TYPE_COLOR_MAP.put("ROCK", 0xB6A136);
        TYPE_COLOR_MAP.put("STEEL", 0xB7B7CE);
        TYPE_COLOR_MAP.put("WATER", 0x6390F0);
    }

    private final Moves moves;

    public MoveUtil(Moves moves) {
        this.moves = moves;
    }

    public GuiElementBuilder getGemForMove(MoveTemplate move, BigDecimal price, String currencyKey) {
        String gemId = TYPE_GEM_MAP.getOrDefault(move.getElementalType().getName().toUpperCase(), "cobblemon:normal_gem");
        ItemStack itemStack = new ItemStack(Registries.ITEM.get(Identifier.of(gemId)));

        // Get the color for the type
        String type = capitalize(move.getElementalType().getName());
        Integer typeColor = TYPE_COLOR_MAP.getOrDefault(move.getElementalType().getName().toUpperCase(), 0xA8A77A);

        // Format power and accuracy
        int power = (int) move.getPower();
        int accuracy = (int) move.getAccuracy();

        // Collect all lore entries
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Type: ").styled(style -> style.withColor(TextColor.fromRgb(0xADD8E6)).withItalic(false))
                .append(Text.literal(type).styled(style -> style.withColor(TextColor.fromRgb(typeColor)).withItalic(false))));
        lore.add(Text.literal("Category: " + capitalize(move.getDamageCategory().getName())).styled(style -> style.withColor(TextColor.fromRgb(0xADD8E6)).withItalic(false)));
        lore.add(Text.literal("Power: " + power).styled(style -> style.withColor(TextColor.fromRgb(0xADD8E6)).withItalic(false)));
        lore.add(Text.literal("Accuracy: " + accuracy + "%").styled(style -> style.withColor(TextColor.fromRgb(0xADD8E6)).withItalic(false)));

        // Split description for better readability and add to lore
        List<String> descriptionLines = splitDescription(move.getDescription().getString(), 40);
        descriptionLines.forEach(line -> lore.add(Text.literal(line).styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(false))));

        // Add price information based on currency type
        lore.add(Text.literal(""));
        lore.add(Text.literal(""));
        if (currencyKey.startsWith("ITEMS:")) {
            String itemId = currencyKey.substring("ITEMS:".length());
            String itemName = Registries.ITEM.get(Identifier.of(itemId)).getName().getString();
            String displayName = capitalize(itemName.replace("_", " "));
            lore.add(Text.literal("§6§lPrice§f: " + price.intValue() + " " + displayName));
        } else {
            lore.add(Text.literal("§6§lPrice:§f $" + price));
        }

        // Set the name and lore without colors
        GuiElementBuilder itemBuilder = GuiElementBuilder.from(itemStack)
                .setName(Text.literal(move.getDisplayName().getString()).styled(style -> style.withColor(TextColor.fromRgb(typeColor)).withBold(true)))
                .setLore(lore);

        return itemBuilder;
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
}

