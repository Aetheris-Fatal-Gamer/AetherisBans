package br.com.aetherismc.bans.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.com.aetherismc.bans.Core;
import br.com.aetherismc.bans.discord.Webhook;

import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommandUnbanIP implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		String nameStaff = "*Console*";
		if ( sender instanceof Player ) {
			nameStaff = sender.getName();
		}
		
		if ( lbl.equalsIgnoreCase("unbanip") ) {
        	if ( sender.hasPermission(Core.prefixPerm + ".unbanip") ) {
        		if (args.length >= 2) {
    				Connection conn = Core.getConnection();
    				Statement stm = null;
    				if ( conn == null ) {
    					return false;
    				}

    				try {
    					stm = conn.createStatement();
    					String playerIP = args[0].toLowerCase().replaceAll("'", "\"");
    					ResultSet result = stm.executeQuery("SELECT * FROM `" + Core.pl.getConfig().getString("MySQL.table") + "` WHERE `ip` = '" + playerIP + "' and `status` = 1");

    					boolean banAtivo;
    					for ( banAtivo = false; result.next(); banAtivo = true ) {
    						PreparedStatement pstmt = conn.prepareStatement("UPDATE " + Core.pl.getConfig().getString("MySQL.table") + " SET status = 2 WHERE id=?");
    						pstmt.setInt(1, result.getInt("id"));
    						pstmt.executeUpdate();
    						pstmt.close();
    					}

    					if ( banAtivo ) {
    						StringBuilder unbanIPMotivo = new StringBuilder();
    						for ( int i = 1; i < args.length; ++i ) {
    							unbanIPMotivo.append(" ").append(args[i]);
    						}
    						
    						String dateUnban = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
							Bukkit.broadcastMessage(
									"§2O IP §a" + args[0] + " §2foi desbanido do servidor" +
									"\n§2Por: §a" + nameStaff +
									"\n§2Motivo: §a" + unbanIPMotivo +
									"\n§2Data e hora: §a" + dateUnban +
									"\n"
							);
							
							Webhook webhook = new Webhook(Core.pl.getConfig().getString("Settings.BotLink"));
							webhook.sendMessage(
									"**Servidor:** " + Core.pl.getConfig().getString("Settings.Servidor") +
									"\n**Ação:** UnbanIP" +
									"\n**IP:** " + args[0] +
									"\n**Staff:** " + nameStaff +
									"\n**Motivo:** " + unbanIPMotivo +
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
        			sender.sendMessage("§4[!] §cUtilize: /unban <IP ADDRESS> <motivo>");
        		}
        	} else {
        		sender.sendMessage("§4[!] §cSem permissão.");
        	}	
        }
		return false;
	}
}
