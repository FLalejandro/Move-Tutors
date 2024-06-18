package TutorMoves.guis;

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
import java.util.Set;

public class SelectionScreen {

    /**
     * Opens the selection menu GUI for the player.
     *
     * @param player The player to open the GUI for.
     */
    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Select a Pokémon"));

        try {
            PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());

            // Specific middle row indexes
            int[] middleRowIndexes = {10, 11, 12, 14, 15, 16};

            for (int i = 0; i < 6; i++) {
                int slotIndex = middleRowIndexes[i];
                Pokemon pokemon = partyStore.get(i);

                final int slotNumber = i + 1;

                if (pokemon != null) {
                    ItemStack pokemonItem = createPokemonItem(pokemon);
                    gui.setSlot(slotIndex, GuiElementBuilder.from(pokemonItem)
                            .setCallback((x, y, z) -> {
                                try {
                                    AllTutorScreen.open(player, slotNumber);
                                } catch (NoPokemonStoreException e) {
                                    player.sendMessage(Text.literal("No Pokémon found in slot " + slotNumber).formatted(Formatting.RED));
                                }
                            }));
                } else {
                    ItemStack emptyItem = new ItemStack(Registries.ITEM.get(new Identifier("cobblemon:poke_ball")));
                    emptyItem.setCustomName(Text.literal("Empty Slot"));
                    gui.setSlot(slotIndex, GuiElementBuilder.from(emptyItem));
                }
            }

        } catch (NoPokemonStoreException e) {
            player.sendMessage(Text.literal("Error retrieving Pokémon party").formatted(Formatting.RED));
        }

        // Add filler items to all other slots
        ItemStack fillerItem = new ItemStack(Registries.ITEM.get(new Identifier("minecraft:cyan_stained_glass_pane")));
        fillerItem.setCustomName(Text.literal(""));
        for (int i = 0; i < 27; i++) {
            if (gui.getSlot(i) == null) {
                gui.setSlot(i, GuiElementBuilder.from(fillerItem));
            }
        }

        gui.open();
    }

    /**
     * Creates an ItemStack representing the Pokémon with the appropriate NBT data.
     *
     * @param pokemon The Pokémon to represent.
     * @return The ItemStack representing the Pokémon.
     */
    private static ItemStack createPokemonItem(Pokemon pokemon) {
        Species species = pokemon.getSpecies();
        ItemStack itemStack = new ItemStack(Registries.ITEM.get(new Identifier("cobblemon:pokemon_model")));
        NbtCompound nbt = new NbtCompound();
        nbt.putString("species", "cobblemon:" + species.getName().toLowerCase());

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
