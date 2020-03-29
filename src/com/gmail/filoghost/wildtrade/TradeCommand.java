/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.wildtrade;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import wild.api.WildCommons;
import wild.api.command.CommandFramework;
import wild.api.command.CommandFramework.Permission;
import wild.api.sound.EasySound;

@Permission("wildtrade.use")
public class TradeCommand extends CommandFramework {
	
	private Map<Player, Long> cooldowns;

	public TradeCommand(JavaPlugin plugin) {
		super(plugin, "trade");
		cooldowns = new WeakHashMap<>();
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.DARK_PURPLE + "Comandi per il commercio:");
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "/trade sell <giocatore> <soldi>");
			sender.sendMessage(ChatColor.GRAY + " - Cerca di vendere al giocatore ciò che hai in mano");
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "/trade view <giocatore>");
			sender.sendMessage(ChatColor.GRAY + " - Vedi ed eventualmente accetta uno scambio");
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "/trade cancel");
			sender.sendMessage(ChatColor.GRAY + " - Elimina la tua proposta di scambio");
			return;
		}
		
		if (args[0].equalsIgnoreCase("sell")) {
		
			Player trader = CommandValidate.getPlayerSender(sender);
			CommandValidate.minLength(args, 3, "Utilizzo comando: /trade sell <giocatore> <soldi>");
			
			Player recipient = Bukkit.getPlayerExact(args[1]);
			CommandValidate.notNull(recipient, "Quel giocatore non è online.");
			
			CommandValidate.isTrue(recipient != trader, "Non puoi commerciare da solo!");
			
			int value = CommandValidate.getPositiveIntegerNotZero(args[2]);
			
			ItemStack itemToTrade = trader.getInventory().getItemInMainHand();
			CommandValidate.isTrue(itemToTrade != null && itemToTrade.getType() != Material.AIR, "Non hai nulla in mano!");
			
			if (cooldowns.containsKey(trader) && System.currentTimeMillis() - cooldowns.get(trader) < 15000) {
				trader.sendMessage(ChatColor.RED + "Devi aspettare almeno 15 secondi dall'ultima proposta di commercio che hai fatto.");
				return;
			}
			
			if (!trader.getWorld().equals(recipient.getWorld()) || trader.getLocation().distanceSquared(recipient.getLocation()) > 10 * 10) {
				trader.sendMessage(ChatColor.RED + "Devi essere in un raggio di 10 blocchi dall'altro giocatore.");
				return;
			}
			
			cooldowns.put(trader, System.currentTimeMillis());
			WildTrade.setActiveTrade(trader, new Trade(trader.getName(), recipient.getName(), itemToTrade, value));
			trader.sendMessage(WildTrade.CHAT_PREFIX + "Hai offerto l'oggetto che tieni in mano a " + recipient.getName() + " per " + WildTrade.DECIMAL_FORMAT.format(value) + " $. L'offerta è valida per 1 minuto.");
			
			EasySound.quickPlay(recipient, Sound.BLOCK_NOTE_HARP, 2F);
			WildCommons.fancyMessage("[Trade] ")
					.color(ChatColor.LIGHT_PURPLE)
					.then(trader.getName() + " vuole commerciare con te. ")
					.color(ChatColor.GRAY).send(recipient);
			WildCommons.fancyMessage("[Trade] ")
					.color(ChatColor.LIGHT_PURPLE)
					.then("CLICCA QUI")
					.tooltip(ChatColor.AQUA + "" + "Clicca per vedere l'offerta")
					.command("/trade view " + trader.getName())
					.color(ChatColor.WHITE)
					.style(ChatColor.BOLD)
					.then(" per vedere l'offerta.")
					.color(ChatColor.GRAY)
					.send(recipient);
			return;
		}
		
		if (args[0].equalsIgnoreCase("view")) {
			
			Player recipient = CommandValidate.getPlayerSender(sender);
			CommandValidate.minLength(args, 2, "Utilizzo comando: /trade view <giocatore>");
			
			Player trader = Bukkit.getPlayerExact(args[1]);
			CommandValidate.notNull(trader, "Quel giocatore non è online.");
			
			CommandValidate.isTrue(trader != recipient, "Non puoi commerciare da solo!");
			
			Trade traderTrade = WildTrade.getActiveTrade(trader);
			
			if (traderTrade == null || !traderTrade.getRecipientName().equalsIgnoreCase(recipient.getName())) {
				recipient.sendMessage(ChatColor.RED + "Quel giocatore non ha richiesto un commercio, lo ha cancellato, oppure è scaduto.");
				return;
			}
			
			if (!recipient.getWorld().equals(trader.getWorld()) || recipient.getLocation().distanceSquared(trader.getLocation()) > 100 * 100) {
				recipient.sendMessage(ChatColor.RED + "Devi essere in un raggio di 10 blocchi dall'altro giocatore.");
				return;
			}
			
			if (traderTrade.getElapsedMillisFromStart() > 60000) {
				WildTrade.removeActiveTrade(trader);
				recipient.sendMessage(ChatColor.RED + "E' passato più di 1 minuto, quel commercio è scaduto!");
				return;
			}
			
			traderTrade.displayGUI(recipient);
			return;
		}
		
		if (args[0].equalsIgnoreCase("cancel")) {
			
			Player player = CommandValidate.getPlayerSender(sender);
			Trade trade = WildTrade.getActiveTrade(player);
			CommandValidate.notNull(trade, "Non hai nessun commercio attivo, oppure è scaduto.");
			
			WildTrade.removeActiveTrade(player);
			player.sendMessage(WildTrade.CHAT_PREFIX + "Hai cancellato il commercio con " + trade.getRecipientName() + ".");
			if (trade.getElapsedMillisFromStart() > 60000) {
				player.sendMessage(WildTrade.CHAT_PREFIX + "Il commercio comunque era scaduto.");
			}
			
			return;
		}
		
		sender.sendMessage(ChatColor.RED + "Sub-comando sconosciuto. Scrivi /trade per vedere i comandi.");
	}

}
