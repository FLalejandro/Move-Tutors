package me.novoro.tutormoves.guis;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.novoro.tutormoves.config.ConfigManager;
import me.novoro.tutormoves.utils.ColorUtil;
import me.novoro.tutormoves.utils.GuiUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.Optional;
import java.util.Set;

public class SelectionScreen {

    /**
     * Opens the selection menu GUI for the player.
     * @param player The player to open the GUI for.
     * @param specificTutorName Optional tutor name for specific tutors.
     */
    public static void open(ServerPlayerEntity player, Optional<String> specificTutorName) {
        int rows = 3;
        String guiTitle = ConfigManager.getSelectionGuiTitle();
        SimpleGui gui = GuiUtil.createGui(player, rows, ColorUtil.parseColourToText(guiTitle));

        PlayerPartyStore partyStore;
        partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

        int[] middleRowIndexes = {10, 11, 12, 14, 15, 16};

        for (int i = 0; i < 6; i++) {
            int slotIndex = middleRowIndexes[i];
            Pokemon pokemon = partyStore.get(i);
            final int slotNumber = i + 1;

            if (pokemon != null) {
                ItemStack pokemonItem = createPokemonItem(pokemon);
                GuiElementBuilder element = GuiElementBuilder.from(pokemonItem).setCallback((x, y, z) -> {
                    try {
                        if (specificTutorName.isPresent()) {
                            SpecificTutorScreen.open(player, slotNumber, specificTutorName.get());
                        } else {
                            GeneralTutorScreen.open(player, slotNumber);
                        }
                    } catch (NoPokemonStoreException e) {
                        player.sendMessage(Text.literal("No Pokémon found in slot " + slotNumber).formatted(Formatting.RED));
                    }
                });
                gui.setSlot(slotIndex, element);
            } else {
                // For empty slots, use a default Poké Ball item
                ItemStack emptyItem = new ItemStack(Registries.ITEM.get(Identifier.of("cobblemon:poke_ball")));
                emptyItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Empty Slot"));
                emptyItem.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                gui.setSlot(slotIndex, GuiElementBuilder.from(emptyItem));
            }
        }

        // Add a filler item for any remaining slots
        ConfigManager.getSelectionFillerItem().set(DataComponentTypes.CUSTOM_NAME, Text.literal(""));
        for (int i = 0; i < 27; i++) {
            if (gui.getSlot(i) == null) {
                gui.setSlot(i, GuiElementBuilder.from(ConfigManager.getSelectionFillerItem()));
            }
        }

        // Open the GUI for the player
        gui.open();
    }

    /**
     * Creates an ItemStack representing the Pokémon for display in the GUI.
     * @param pokemon The Pokémon to create the ItemStack for.
     * @return The ItemStack representing the Pokémon.
     */
    private static ItemStack createPokemonItem(Pokemon pokemon) {
        Species species = pokemon.getSpecies();
        Set<String> aspects = pokemon.getAspects();
        String[] aspectsArray = aspects.toArray(new String[0]);
        return PokemonItem.from(species, aspectsArray);
    }
}