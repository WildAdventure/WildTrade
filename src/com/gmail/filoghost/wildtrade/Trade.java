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

import java.util.Arrays;

import javax.annotation.Nonnull;

import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import wild.api.menu.ClickHandler;
import wild.api.menu.IconBuilder;
import wild.api.menu.IconMenu;
import wild.api.menu.StaticIcon;

@Getter
@Nonnull
public class Trade {

	private String traderName;
	private String recipientName;
	private ItemStack itemToTrade;
	private int money;
	private long startedOn;
	private IconMenu iconMenu;
	
	public Trade(String traderName, String recipientName, ItemStack tradedItem, int money) {
		this.traderName = traderName;
		this.recipientName = recipientName;
		this.itemToTrade = tradedItem.clone();
		this.money = money;
		this.startedOn = System.currentTimeMillis();
	}
	
	public long getElapsedMillisFromStart() {
		return System.currentTimeMillis() - startedOn;
	}

	public void displayGUI(Player player) {
		if (iconMenu == null) {
			iconMenu = new IconMenu("Commercio", 5);
			
			StaticIcon itemIcon = new StaticIcon(itemToTrade, false);
			itemIcon.setClickHandler(new ClickHandler() {
				@Override
				public void onClick(Player player) {
					player.sendMessage(ChatColor.RED + "Guarda sul blocco d'oro il prezzo, cliccalo per accettare.");
				}
			});
			
			iconMenu.setIcon(5, 2, itemIcon);
			iconMenu.setIcon(5, 4, new IconBuilder(Material.GOLD_BLOCK)
												.name(ChatColor.GOLD + ChatColor.BOLD.toString() + WildTrade.DECIMAL_FORMAT.format(money) + " $")
												.lore(Arrays.asList(ChatColor.GRAY + "Clicca per accettare."))
												.clickHandler(new TradeClickHandler(this))
												.closeOnClick(true)
										.build());
			iconMenu.refresh();
		}
		
		iconMenu.open(player);
	}
	
}
