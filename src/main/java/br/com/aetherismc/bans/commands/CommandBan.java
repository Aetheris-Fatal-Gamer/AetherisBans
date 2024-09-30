package br.com.aetherismc.bans.commands;

import java.sql.Connection;
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

public class CommandBan implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		String nameStaff = "*Console*";
		if ( sender instanceof Player ) {
			nameStaff = sender.getName();
		}

		if ( lbl.equalsIgnoreCase("ban") ) {
			if ( sender.hasPermission(Core.prefixPerm + ".ban") || sender.hasPermission(Core.prefixPerm + ".tempban") ) {
				if ( args.length >= 3 ) {
					boolean playerExists = Core.pl.getServer().getPlayer(args[0]) != null;
					if (Core.isInt(args[1])) {
						sender.sendMessage("§4[!] §cA quantidade de minutos deve ser um número inteiro.");
						return true;
					}
					if ( Integer.parseInt(args[1]) < 0 ) {
						sender.sendMessage("§4[!] §cO tempo em minutos deve ser maior ou igual a 0.");
						return true;
					}
					if ( args[1].equalsIgnoreCase("0") && !sender.hasPermission(Core.prefixPerm + ".ban") ) {
						sender.sendMessage("§4[!] §cVocê não tem permissão para banir permanente.");
						return true;
					}
					if ( Integer.parseInt(args[1]) > 0 && !sender.hasPermission(Core.prefixPerm + ".tempban") ) {
						sender.sendMessage("§4[!] §cVocê não tem permissão para banir players.");
						return true;
					}
					StringBuilder banMotivo = new StringBuilder();
					for ( int i = 2; i < args.length; ++i ) {
						banMotivo.append(" ").append(args[i]);
					}

					String dateBan = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
					String playerIP;
					if ( playerExists ) {
						Player player = Core.pl.getServer().getPlayer(args[0]);
						player.kickPlayer(
								"§cVocê foi banido do servidor" +
										"\n§cPor: " + nameStaff +
										"\n§cMotivo: " + banMotivo +
										"\n§cTempo: " + ( (args[1].equalsIgnoreCase("0")) ? "Permanente" : args[1] + " minutos" ) +
										"\n§cData e hora: " + dateBan
						);
						playerIP = player.getAddress().getAddress().getHostAddress();
					} else {
						playerIP = "Offline";
					}
					String timeBD = "0";
					if ( !args[1].equalsIgnoreCase("0") ) {
						String.valueOf((int) (System.currentTimeMillis() / 1000L) + Integer.parseInt(args[1]) * 60);
					}

					Connection conn = Core.getConnection();
					Statement stm = null;
					if ( conn == null ) {
						sender.sendMessage("§4[!] §cErro na base de dados.");
						return false;
					}

					try {
						stm = conn.createStatement();
						stm.execute("INSERT INTO `" + Core.pl.getConfig().getString("MySQL.table") + "` " +
								"(`id`, `nick`, `adminNick` , `ip`, `banfrom`, `banto`, `reason`, `status`) " +
								"VALUES " +
								"(NULL, '" + args[0].replaceAll("'", "\"") + "', '" + nameStaff.replaceAll("'", "\"") + "' ,'" + playerIP + "', '" + (System.currentTimeMillis() / 1000L) + "', '" + timeBD + "', '" + banMotivo.toString().replaceAll("'", "\"") + "', '1');"
						);

						Bukkit.broadcastMessage(
								"§2O jogador §a" + args[0] + " §2foi banido do servidor" +
										"\n§2Por: §a" + nameStaff +
										"\n§2Motivo: §a" + banMotivo +
										"\n§2Tempo: §a" + ( (args[1].equalsIgnoreCase("0")) ? "Permanente" : args[1] + " minutos" ) +
										"\n§2Data e hora: §a" + dateBan
						);

						Webhook webhook = new Webhook(Core.pl.getConfig().getString("Settings.BotLink"));
						webhook.sendMessage(
								"**Servidor:** " + Core.pl.getConfig().getString("Settings.Servidor") +
										"\n**Ação:** Ban " + ( (args[1].equalsIgnoreCase("0")) ? "permanente" : "temporário" ) +
										"\n**Player:** " + args[0] +
										"\n**Staff:** " + nameStaff +
										"\n**Motivo:** " + banMotivo +
										"\n**Data e hora:** " + dateBan
						);
					} catch (SQLException e) {
						sender.sendMessage("§4[!] §cErro na base de dados na hora de salvar.");
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
					sender.sendMessage("§4[!] §cUtilize: /ban <nick> <tempo> <motivo>");
				}
			} else {
				sender.sendMessage("§4[!] §cSem permissão.");
			}
		}
		return false;
	}

}
