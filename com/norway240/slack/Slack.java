package com.norway240.slack; 

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Slack extends JavaPlugin implements Listener {
	
	public static Comms comm;
	File configFile;
    FileConfiguration config;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		Plugin plugin = Bukkit.getPluginManager().getPlugin("SlackChat");
		configFile = new File(plugin.getDataFolder(), "config.yml");
		
		try {
	        firstRun();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    config = new YamlConfiguration();
	    loadYamls();
		
	    comm.receive();
		getLogger().info("SlackChat enabled! (a plugin by norway240)");
	}

	public void onDisable() {
		comm.close();
		getLogger().info("SlackChat disabled! (a plugin by norway240)");
	}
	
	private void firstRun(){
		if(!configFile.exists()){
	        configFile.getParentFile().mkdirs();
	        copy(getResource("config.yml"), configFile);
	    }
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void loadYamls() {
	    try {
	        config.load(configFile);
		    
		    String w = config.getString("webhook");
		    if(w.contains("CONFIGURETHIS")){
		    	getServer().broadcast("[SlackChat] SlackChat must be configured", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
		    }else{
			    String v = config.getString("verification");
			    int p = config.getInt("port");
			    
				comm = new Comms(w, v, p);
		    }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

    String msg = "", name = "";
    
	@EventHandler
	public void playerChat(AsyncPlayerChatEvent  event){
	    msg = event.getMessage();
	    name = event.getPlayer().getName();
	    new Thread(new Runnable() {
	        public void run(){
			    try{
					comm.send(name, msg);
				}catch(IOException e){
					e.printStackTrace();
				}
	        }
	    }).start();
	}
	//Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), mv modify 1);
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("slackchat")){
			loadYamls();
			sender.sendMessage("[SlackChat] Configuration reloaded");
			return true;
		}else if(cmd.getName().equalsIgnoreCase("staff")){
			sender.sendMessage("Staff not online? No need to fear!");
			sender.sendMessage("Say a staff member's name to contact them even if they are offline!");
			sender.sendMessage("For Example: \"@norway240\" (may differ from in game name)");
			sender.sendMessage("Here are the staff members of which you can get their attention:");
			sender.sendMessage("@norway240");
			sender.sendMessage("@mailmanq");
			return true;
		}
		return false;
	}
	
}