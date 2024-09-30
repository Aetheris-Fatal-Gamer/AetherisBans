package br.com.aetherismc.bans.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.com.aetherismc.bans.Core;
import br.com.aetherismc.bans.discord.Webhook;

public class CommandKick implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		String nameStaff = "*Console*";
		if ( sender instanceof Player ) {
			nameStaff = sender.getName();
		}
		
		// Comando de kick
		if (lbl.equalsIgnoreCase("kick")) {
			if ( sender.hasPermission(Core.prefixPerm + ".kick") ) {
				if ( args.length >= 2 ) {
					if ( Core.pl.getServer().getPlayer(args[0]) != null ) {
						Player player = Core.pl.getServer().getPlayer(args[0]);
						StringBuilder kickMotivo = new StringBuilder();
						for( int i = 1; i < args.length; ++i ) {
							kickMotivo.append(" ").append(args[i]);
						}
						String dateKick = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
						Bukkit.broadcastMessage(
								"§2O jogador §a" + player.getName() + " §2foi expulso do servidor" +
								"\n§2Por: §a" + nameStaff +
								"\n§2Motivo: §a" + kickMotivo +
								"\n§2Data e hora: §a" + dateKick
						);
						player.kickPlayer(
								"§2Você foi expluso do servidor" +
								"\n§2Por: §a" + nameStaff +
								"\n§2Motivo: §a" + kickMotivo +
								"\n§2Data e hora: §a" + dateKick 
						);
						Webhook webhook = new Webhook(Core.pl.getConfig().getString("Settings.BotLink"));
						webhook.sendMessage(
								"**Servidor:** " + Core.pl.getConfig().getString("Settings.Servidor") +
								"\n**Ação:** Kick" +
								"\n**Player:** " + player.getName() +
								"\n**Staff:** " + nameStaff +
								"\n**Motivo:** " + kickMotivo +
								"\n**Data e hora:** " + dateKick
						);
					} else {
						sender.sendMessage("§4[!] §cEsse player não está online.");
					}
				} else {
					sender.sendMessage("§4[!] §cUtilize: /kick <nick> <motivo>");
				}
			} else {
				sender.sendMessage("§4[!] §cSem permissão.");
			}
		}
		return false;
	}

}
