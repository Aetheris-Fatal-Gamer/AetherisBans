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

public class CommandMutar implements CommandExecutor {
	
	private final MuteManager muteLegend = Legendchat.getMuteManager();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		String nameStaff = "*Console*";
		if ( sender instanceof Player ) {
			nameStaff = sender.getName();
		}
		
		if ( lbl.equalsIgnoreCase("mutar") ) {
			if ( sender.hasPermission(Core.prefixPerm + ".mute") ) {
				if ( args.length >= 3 ) {
					Player player = Core.pl.getServer().getPlayer(args[0]);
					if ( player == null ) {
						sender.sendMessage("§4[!] §cEsse player não está online.");
						return true;
					}
					
					if (Core.isInt(args[1])) {
						sender.sendMessage("§4[!] §cA quantidade de minutos deve ser um número inteiro.");
						return true;
					}
					
					if ( muteLegend.isPlayerMuted(player.getName()) ) {
						sender.sendMessage("§4[!] §cEsse player já está mutado.");
						return true;
					}
					
					String dateMute = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
					StringBuilder muteMotivo = new StringBuilder();
					for( int i = 2; i < args.length; ++i ) {
						muteMotivo.append(" ").append(args[i]);
					}
					muteLegend.mutePlayer(player.getName(), Integer.parseInt(args[1]));
					
					player.sendMessage("§4[!] §cVocê foi mutado por §4" + args[1] + " minutos §cpelo o staff §4" + nameStaff);
					Bukkit.broadcastMessage(
							"§2O jogador §a" + player.getName() + " §2foi mutado" +
							"\n§2Por: §a" + nameStaff +
							"\n§2Motivo: §a" + muteMotivo +
							"\n§2Tempo: §a" + args[1] + " minutos" +
							"\n§2Data e hora: §a" + dateMute
					);
					
					Webhook webhook = new Webhook(Core.pl.getConfig().getString("Settings.BotLink"));
					webhook.sendMessage(
							"**Servidor:** " + Core.pl.getConfig().getString("Settings.Servidor") +
							"\n**Ação:** Mutar" +
							"\n**Player:** " + player.getName() +
							"\n**Staff:** " + nameStaff +
							"\n**Motivo:** " + muteMotivo +
							"\n**Tempo:** " + args[1] + " minutos" +
							"\n**Data e hora:** " + dateMute
					);
				} else {
					sender.sendMessage("§4[!] §cUtilize: /mutar <nick> <tempo> <motivo>");
				}
			} else {
				sender.sendMessage("§4[!] §cSem permissão.");
			}
		}
		return false;
	}

}
