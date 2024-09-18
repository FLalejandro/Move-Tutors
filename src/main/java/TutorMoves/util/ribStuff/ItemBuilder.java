package TutorMoves.util.ribStuff;

import com.google.gson.JsonObject;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemBuilder {
    ItemStack stack = null;

    public ItemBuilder(Item item) {
        this.stack = new ItemStack(item);
    }
    public ItemBuilder(ItemStack item) {
        this.stack = item.copy();
    }

    public ItemBuilder(String itemId) {
        Identifier identifier = Identifier.tryParse(itemId);

        this.stack = Registries.ITEM.get(identifier).getDefaultStack();
    }

    public static ItemBuilder fromJson(JsonObject itemData) {
        if (!itemData.has("id")) {
            return null;
        }

        String id = itemData.get("id").getAsString();
        var itemBuilder = new ItemBuilder(id);


        if (itemData.has("customModelData")) {
            itemBuilder.setCustomModelData(itemData.get("customModelData").getAsInt());
        }

        return itemBuilder;
    }

    public String getItemID() {
        return Registries.ITEM.getId(stack.getItem()).toString();
    }

    public boolean matches(ItemStack stack) {

        if (stack.isOf(this.build().getItem())) {
            return new ItemBuilder(stack).getCustomModelData() == this.getCustomModelData();
        }

        return false;
    }

    public int getCustomModelData() {
        NbtCompound tag = stack.getOrCreateNbt();
        return tag.getInt("CustomModelData");
    }

    public ItemBuilder setCustomModelData(int customModelData) {
        NbtCompound tag = stack.getOrCreateNbt();
        tag.putInt("CustomModelData", customModelData);
        stack.setNbt(tag);
        return this;
    }

    public ItemBuilder addLore(List<Text> lore) {
        NbtCompound nbt = this.stack.getOrCreateNbt();
        NbtCompound displayNbt = this.stack.getOrCreateSubNbt("display");
        NbtList nbtLore = new NbtList();

        for (Text text : lore) {
            Text line = Texts.join(text.getWithStyle(Style.EMPTY.withItalic(false)), Text.of(""));
            nbtLore.add(NbtString.of(Text.Serializer.toJson(line)));
        }

        displayNbt.put("Lore", nbtLore);
        nbt.put("display", displayNbt);
        this.stack.setNbt(nbt);
        return this;
    }

    public ItemBuilder addLore(Text[] lore) {
        return this.addLore(Arrays.stream(lore).toList());
    }

    public ItemBuilder addLore(LoreLike lore) {
        return this.addLore(lore.getLore());
    }

    public ItemBuilder setCount(int count) {
        this.stack.setCount(count);
        return this;
    }

    public ItemBuilder hideAdditional() {
        this.stack.addHideFlag(ItemStack.TooltipSection.ADDITIONAL);
        return this;
    }
    public ItemBuilder setCustomName(Text customName) {
        Text pokemonName = Texts.join(customName.getWithStyle(Style.EMPTY.withItalic(false)), Text.of(""));
        this.stack.setCustomName(pokemonName);
        return this;
    }

    public ItemStack build() {
        return this.stack.copy();
    }

    public GuiElementBuilder gbuild() {
        return GuiElementBuilder.from(build());
    }

    public JsonObject toJson() {
        JsonObject itemData = new JsonObject();
        itemData.addProperty("id", getItemID());
        return itemData;
    }

    @Override
    public String toString() {
        return stack.toString();
    }
}
