package network.roanoke.ttms.gui

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

        fun getTMShop(player: ServerPlayerEntity): SimpleGui {
            val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)

            val tmList: List<GuiElementBuilder> = TTMs.tmsConfig.moveData.filter { it.price != -1 }.map {
                GuiElementBuilder.from(TMs.getTR(it.move))
                    .addLoreLine(Text.literal("§6§lPrice: §r§f$${it.price}"))
                    .setCallback { _, _, _ ->
                    getConfirmationWindow(player, it).open()
                }
            }
            val paginatedSection = PaginatedSection(tmList).setSlotRanges(
                listOf(
                    SlotRange(10, 16), SlotRange(19, 25), SlotRange(29, 33)
                )
            )

            paginatedSection.applyToGui(gui)

            gui.title = Text.literal("TR Shop")

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

            fillGUI(gui)

            return gui
        }

        private fun getConfirmationWindow(player: ServerPlayerEntity, moveData: MoveData): SimpleGui {
            val account = EconomyService.instance().account(player.uuid).get()
            val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)

            gui.title = Text.literal("Are you sure?")

            gui.setSlot(13, GuiElementBuilder.from(TMs.getTR(moveData.move))
                .addLoreLine(Text.literal("§6§lPrice: §r§f$${moveData.price}"))
                .build())

            gui.setSlot(30,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc5YTVjOTVlZTE3YWJmZWY0NWM4ZGMyMjQxODk5NjQ5NDRkNTYwZjE5YTQ0ZjE5ZjhhNDZhZWYzZmVlNDc1NiJ9fX0=",
                    null,
                    null
                ).setName(Text.literal("§aConfirm"))
                    .setCallback { _, _, _ ->
                        val tr = TMs.getTR(moveData.move)
                        val price: BigDecimal = moveData.price.toBigDecimal()
                        if (account.balanceAsync().get() < price) {
                            player.sendMessage(Text.literal("§cYou can't afford this TR."))
                        } else {
                            account.withdrawAsync(price)
                            player.sendMessage(Text.literal("§aYou have purchased ").append(tr.name).append("§a!"))
                            player.inventory.insertStack(tr)
                        }
                        getTMShop(player).open()
                    })

            gui.setSlot(32,
                GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1NDgzNjJhMjRjMGZhODQ1M2U0ZDkzZTY4YzU5NjlkZGJkZTU3YmY2NjY2YzAzMTljMWVkMWU4NGQ4OTA2NSJ9fX0=",
                    null,
                    null
                ).setName(Text.literal("§4Cancel"))
                    .setCallback { _, _, _ ->
                        getTMShop(player).open()
                    })

            fillGUI(gui)

            return gui
        }

    }
}