package tv.mineinthebox.simplekit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class KitPlugin extends JavaPlugin implements Listener {
	
	private HashMap<String, Kit> kits;
	private String InvTitle = ChatColor.RED + "kit selector";
	
	public void onEnable() {
		setLog("has been enabled!", LogType.INFO);
		createConfig();
		
		//parsing kits
		this.kits = new HashMap<String, Kit>();
		File f = new File(getDataFolder() + File.separator + "kits.yml");
		FileConfiguration con = YamlConfiguration.loadConfiguration(f);
		Kit[] kits = parseKits(con);
		for(Kit kit : kits) {
			System.out.print("kit name: " + kit.getKitName());
			this.kits.put(kit.getKitName(), kit);
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
		setLog("has been disabled!", LogType.INFO);
	}
	

	/**
	 * @author xize
	 * @param message - the message which get logged
	 * @param type - the LogType enum
	 */
	public void setLog(String message, LogType type) {
		String prefix = ChatColor.GREEN + "[" + ChatColor.GRAY + "SimpleKits" + ChatColor.GREEN + "]" + ChatColor.GRAY + ": ";
		if(type == LogType.INFO) {
			Bukkit.getConsoleSender().sendMessage(prefix + message);
		} else if(type == LogType.SEVERE) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Severe]" + prefix + message);
		}
	}
	
	/**
	 * @author xize
	 * @param creates all the kits in the config
	 */
	@SuppressWarnings("deprecation")
	private void createConfig() {
		try {
			File f = new File(getDataFolder() + File.separator + "kits.yml");
			if(!f.exists()) {
				FileConfiguration con = YamlConfiguration.loadConfiguration(f);
				String[] DiamondKit = {Material.DIAMOND_PICKAXE.getId()+":0:1", Material.DIAMOND_SPADE.name()+":0:1", Material.DIAMOND_AXE.name()+":0:1", Material.DIAMOND_SWORD+":0:1", Material.MELON+":0:30"};
				String[] IronKit = {Material.IRON_PICKAXE.getId()+":0:1", Material.IRON_SPADE.name()+":0:1", Material.IRON_AXE.name()+":0:1", Material.IRON_SWORD+":0:1", Material.MELON+":0:30"};
				String[] WoodKit = {Material.WOOD_PICKAXE.getId()+":0:1", Material.WOOD_SPADE.name()+":0:1", Material.WOOD_AXE.name()+":0:1", Material.WOOD_SWORD+":0:1", Material.MELON+":0:30"};
				con.set("cooldown.isEnabled", true);
				con.set("cooldown.time", 100);
				con.set("kit.diamondkit", DiamondKit);
				con.set("kit.ironkit", IronKit);
				con.set("kit.woodkit", WoodKit);
				con.save(f);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @author xize
	 * @param parse the kits from the kits.yml config
	 * @return Kit[]
	 */
	@SuppressWarnings("deprecation")
	private Kit[] parseKits(FileConfiguration con) {
		List<Kit> kits = new ArrayList<Kit>();
		for(String path : con.getConfigurationSection("kit").getKeys(true)) {
			String kitname = path;
			List<ItemStack> stacks = new ArrayList<ItemStack>();
			List<String> items = new ArrayList<String>(con.getStringList("kit."+kitname));
			for(String item : items) {
				String[] split = item.split(":");
				if(isNumberic(split[0])) {
					Material mat = Material.getMaterial(Integer.parseInt(split[0]));
					Short subdata = Short.parseShort(split[1]);
					int amount = Integer.parseInt(split[2]);
					ItemStack stack = new ItemStack(mat, amount);
					stack.setDurability(subdata);
					stacks.add(stack);
				} else {
					Material mat = Material.getMaterial(split[0].toUpperCase());
					Short subdata = Short.parseShort(split[1]);
					int amount = Integer.parseInt(split[2]);
					ItemStack stack = new ItemStack(mat, amount);
					stack.setDurability(subdata);
					stacks.add(stack);
				}
			}
			Kit kit = new Kit(kitname, stacks.toArray(new ItemStack[stacks.size()]));
			kits.add(kit);
		}
		return kits.toArray(new Kit[kits.size()]);
	}
	
	/**
	 * @author xize
	 * @param arg - the String which may is a number
	 * @return Boolean
	 */
	private Boolean isNumberic(String arg) {
		try {
			Integer i = Integer.parseInt(arg);
			if(i != null) {
				return true;
			}
		} catch(NumberFormatException e) {
			return false;
		}
		return false;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("kits")) {
			Player p = (Player) sender;
			if(sender instanceof Player) {
				Inventory inv = Bukkit.createInventory(null, 36, InvTitle);
				for(Kit kit : kits.values()) {
					ItemStack item = new ItemStack(kit.getKitItems()[0]);
					item.setAmount(1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(kit.getKitName());
					item.setItemMeta(meta);
					inv.addItem(item);
				}
				
				p.getWorld().playSound(p.getLocation(), Sound.CHEST_OPEN, 1F, 1F);
				p.sendMessage(ChatColor.GREEN + "opening kit selector!");
				p.openInventory(inv);
			} else {
				sender.sendMessage(ChatColor.RED + "an console cannot open a inventory!");
			}
		}
		return false;
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryCloseEvent e) {
		if(e.getInventory().getTitle().equalsIgnoreCase(InvTitle)) {
			Player p = (Player) e.getPlayer(); 
			p.getPlayer().getWorld().playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
			p.sendMessage(ChatColor.GREEN + "closing kit selector!");
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory().getTitle().equalsIgnoreCase(InvTitle)) {
			if(e.getCurrentItem() != null && e.getCurrentItem() instanceof ItemStack) {
				if(e.getCurrentItem().hasItemMeta()) {
					if(e.getCurrentItem().getItemMeta().hasDisplayName()) {
						if(kits.containsKey(e.getCurrentItem().getItemMeta().getDisplayName())) {
							Player p = (Player) e.getWhoClicked();
							Kit kit = kits.get(e.getCurrentItem().getItemMeta().getDisplayName());
							for(ItemStack stack : kit.getKitItems()) {
								p.getInventory().addItem(stack);
							}
							p.closeInventory();
							p.sendMessage(ChatColor.GREEN + "you successfully received the " + kit.getKitName() + "!");
						} else {
							Player p = (Player) e.getWhoClicked();
							p.sendMessage(ChatColor.RED + "invalid item, to click on!");
						}
					}
				}
			}
			e.setCancelled(true);
		}
	}

}
