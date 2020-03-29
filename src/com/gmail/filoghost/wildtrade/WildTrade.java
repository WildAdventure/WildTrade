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

import java.text.DecimalFormat;
import java.util.Map;
import java.util.WeakHashMap;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WildTrade extends JavaPlugin implements Listener {
	
	public static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "[Trade] " + ChatColor.GRAY;
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###");

	@Getter private static WildTrade instance;
	private static Map<Player, Trade> activeTrades;
	
	
	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("WildCommons")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto WildCommons!");
			setEnabled(false);
			return;
		}
		
		activeTrades = new WeakHashMap<>();
		new TradeCommand(this);
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		if (!EconomyBridge.setupEconomy()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "No economy plugin or Vault found!");
		}
	}
	
	public static void removeActiveTrade(Player player) {
		activeTrades.remove(player);
	}
	
	public static void setActiveTrade(Player player, Trade trade) {
		activeTrades.put(player, trade);
	}
	
	public static Trade getActiveTrade(Player player) {
		return activeTrades.get(player);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		activeTrades.remove(event.getPlayer());
	}
}
