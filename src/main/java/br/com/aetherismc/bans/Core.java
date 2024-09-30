package br.com.aetherismc.bans;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import br.com.aetherismc.bans.commands.CommandBan;
import br.com.aetherismc.bans.commands.CommandBanIP;
import br.com.aetherismc.bans.commands.CommandDesmutar;
import br.com.aetherismc.bans.commands.CommandKick;
import br.com.aetherismc.bans.commands.CommandMutar;
import br.com.aetherismc.bans.commands.CommandUnban;
import br.com.aetherismc.bans.commands.CommandUnbanIP;
import br.com.aetherismc.bans.execute.ConnectionPool;
import br.com.aetherismc.bans.execute.PlayerListener;

public class Core extends JavaPlugin {

    public static String prefixPerm;
    private static ConnectionPool pool;
    private static Configuration config;
    public static Plugin pl;

    public void onEnable() {

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getCommand("ban").setExecutor(new CommandBan());
        this.getCommand("banip").setExecutor(new CommandBanIP());
        this.getCommand("kick").setExecutor(new CommandKick());
        this.getCommand("unban").setExecutor(new CommandUnban());
        this.getCommand("unbanip").setExecutor(new CommandUnbanIP());
        this.getCommand("mutar").setExecutor(new CommandMutar());
        this.getCommand("desmutar").setExecutor(new CommandDesmutar());


        PluginDescriptionFile pdfFile = this.getDescription();
        this.loadConfig();
        pl = this;
        prefixPerm = "aetherismc.bans";

        try {
            pool = new ConnectionPool("jdbc:mysql://" + this.getConfig().getString("MySQL.Host") + ":" + this.getConfig().getString("MySQL.Port") + "/" + this.getConfig().getString("MySQL.Database"), this.getConfig().getString("MySQL.User"), this.getConfig().getString("MySQL.Password"));
            Connection conn = getConnection();
            assert conn != null;
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        if (!this.checkTables()) {
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        if ( new Float(config.getString("version", pdfFile.getVersion())) < new Float("1.1")) {
            Connection conn = getConnection();
            try {
                conn.createStatement().execute("ALTER TABLE `" + this.getConfig().getString("MySQL.table") + "` ADD COLUMN `unbanreason` VARCHAR(128) NULL  AFTER `status` , ADD COLUMN `unbannick` VARCHAR(64) NULL  AFTER `unbanreason`, ADD INDEX `Minecraftname` (`nick` ASC);");
                conn.close();

                this.getConfig().set("version", pdfFile.getVersion());
                this.saveConfig();
            } catch (SQLException e) {
                e.printStackTrace();
                this.getPluginLoader().disablePlugin(this);
                return;
            }
        }
    }

    public void onDisable() {
        if (pool != null) {
            pool.closeConnections();
        }
    }

    protected void loadConfig() {
        config = this.getConfig().getRoot();
        config.options().copyDefaults(true);
        this.saveConfig();
    }

    private boolean checkTables() {
        Connection conn = Core.getConnection();
        Statement stm = null;
        if (conn == null) {
            return false;
        } else {
            try {
                DatabaseMetaData dbm = conn.getMetaData();
                stm = conn.createStatement();
                if ( !dbm.getTables((String) null, (String) null, this.getConfig().getString("MySQL.table-history"), (String[]) null).next() ) {
                    stm.execute("CREATE TABLE  `" + this.getConfig().getString("MySQL.table-history") + "` (`id` INT( 10 ) NOT NULL AUTO_INCREMENT ,`name` VARCHAR( 32 ) NOT NULL ,`ip` VARCHAR( 35 ) NOT NULL ,PRIMARY KEY (  `id` ));");
                    if ( !dbm.getTables((String) null, (String) null, this.getConfig().getString("MySQL.table-history"), (String[]) null).next() ) {
                        return false;
                    }
                }

                if ( !dbm.getTables((String)null, (String) null, this.getConfig().getString("MySQL.table"), (String[]) null).next() ) {
                    stm.execute("CREATE TABLE  `" + this.getConfig().getString("MySQL.table") + "` (`id` INT( 10 ) NOT NULL AUTO_INCREMENT ,`nick` VARCHAR( 64 ) NOT NULL ,`adminnick` VARCHAR( 64 ) NOT NULL ,`ip` VARCHAR( 35 ) NOT NULL ,`banfrom` INT( 8 ) NOT NULL ,`banto` INT( 8 ) NOT NULL ,`reason` VARCHAR( 128 ) NOT NULL, `unbannick` VARCHAR(64) NULL, `unbanreason` VARCHAR(64) NULL, `status` INT( 1 ) NOT NULL ,PRIMARY KEY (  `id` ));");
                    return dbm.getTables((String) null, (String) null, this.getConfig().getString("MySQL.table"), (String[]) null).next();
                }

                return true;
            } catch (SQLException ignored) {
            } finally {
                try {
                    if ( stm != null ) {
                        stm.close();
                    }
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
            return false;
        }
    }

    public static boolean isInt(String i) {
        try {
            Integer.parseInt(i);
            return false;
        } catch (NumberFormatException var3) {
            return true;
        }
    }

    public static Connection getConnection() {
        try {
            return pool.getConnection();
        } catch (SQLException var2) {
            return null;
        } catch (NullPointerException var3) {
            Core.pl.getServer().getPluginManager().disablePlugin(Core.pl);
            return null;
        }
    }
}
