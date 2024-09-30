package br.com.aetherismc.bans.execute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.api.events.PrivateMessageEvent;
import br.com.aetherismc.bans.Core;

public class PlayerListener implements Listener {
	
	public ArrayList<Player> playersUnban = new ArrayList<Player>();

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		String playerNick = p.getName().toLowerCase().replaceAll("'", "\"");
		String playerIP = p.getAddress().getAddress().getHostAddress();
		
		if ( playersUnban.contains(p) ) {
			playersUnban.remove(p);
			p.sendMessage("§4[!] §cSeu tempo de banimento acabou, bem vindo novamente.");
		} else {
			Connection conn = Core.getConnection();
			Statement stm = null;	
			try {
				if (conn != null) {
					stm = conn.createStatement();
					ResultSet result;
					result = stm.executeQuery("SELECT * FROM `" + Core.pl.getConfig().getString("MySQL.table-history") + "` WHERE `name` = '" + playerNick + "'");
					if ( !result.next() ) {
						PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `" + Core.pl.getConfig().getString("MySQL.table-history") + "` (`id`, `name`, `ip`) VALUES (NULL, ?, ?);", Statement.RETURN_GENERATED_KEYS);
						pstmt.setString(1, playerNick);
						pstmt.setString(2, playerIP);
						pstmt.execute();
						pstmt.close();
					}
						
					result = stm.executeQuery("SELECT * FROM `" + Core.pl.getConfig().getString("MySQL.table") + "` WHERE `ip` = '" + playerIP + "' And `status` = 1 ORDER BY  `banfrom` DESC");
					int currentBan;
					long currentTime;
					if ( result.next() ) {
						currentBan = result.getInt("banto");
						currentTime = System.currentTimeMillis() / 1000L;
						if (currentBan == 0 || (long) currentBan >= currentTime) {
							p.kickPlayer("§cSeu IP está banido do servidor.");
						}
					}
				}
			} catch (SQLException ignored) {
			} finally {
				try {
					if ( stm != null ) {
						stm.close();
					}
					if ( conn != null ) {
						conn.close();
					}
				} catch (SQLException ignored) {
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player p = event.getPlayer();
		Connection conn = Core.getConnection();
		Statement stm = null;
		
		try {
			if (conn != null) {
				stm = conn.createStatement();
				String playerNick = p.getName().toLowerCase().replaceAll("'", "\"");
				
				ResultSet result = stm.executeQuery("SELECT * FROM `" + Core.pl.getConfig().getString("MySQL.table") + "` WHERE `nick` = '" + playerNick + "' and `status` = 1 ORDER BY  `banfrom` DESC");
				if ( result.next() ) {
					int currentBan = result.getInt("banto");
					long currentTime = System.currentTimeMillis() / 1000L;
					
					if ( currentBan == 0 ) {
						event.disallow(
								Result.KICK_OTHER, 
								"§cVocê está banido do servidor" +
								"\n§cPor: " + result.getString("adminNick") +
								"\n§cMotivo: " + result.getString("reason") +
								"\n§cTempo: Permanente"
						);
					} else {
						if ( (long) currentBan > currentTime ) {
							event.disallow(
									Result.KICK_OTHER, 
									"§cVocê está banido do servidor" +
									"\n§cPor: " + result.getString("adminNick") +
									"\n§cMotivo: " + result.getString("reason") +
									"\n§cTempo restante: " + ((long) currentBan - currentTime) / 60L + 1L + " minuto(s)"
							);
						} else {
							PreparedStatement pstmt = conn.prepareStatement("UPDATE " + Core.pl.getConfig().getString("MySQL.table") + " SET status = 0 WHERE id=?");
							pstmt.setInt(1, result.getInt("id"));
							pstmt.executeUpdate();
							pstmt.close();
							playersUnban.add(p);
						}
					}
				}
			}
		} catch (SQLException ignored) {
		} finally {
			try {
				if ( stm != null ) {
					stm.close();
				}
				if ( conn != null ) {
					conn.close();
				}
			} catch (SQLException ignored) {
			}
		}
	}  
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onTell(PrivateMessageEvent e) {
		if ( Legendchat.getMuteManager().isPlayerMuted(e.getSender().getName()) ) {
			int time = Legendchat.getMuteManager().getPlayerMuteTimeLeft(e.getSender().getName());
			e.getSender().sendMessage(Legendchat.getMessageManager().getMessage("mute_error5").replace("@time", Integer.toString(time)));
			e.setCancelled(true);
		}
	}
}
