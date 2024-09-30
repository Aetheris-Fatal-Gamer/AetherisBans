package br.com.aetherismc.bans.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.com.aetherismc.bans.Core;
import br.com.aetherismc.bans.discord.Webhook;

public class CommandBanIP implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		String nameStaff = "*Console*";
		if ( sender instanceof Player ) {
			nameStaff = sender.getName();
		}
		
		if ( lbl.equalsIgnoreCase("banip") ) {
			if ( sender.hasPermission(Core.prefixPerm + ".banip") ) {
				if ( args.length >= 3 ) {
					if (Core.isInt(args[1])) {
						sender.sendMessage("§4[!] §cA quantidade de minutos deve ser um número inteiro.");
						return true;
					}
					if ( Integer.parseInt(args[1]) < 0 ) {
						sender.sendMessage("§4[!] §cO tempo em minutos deve ser maior ou igual a 0.");
						return true;
					}
					String timeBD = String.valueOf((int) (System.currentTimeMillis() / 1000L) + Integer.parseInt(args[1]) * 60);
					StringBuilder banIPMotivo = new StringBuilder();
					for ( int i = 2; i < args.length; ++i ) {
						banIPMotivo.append(" ").append(args[i]);
					}
					banIPMotivo = new StringBuilder(banIPMotivo.toString().replaceAll("'", "\""));
					String playerIP = args[0].toLowerCase().replaceAll("'", "\"");
					
					Connection conn = Core.getConnection();
					Statement stm = null;
					if ( conn == null ) {
						sender.sendMessage("§4[!] §cErro na base de dados.");
						return false;
					}
					try {
						stm = conn.createStatement();
						String playerNick = "IP BANIDO";
						
						ResultSet rs1 = stm.executeQuery("SELECT * FROM `" + Core.pl.getConfig().getString("MySQL.table-history") + "` WHERE `ip` = '" + playerIP + "' LIMIT 1");
						if ( rs1.next() ) {
							playerNick = rs1.getString("name");
							sender.sendMessage("§4[!] §cVocê baniu o player: " + playerNick);
						}
						PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `" + Core.pl.getConfig().getString("MySQL.table") + "` (`id`, `nick`, `adminNick` ,`ip`, `banfrom`, `banto`, `reason`, `status`) VALUES (NULL, ?, ?, ?, ?, ?, ?, '1');");
						pstmt.setString(1, playerNick);
						pstmt.setString(2, nameStaff);
						pstmt.setString(3, playerIP);
						pstmt.setLong(4, System.currentTimeMillis() / 1000L);
						pstmt.setString(5, timeBD);
						pstmt.setString(6, banIPMotivo.toString());
						pstmt.execute();
						pstmt.close();
						
						String dateBanIP = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
						Bukkit.broadcastMessage(
								"§2O jogador §a" + playerNick + " §2foi banido IP do servidor" +
								"\n§2IP: §a" + playerIP +
								"\n§2Por: §a" + nameStaff +
								"\n§2Motivo: §a" + banIPMotivo +
								"\n§2Tempo: §a" + ( (args[1].equalsIgnoreCase("0")) ? "Permanente" : args[1] + " minutos" ) +
								"\n§2Data e hora: §a" + dateBanIP
						);
						
						Webhook webhook = new Webhook(Core.pl.getConfig().getString("Settings.BotLink"));
						webhook.sendMessage(
								"**Servidor:** " + Core.pl.getConfig().getString("Settings.Servidor") +
								"\n**Ação:** BanIP " + ( (args[1].equalsIgnoreCase("0")) ? "permanente" : "temporário" ) +
								"\n§cIP: " + playerIP +
								"\n**Player:** " + playerNick +
								"\n**Staff:** " + nameStaff +
								"\n**Motivo:** " + banIPMotivo +
								"\n**Data e hora:** " + dateBanIP
						);
					} catch (SQLException ignored) {
					} finally {
						try {
							if (stm != null) {
								stm.close();
							}
                            conn.close();
                        } catch (SQLException ignored) {
						}
					}
				} else {
					sender.sendMessage("§4[!] §cUtilize: /banip <IP ADDRESS> <tempo> <motivo>");
				}
			} else {
				sender.sendMessage("§4[!] §cSem permissão.");
			}
		}
		return false;
	}
	
}
