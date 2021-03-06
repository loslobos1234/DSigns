package mineapi.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.block.Sign;

public class StatusSign {

	private Location location;
	private Sign sign;
	private String name, ip;
	private int port;
	
	public StatusSign(Location location, String name, String ip, int port){
		this.location = location;
		this.sign = (Sign) location.getBlock().getState();
		this.name = name;
		this.ip = ip;
		this.port = port;
	}
	
	public String getName(){
		return name;
	}
	
	public Location getLocation(){
		return location;
	}
	
	public String getIP() {
		return ip;
	}
	
	public int getPort(){
		return port;
	}
	
	public void update(){
		try{
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), 1 * 1000);
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
			out.write(0xFE);
			
			StringBuilder str = new StringBuilder();
			
			int b;
			
			while ((b = in.read()) != -1){
				if(b != 0 && b > 16 && b != 255 && b != 23 && b!=24){
					str.append((char)b);
				}
			}
			
			String[] data = str.toString().split("§");
		
			// Order: MOTD = [0], online players = [1], maximum players = [2]
			
			
			int onlinePlayers = Integer.valueOf(data[1]);
			int maxPlayers = Integer.valueOf(data[2]);
		
			sign.setLine(0, ChatColor.GREEN + name);
			sign.setLine(1, ChatColor.BLACK + ip);
			sign.setLine(2, ChatColor.RED + String.valueOf(onlinePlayers) + ChatColor.BLACK + "/" + ChatColor.RED + String.valueOf(maxPlayers));
			sign.setLine(3,ChatColor.GOLD + "Status: " + ChatColor.GREEN + "Online.");
			
			socket.close();
		}catch(Exception e){
			e.printStackTrace();
			
			sign.setLine(0, ChatColor.RED + name);
			sign.setLine(1, ChatColor.BLACK + ip);
			sign.setLine(2, ChatColor.RED + "N/A");
			sign.setLine(3, ChatColor.GOLD + "Status: " + ChatColor.RED + "Offline");
		}
		
		sign.update();
	}
}
