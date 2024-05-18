package network.roanoke.ttms.gui

import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.impactdev.impactor.api.economy.EconomyService
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import network.roanoke.ttms.TTMs
import network.roanoke.ttms.items.TMs
import network.roanoke.ttms.utils.MoveData
import java.math.BigDecimal

class GUIs {
    companion object {

        private fun fillGUI(gui: SimpleGui) {
            var freeslot = gui.firstEmptySlot
            while (freeslot != -1) {
                gui.setSlot(
                    freeslot,
                    GuiElementBuilder.from(Items.BLACK_STAINED_GLASS_PANE.defaultStack.setCustomName(Text.literal("")))
                )
                freeslot = gui.firstEmptySlot
            }
        }

        private fun getShop(player: ServerPlayerEntity, shopType: String, nameSorting: Boolean): SimpleGui {
            val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)

            var list: List<MoveData> = if (shopType == "TR") {
                TTMs.tmsConfig.trsMoveData.filter { it.price != -1 }
            } else {
                TTMs.tmsConfig.tmsMoveData.filter { it.price != -1 }
            }

            list = if (nameSorting) {
                list.sortedBy { it.move }
            } else {
                list.sortedBy { it.number }
            }

            val moveList: List<GuiElementBuilder> = list.map {
                GuiElementBuilder.from(if (shopType == "TR") TMs.getTR(it.move) else TMs.getTM(it.move))
                    .addLoreLine(Text.literal("§6§lPrice: §r§f$${it.price}"))
                    .setCallback { _, _, _ ->
                        getConfirmationWindow(player, it, shopType, gui).open()
                    }
            }

            val paginatedSection = PaginatedSection(moveList).setSlotRanges(
                listOf(
                    SlotRange(10, 16), SlotRange(19, 25), if (moveList.size > 21) SlotRange(29, 33) else SlotRange(28, 34)
                )
            )

            paginatedSection.applyToGui(gui)

            gui.title = Text.literal("TR Shop")

            if (moveList.size > 21) {
            gui.setSlot(28,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=",
                    null,
                    null
                )
                    .setName(Text.literal("§fPrevious"))
                    .setCallback { _, _, _ ->
                        paginatedSection.decrementPage()
                        paginatedSection.applyToGui(gui)
                    })

            gui.setSlot(34,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19",
                    null,
                    null
                )
                    .setName(Text.literal("§fNext"))
                    .setCallback { _, _, _ ->
                        paginatedSection.incrementPage()
                        paginatedSection.applyToGui(gui)
                    })
            }

            var itemName = "§7Sort By Name"
            var itemDescription = "§fCurrently sorting by Number"
            if (nameSorting) {
               itemName = "§7Sort By Number"
                itemDescription = "§fCurrently sorting by Name"
            }

            gui.setSlot(4,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRkNDliYWU5NWM3OTBjM2IxZmY1YjJmMDEwNTJhNzE0ZDYxODU0ODFkNWIxYzg1OTMwYjNmOTlkMjMyMTY3NCJ9fX0=",
                    null,
                    null
                ).setName(Text.literal(itemName))
                    .addLoreLine(Text.literal(itemDescription))
                    .setCallback { _, _, _ ->
                        getShop(player, shopType, !nameSorting).open()
                    })

            gui.setSlot(40,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRmNWMyZjg5M2JkM2Y4OWNhNDA3MDNkZWQzZTQyZGQwZmJkYmE2ZjY3NjhjODc4OWFmZGZmMWZhNzhiZjYifX19",
                    null,
                    null
                ).setName(Text.literal("§cBack"))
                    .setCallback { _, _, _ ->
                        getShopStartGUI(player, shopType, nameSorting).open()
                    })

            fillGUI(gui)

            return gui
        }

        private fun getConfirmationWindow(player: ServerPlayerEntity, moveData: MoveData, shopType: String, oldUi: SimpleGui): SimpleGui {
            val account = EconomyService.instance().account(player.uuid).get()
            val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)

            gui.title = Text.literal("Are you sure?")

            gui.setSlot(13, GuiElementBuilder.from(if (shopType == "TR") TMs.getTR(moveData.move) else TMs.getTM(moveData.move))
                .addLoreLine(Text.literal("§6§lPrice: §r§f$${moveData.price}"))
                .build())

            gui.setSlot(30,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc5YTVjOTVlZTE3YWJmZWY0NWM4ZGMyMjQxODk5NjQ5NDRkNTYwZjE5YTQ0ZjE5ZjhhNDZhZWYzZmVlNDc1NiJ9fX0=",
                    null,
                    null
                ).setName(Text.literal("§aConfirm"))
                    .setCallback { _, _, _ ->
                        val move = if (shopType == "TR") TMs.getTR(moveData.move) else TMs.getTM(moveData.move)
                        val price: BigDecimal = moveData.price.toBigDecimal()
                        if (account.balanceAsync().get() < price) {
                            player.sendMessage(Text.literal("§cYou can't afford this $shopType."))
                        } else {
                            if (player.inventory.emptySlot != -1) {
                                account.withdrawAsync(price)
                                player.sendMessage(Text.literal("§aYou have purchased ").append(move.name).append("§a!"))
                                player.inventory.insertStack(move)
                            } else {
                                player.sendMessage(Text.literal("§cYou don't have enough space in your inventory."))
                            }
                        }
                        oldUi.open()
                    })

            gui.setSlot(32,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1NDgzNjJhMjRjMGZhODQ1M2U0ZDkzZTY4YzU5NjlkZGJkZTU3YmY2NjY2YzAzMTljMWVkMWU4NGQ4OTA2NSJ9fX0=",
                    null,
                    null
                ).setName(Text.literal("§4Cancel"))
                    .setCallback { _, _, _ ->
                        oldUi.open()
                    })

            fillGUI(gui)

            return gui
        }

        fun getShopStartGUI(player: ServerPlayerEntity, shopType: String, nameSorting: Boolean): SimpleGui {
            val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
            gui.title = Text.literal("$shopType Shop")

            gui.setSlot(10, GuiElementBuilder.from(TMs.getTR("protect"))
                .setName(Text.literal("§fNormal"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.NORMAL, shopType, nameSorting).open()
                })

            gui.setSlot(11, GuiElementBuilder.from(TMs.getTR("flamethrower"))
                .setName(Text.literal("§fFire"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.FIRE, shopType, nameSorting).open()
                })

            gui.setSlot(12, GuiElementBuilder.from(TMs.getTR("brine"))
                .setName(Text.literal("§fWater"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.WATER, shopType, nameSorting).open()
                })

            gui.setSlot(19, GuiElementBuilder.from(TMs.getTR("gigadrain"))
                .setName(Text.literal("§fGrass"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.GRASS, shopType, nameSorting).open()
                })

            gui.setSlot(20, GuiElementBuilder.from(TMs.getTR("thunderbolt"))
                .setName(Text.literal("§fElectric"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.ELECTRIC, shopType, nameSorting).open()
                })

            gui.setSlot(21, GuiElementBuilder.from(TMs.getTR("icebeam"))
                .setName(Text.literal("§fIce"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.ICE, shopType, nameSorting).open()
                })

            gui.setSlot(28, GuiElementBuilder.from(TMs.getTR("closecombat"))
                .setName(Text.literal("§fFighting"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.FIGHTING, shopType, nameSorting).open()
                })

            gui.setSlot(29, GuiElementBuilder.from(TMs.getTR("sludgebomb"))
                .setName(Text.literal("§fPoison"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.POISON, shopType, nameSorting).open()
                })

            gui.setSlot(30, GuiElementBuilder.from(TMs.getTR("earthquake"))
                .setName(Text.literal("§fGround"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.GROUND, shopType, nameSorting).open()
                })

            gui.setSlot(14, GuiElementBuilder.from(TMs.getTR("acrobatics"))
                .setName(Text.literal("§fFlying"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.FLYING, shopType, nameSorting).open()
                })

            gui.setSlot(15, GuiElementBuilder.from(TMs.getTR("psychic"))
                .setName(Text.literal("§fPsychic"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.PSYCHIC, shopType, nameSorting).open()
                })

            gui.setSlot(16, GuiElementBuilder.from(TMs.getTR("strugglebug"))
                .setName(Text.literal("§fBug"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.BUG, shopType, nameSorting).open()
                })

            gui.setSlot(23, GuiElementBuilder.from(TMs.getTR("rocktomb"))
                .setName(Text.literal("§fRock"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.ROCK, shopType, nameSorting).open()
                })

            gui.setSlot(24, GuiElementBuilder.from(TMs.getTR("shadowclaw"))
                .setName(Text.literal("§fGhost"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.GHOST, shopType, nameSorting).open()
                })

            gui.setSlot(25, GuiElementBuilder.from(TMs.getTR("dragondance"))
                .setName(Text.literal("§fDragon"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.DRAGON, shopType, nameSorting).open()
                })

            gui.setSlot(32, GuiElementBuilder.from(TMs.getTR("thief"))
                .setName(Text.literal("§fDark"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.DARK, shopType, nameSorting).open()
                })

            gui.setSlot(33, GuiElementBuilder.from(TMs.getTR("flashcannon"))
                .setName(Text.literal("§fSteel"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.STEEL, shopType, nameSorting).open()
                })

            gui.setSlot(34, GuiElementBuilder.from(TMs.getTR("drainingkiss"))
                .setName(Text.literal("§fFairy"))
                .setCallback { _, _, _ ->
                    getTypeStore(player, ElementalTypes.FAIRY, shopType, nameSorting).open()
                })

            gui.setSlot(22, GuiElementBuilder.from(TMs.getTR("protect"))
                .setName(Text.literal("§fAll Types"))
                .setCallback { _, _, _ ->
                    getShop(player, shopType, false).open()
                })

            fillGUI(gui)

            return gui
        }

        private fun getTypeStore(player: ServerPlayerEntity, type: ElementalType, shopType: String, nameSorting: Boolean): SimpleGui {
            val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)

            var list: List<MoveData> = if (shopType == "TR") {
                TTMs.tmsConfig.trsMoveData.filter { it.price != -1 && Moves.getByName(it.move)!!.elementalType == type }
            } else {
                TTMs.tmsConfig.tmsMoveData.filter { it.price != -1 && Moves.getByName(it.move)!!.elementalType == type }
            }

            list = if (nameSorting) {
                list.sortedBy { it.move }
            } else {
                list.sortedBy { it.number }
            }

            val moveList: List<GuiElementBuilder> = list.map {
                GuiElementBuilder.from(if (shopType == "TR") TMs.getTR(it.move) else TMs.getTM(it.move))
                    .addLoreLine(Text.literal("§6§lPrice: §r§f$${it.price}"))
                    .setCallback { _, _, _ ->
                        getConfirmationWindow(player, it, shopType, gui).open()
                    }
            }

            val paginatedSection = PaginatedSection(moveList).setSlotRanges(
                listOf(
                    SlotRange(10, 16), SlotRange(19, 25), if (moveList.size > 21) SlotRange(29, 33) else SlotRange(28, 34)
                )
            )

            paginatedSection.applyToGui(gui)

            gui.title = Text.literal("$shopType Shop")

            if (moveList.size > 21) {
            gui.setSlot(28,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=",
                    null,
                    null
                )
                    .setName(Text.literal("§fPrevious"))
                    .setCallback { _, _, _ ->
                        paginatedSection.decrementPage()
                        paginatedSection.applyToGui(gui)
                    })

            gui.setSlot(34,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19",
                    null,
                    null
                )
                    .setName(Text.literal("§fNext"))
                    .setCallback { _, _, _ ->
                        paginatedSection.incrementPage()
                        paginatedSection.applyToGui(gui)
                    })
            }

            var itemName = "§7Sort By Name"
            var itemDescription = "§fCurrently sorting by Number"
            if (nameSorting) {
                itemName = "§7Sort By Number"
                itemDescription = "§fCurrently sorting by Name"
            }

            gui.setSlot(4,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRkNDliYWU5NWM3OTBjM2IxZmY1YjJmMDEwNTJhNzE0ZDYxODU0ODFkNWIxYzg1OTMwYjNmOTlkMjMyMTY3NCJ9fX0=",
                    null,
                    null
                ).setName(Text.literal(itemName))
                    .addLoreLine(Text.literal(itemDescription))
                    .setCallback { _, _, _ ->
                        getTypeStore(player, type, shopType, !nameSorting).open()
                    })

            gui.setSlot(40,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRmNWMyZjg5M2JkM2Y4OWNhNDA3MDNkZWQzZTQyZGQwZmJkYmE2ZjY3NjhjODc4OWFmZGZmMWZhNzhiZjYifX19",
                    null,
                    null
                ).setName(Text.literal("§cBack"))
                    .setCallback { _, _, _ ->
                        getShopStartGUI(player, shopType, nameSorting).open()
                    })

            fillGUI(gui)

            return gui
        }

    }
}