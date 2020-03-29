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

import lombok.AllArgsConstructor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import wild.api.menu.ClickHandler;
import wild.api.sound.EasySound;

@AllArgsConstructor
public class TradeClickHandler implements ClickHandler {

	private Trade trade;
	
	@Override
	public void onClick(Player clicker) {
		
		if (!clicker.getName().equalsIgnoreCase(trade.getRecipientName())) {
			clicker.sendMessage(ChatColor.RED + "Non sei tu l'acquirente!");
			return;
		}
		
		Player trader = Bukkit.getPlayerExact(trade.getTraderName());
		
		if (trader == null) {
			clicker.sendMessage(ChatColor.RED + trade.getTraderName() + " non è più online.");
			return;
		}
		
		if (trade != WildTrade.getActiveTrade(trader)) {
			clicker.sendMessage(ChatColor.RED + "Quel giocatore ha cancellato lo scambio, oppure è scaduto.");
			return;
		}
		
		if (trade.getElapsedMillisFromStart() > 60000) {
			WildTrade.removeActiveTrade(trader);
			clicker.sendMessage(ChatColor.RED + "E' passato più di 1 minuto, quel commercio è scaduto!");
			return;
		}
		
		if (clicker.getInventory().firstEmpty() == -1) {
			clicker.sendMessage(ChatColor.RED + "Lascia almeno uno spazio vuoto nell'inventario.");
			return;
		}
		
		if (!trader.getWorld().equals(clicker.getWorld()) || trader.getLocation().distanceSquared(clicker.getLocation()) > 10 * 10) {
			clicker.sendMessage(ChatColor.RED + "Devi essere in un raggio di 10 blocchi dall'altro giocatore.");
			return;
		}
		
		if (!InventoryUtils.has(trader, trade.getItemToTrade(), trade.getItemToTrade().getAmount())) {
			clicker.sendMessage(ChatColor.RED + trade.getTraderName() + " non ha più quell'oggetto.");
			return;
		}
		
		if (!EconomyBridge.hasValidEconomy()) {
			clicker.sendMessage(ChatColor.RED + "Il server non ha un plugin per l'economia valido.");
			return;
		}
		
		if (!EconomyBridge.hasMoney(clicker, trade.getMoney())) {
			clicker.sendMessage(ChatColor.RED + "Non hai abbastanza soldi.");
			return;
		}
		
		WildTrade.removeActiveTrade(trader);
		
		EconomyBridge.giveMoney(trader, trade.getMoney());
		EconomyBridge.takeMoney(clicker, trade.getMoney());
		
		
		InventoryUtils.take(trader, trade.getItemToTrade(), trade.getItemToTrade().getAmount());
		clicker.getInventory().addItem(trade.getItemToTrade());
		
		trader.sendMessage(WildTrade.CHAT_PREFIX + ChatColor.GREEN + clicker.getName() + " ha accettato lo scambio per " + trade.getMoney() + " $.");
		clicker.sendMessage(WildTrade.CHAT_PREFIX + ChatColor.GREEN + "Hai accettato lo scambio per " + trade.getMoney() + " $.");
		
		EasySound.quickPlay(trader, Sound.ENTITY_PLAYER_LEVELUP, 1.6F);
		EasySound.quickPlay(clicker, Sound.ENTITY_PLAYER_LEVELUP, 1.6F);
	}

}
