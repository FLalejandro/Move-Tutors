package TutorMoves.guis;

import TutorMoves.util.PokemonUtil;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
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

        try {
            PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());
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
                                AllTutorScreen.open(player, slotNumber);
                            }
                        } catch (NoPokemonStoreException e) {
                            player.sendMessage(Text.literal("No Pokémon found in slot " + slotNumber).formatted(Formatting.RED));
                        }
                    });
                    gui.setSlot(slotIndex, element);
                } else {
                    ItemStack emptyItem = new ItemStack(Registries.ITEM.get(new Identifier("cobblemon:poke_ball")));
                    emptyItem.setCustomName(Text.literal("Empty Slot"));
                    gui.setSlot(slotIndex, GuiElementBuilder.from(emptyItem));
                }
            }

        } catch (NoPokemonStoreException e) {
            player.sendMessage(Text.literal("Error retrieving Pokémon party").formatted(Formatting.RED));
        }

        ItemStack fillerItem = new ItemStack(Registries.ITEM.get(new Identifier("minecraft:cyan_stained_glass_pane")));
        fillerItem.setCustomName(Text.literal(""));
        for (int i = 0; i < 27; i++) {
            if (gui.getSlot(i) == null) {
                gui.setSlot(i, GuiElementBuilder.from(fillerItem));
            }
        }

        gui.open();
    }


    private static ItemStack createPokemonItem(Pokemon pokemon) {
        Species species = pokemon.getSpecies();
        String cleanedSpeciesName = PokemonUtil.makeAlphabetic(species.getName().toLowerCase());
        ItemStack itemStack = new ItemStack(Registries.ITEM.get(new Identifier("cobblemon:pokemon_model")));
        NbtCompound nbt = new NbtCompound();
        nbt.putString("species", "cobblemon:" + cleanedSpeciesName);

        // Add aspects
        Set<String> aspectsSet = pokemon.getAspects();
        List<String> aspects = new ArrayList<>(aspectsSet);
        NbtList nbtAspects = new NbtList();
        for (String aspect : aspects) {
            nbtAspects.add(NbtString.of(aspect));
        }
        nbt.put("aspects", nbtAspects);

        itemStack.setNbt(nbt);
        itemStack.setCustomName(Text.literal(species.getName()));
        return itemStack;
    }
}
