package br.com.aetherismc.bans.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.mutes.MuteManager;
import br.com.aetherismc.bans.Core;
import br.com.aetherismc.bans.discord.Webhook;

public class CommandDesmutar implements CommandExecutor {
	
	private final MuteManager muteLegend = Legendchat.getMuteManager();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		String nameStaff = "*Console*";
		if ( sender instanceof Player ) {
			nameStaff = sender.getName();
		}
		
		if ( lbl.equalsIgnoreCase("desmutar") ) {
			if ( sender.hasPermission(Core.prefixPerm + ".unmute") ) {
				if ( args.length >= 2 ) {
					Player player = Core.pl.getServer().getPlayer(args[0]);
					if ( player == null ) {
						sender.sendMessage("§4[!] §cEsse player não está online.");
						return true;
					}
					
					if ( !muteLegend.isPlayerMuted(player.getName()) ) {
						sender.sendMessage("§4[!] §cEsse player não está mutado.");
						return true;
					}
					
					String dateMute = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
					StringBuilder desmuteMotivo = new StringBuilder();
					for( int i = 1; i < args.length; ++i ) {
						desmuteMotivo.append(" ").append(args[i]);
					}
					muteLegend.unmutePlayer(player.getName());
					
					player.sendMessage("§4[!] §cVocê foi desmutado pelo o staff §4" + nameStaff);
					Bukkit.broadcastMessage(
							"§2O jogador §a" + player.getName() + " §2foi desmutado" +
							"\n§2Por: §a" + nameStaff +
							"\n§2Motivo: §a" + desmuteMotivo +
							"\n§2Data e hora: §a" + dateMute
					);
					
					Webhook webhook = new Webhook(Core.pl.getConfig().getString("Settings.BotLink"));
					webhook.sendMessage(
							"**Servidor:** " + Core.pl.getConfig().getString("Settings.Servidor") +
							"\n**Ação:** Desmutar" +
							"\n**Player:** " + player.getName() +
							"\n**Staff:** " + nameStaff +
							"\n**Motivo:** " + desmuteMotivo +
							"\n**Data e hora:** " + dateMute
					);
				} else {
					sender.sendMessage("§4[!] §cUtilize: /desmutar <nick> <motivo>");
				}
			} else {
				sender.sendMessage("§4[!] §cSem permissão.");
			}
		}
		return false;
	}

}
