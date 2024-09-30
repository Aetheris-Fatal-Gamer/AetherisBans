package br.com.aetherismc.bans.execute;

import org.bukkit.entity.Player;

public interface PlayerObject {
	boolean ban(Player p, int var2, String var3, String var4);

	boolean unban(Player p, String var2, String var3);

	boolean banIp(String var1, int var2, String var3, String var4);

	boolean unbanIp(String var1, String var2, String var3);

	boolean isBanned(Player p);

	boolean hasBans(Player p);

	int ammountOfBans(Player p, boolean var2);
}
