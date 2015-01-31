package mineapi.api;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener{
	
	private ArrayList<StatusSign> signs;
	
	@Override
	public void onEnable(){
		this.signs = new ArrayList<StatusSign>();
		saveDefaultConfig();
		
		for(String str : getConfig().getKeys(false)){
			ConfigurationSection s = getConfig().getConfigurationSection(str);
			
			ConfigurationSection l = s.getConfigurationSection("loc");
			World w = Bukkit.getWorld(l.getString("world"));
			double x = l.getDouble("x"),y = l.getDouble("y"),z = l.getDouble("z");
			Location loc = new Location(w,x,y,z);
		
			if(loc.getBlock() == null){
				getConfig().set(str, null);
			}else{
				signs.add(new StatusSign(loc, s.getString("name"), s.getString("ip"), s.getInt("port")));
			}
		}
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			
			public void run(){
				for(StatusSign s : signs){
					s.update();
				}
			}
			
		}, 0, 20);
		
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block block = e.getClickedBlock();
		if(block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) return;
	
		for(StatusSign s : signs){
			if(s.getLocation().equals(block.getLocation())){
				try{
					
					ByteArrayOutputStream b = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(b);
					
					out.writeUTF("Connect");
					out.writeUTF(s.getName());
					
					Bukkit.getServer().sendPluginMessage(this, "BungeeCord", b.toByteArray());
				}catch(Exception e1){
					e1.printStackTrace();
				}
			}
		}
	
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "Only players can create status signs!");
			return true;
		}
		
		Player p = (Player) sender;
		
		if(commandLabel.equalsIgnoreCase("dsigns")){
			if(!(p.hasPermission("dsigns.create"))){
				p.sendMessage(ChatColor.RED + "No permission!");
				return true;
			}
			if(args.length < 3){
				p.sendMessage(ChatColor.RED + "/dsigns <ip> <port> <name>");
				return true;
			}
			
			String ip = args[0];
			int port;
			String name = args[2];
			
			try{
				port = Integer.valueOf(args[1]);
			}catch(Exception e){
				p.sendMessage(ChatColor.RED + "Port is not a number.");
				return true;
			}
			
			Block block = p.getTargetBlock(null, 10);
			if(block == null){
				p.sendMessage(ChatColor.RED + "You are not looking at a sign!");
				return true;
			}
			
			if(block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN){
				p.sendMessage(ChatColor.RED + "You are not looking at a sign!");
				return true;
			}
			
			StatusSign statussign = new StatusSign(block.getLocation(), name, ip, port);
			signs.add(statussign);
			save(statussign);
		}
		
		return true;
	}
	
	private void save(StatusSign sign){
		int size = getConfig().getKeys(false).size() + 1;
		
		getConfig().set(size + ".loc.world", sign.getLocation().getWorld().getName());
		getConfig().set(size + ".loc.x", sign.getLocation().getX());
		getConfig().set(size + ".loc.y", sign.getLocation().getY());
		getConfig().set(size + ".loc.z", sign.getLocation().getZ());
		getConfig().set(size + ".name", sign.getName());
		getConfig().set(size + ".ip", sign.getIP());
		getConfig().set(size + ".port", sign.getPort());
	
		saveConfig();
	}

}