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

public class CommandUnban implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		String nameStaff = "*Console*";
		if ( sender instanceof Player ) {
			nameStaff = sender.getName();
		}
		
		if (lbl.equalsIgnoreCase("unban")) {
    		if ( sender.hasPermission(Core.prefixPerm + ".unban") ) {
    			if (args.length >= 2) {
    				StringBuilder unbanMotivo = new StringBuilder();
					for ( int i = 1; i < args.length; ++i ) {
						unbanMotivo.append(" ").append(args[i]);
					}
    				
					Connection conn = Core.getConnection();
					Statement stm = null;
					if ( conn == null ) {
						return false;
					}
					try {
						stm = conn.createStatement();
						String playerNick = args[0].toLowerCase().replaceAll("'", "\"");
						
						ResultSet result = stm.executeQuery("SELECT id FROM `" + Core.pl.getConfig().getString("MySQL.table") + "` WHERE `nick` = '" + playerNick + "' and `status` = 1");
						boolean banAtivo = false;
						PreparedStatement pstmt;
						for( pstmt = conn.prepareStatement("UPDATE " + Core.pl.getConfig().getString("MySQL.table") + " SET status = 2, unbanreason=?, unbannick=? WHERE id=?"); result.next(); banAtivo = true) {
							pstmt.setString(1, playerNick);
							pstmt.setString(2, unbanMotivo.toString().replaceAll("'", "\""));
							pstmt.setInt(3, result.getInt("id"));
							pstmt.executeUpdate();
						}
						pstmt.close();
						
						if ( banAtivo ) {
							String dateUnban = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
							Bukkit.broadcastMessage(
									"§2O jogador §a" + args[0] + " §2foi desbanido do servidor" +
									"\n§2Por: §a" + nameStaff +
									"\n§2Motivo: §a" + unbanMotivo +
									"\n§2Data e hora: §a" + dateUnban 
							);
							
							Webhook webhook = new Webhook(Core.pl.getConfig().getString("Settings.BotLink"));
							webhook.sendMessage(
									"**Servidor:** " + Core.pl.getConfig().getString("Settings.Servidor") +
									"\n**Ação:** Unban" +
									"\n**Player:** " + args[0] +
									"\n**Staff:** " + nameStaff +
									"\n**Motivo:** " + unbanMotivo +
									"\n**Data e hora:** " + dateUnban
							);
						} else {
							sender.sendMessage("§4[!] §cO player não tem nenhum ban ativo.");
						}
					} catch (SQLException e) {
						sender.sendMessage("§4[!] §cErro em verificar na base de dados.");
					} finally {
						try {
							if ( stm != null ) {
								stm.close();
							}
                            conn.close();
                        } catch (SQLException ignored) {
						}
					}
    			} else {
    				sender.sendMessage("§4[!] §cUtilize: /unban <nick> <motivo>");
    			}
    		} else {
    			sender.sendMessage("§4[!] §cSem permissão.");
    		}
    	}
		return false;
	}

}
