package TutorMoves.util.ribStuff;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static net.minecraft.item.Items.*;

public class GuiUtils {

    public static void fillGUI(SimpleGui gui) {
        int freeslot = gui.getFirstEmptySlot();
        while (freeslot != -1) {
            gui.setSlot(freeslot, GuiElementBuilder.from(BLACK_STAINED_GLASS_PANE.getDefaultStack().setCustomName(Text.literal(""))));
            freeslot = gui.getFirstEmptySlot();
        }
    }

    public static void closeGUIs(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false);
        gui.open();
        gui.close();
    }

    @Deprecated(forRemoval = true)
    public static ItemStack getPokemonGuiElement(Pokemon pokemon) {
        String moveOne = pokemon.getMoveSet().getMoves().size() >= 1 ? pokemon.getMoveSet().get(0).getDisplayName().getString() : "None";
        String moveTwo = pokemon.getMoveSet().getMoves().size() >= 2 ? pokemon.getMoveSet().get(1).getDisplayName().getString() : "None";
        String moveThree = pokemon.getMoveSet().getMoves().size() >= 3 ? pokemon.getMoveSet().get(2).getDisplayName().getString() : "None";
        String moveFour = pokemon.getMoveSet().getMoves().size() >= 4 ? pokemon.getMoveSet().get(3).getDisplayName().getString() : "None";

        // defualt male
        String genderIndicator = "♂";
        switch (pokemon.getGender()) {
            case GENDERLESS -> genderIndicator = "⚲";
            case FEMALE -> genderIndicator = "♀";
        }

        return new ItemBuilder(PokemonItem.from(pokemon, 1))
                .hideAdditional()
                .addLore(List.of(
                        Text.literal("Item: ").formatted(Formatting.DARK_AQUA).append(!pokemon.heldItem().isOf(Items.AIR) ? Text.literal("").append(pokemon.heldItem().getName()).formatted(Formatting.WHITE) : Text.literal("None").formatted(Formatting.WHITE)),
                        Text.literal("Nature: ").formatted(Formatting.LIGHT_PURPLE).append(LocalizationUtilsKt.lang(pokemon.getNature().getDisplayName().replace("cobblemon.", "")).formatted(Formatting.WHITE)),
                        Text.literal("Ability: ").formatted(Formatting.DARK_AQUA).append(LocalizationUtilsKt.lang(pokemon.getAbility().getDisplayName().replace("cobblemon.", "")).formatted(Formatting.WHITE)),
                        Text.literal("IVs: ").formatted(Formatting.LIGHT_PURPLE)
                                .append(Text.literal(pokemon.getIvs().getOrDefault(Stats.HP) + "/").formatted(Formatting.DARK_AQUA))
                                .append(Text.literal(pokemon.getIvs().getOrDefault(Stats.ATTACK) + "/").formatted(Formatting.WHITE))
                                .append(Text.literal(pokemon.getIvs().getOrDefault(Stats.DEFENCE) + "/").formatted(Formatting.DARK_AQUA))
                                .append(Text.literal(pokemon.getIvs().getOrDefault(Stats.SPECIAL_ATTACK) + "/").formatted(Formatting.WHITE))
                                .append(Text.literal(pokemon.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE) + "/").formatted(Formatting.DARK_AQUA))
                                .append(Text.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.SPEED))).formatted(Formatting.WHITE)),
                        Text.literal("EVs: ").formatted(Formatting.DARK_AQUA)
                                .append(Text.literal(pokemon.getEvs().getOrDefault(Stats.HP) + "/").formatted(Formatting.LIGHT_PURPLE))
                                .append(Text.literal(pokemon.getEvs().getOrDefault(Stats.ATTACK) + "/").formatted(Formatting.WHITE))
                                .append(Text.literal((pokemon.getEvs().getOrDefault(Stats.DEFENCE) + "/")).formatted(Formatting.LIGHT_PURPLE))
                                .append(Text.literal(pokemon.getEvs().getOrDefault(Stats.SPECIAL_ATTACK) + "/").formatted(Formatting.WHITE))
                                .append(Text.literal((pokemon.getEvs().getOrDefault(Stats.SPECIAL_DEFENCE) + "/")).formatted(Formatting.LIGHT_PURPLE))
                                .append(Text.literal(String.valueOf((pokemon.getEvs().getOrDefault(Stats.SPEED)))).formatted(Formatting.WHITE)),
                        Text.literal("Moves: ").formatted(Formatting.LIGHT_PURPLE),
                        Text.literal(" + ").append(Text.literal(moveOne).formatted(Formatting.WHITE)),
                        Text.literal(" + ").append(Text.literal(moveTwo).formatted(Formatting.WHITE)),
                        Text.literal(" + ").append(Text.literal(moveThree).formatted(Formatting.WHITE)),
                        Text.literal(" + ").append(Text.literal(moveFour).formatted(Formatting.WHITE))
                ))
                .setCustomName(
                        pokemon.getDisplayName().formatted(Formatting.LIGHT_PURPLE)
                )
                .build();
    }
}
