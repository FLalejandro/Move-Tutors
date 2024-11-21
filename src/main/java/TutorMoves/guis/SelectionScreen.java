package TutorMoves.guis;

import TutorMoves.util.PokemonUtil;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SelectionScreen {

    /**
     * Opens the selection menu GUI for the player.
     * @param player The player to open the GUI for.
     * @param specificTutorName Optional tutor name for specific tutors.
     */
    public static void open(ServerPlayerEntity player, Optional<String> specificTutorName) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Select a Pokémon"));

        // get party
        PlayerPartyStore partyStore;
        partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

        int[] middleRowIndexes = {10, 11, 12, 14, 15, 16};

        // Iterate over the first 6 party slots to display the player's Pokémon
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
                            AllTutorScreen.open(player, slotNumber);
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
                gui.setSlot(slotIndex, GuiElementBuilder.from(emptyItem));
            }
        }

        // Add a filler item for any remaining slots
        ItemStack fillerItem = new ItemStack(Registries.ITEM.get(Identifier.of("minecraft:cyan_stained_glass_pane")));
        fillerItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal(""));
        for (int i = 0; i < 27; i++) {
            if (gui.getSlot(i) == null) {
                gui.setSlot(i, GuiElementBuilder.from(fillerItem));
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

        // Convert the Set<String> of aspects to a String array, as required by the method
        String[] aspectsArray = aspects.toArray(new String[0]);

        // Use the built-in method with the species and converted aspects array
        ItemStack pokemonItemStack = PokemonItem.from(species, aspectsArray);

        // Return the generated ItemStack
        return pokemonItemStack;
    }

}
