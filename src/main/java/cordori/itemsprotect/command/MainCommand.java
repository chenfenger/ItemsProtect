package cordori.itemsprotect.command;

import cordori.itemsprotect.file.ConfigFile;
import cordori.itemsprotect.ItemsProtect;
import cordori.itemsprotect.utils.ItemsInv;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class MainCommand implements CommandExecutor, TabCompleter {
    //补全指令
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help");
            completions.add("reload");
            completions.add("add");
            completions.add("open");
        }
        return completions;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ItemsProtect ip = ItemsProtect.getInstance();
        String prefix = ConfigFile.prefix;
        if(args.length == 0) {
            sender.sendMessage(prefix + "§6====================");
            sender.sendMessage(prefix + "§a help - 插件指令与描述");
            sender.sendMessage(prefix + "§a reload - 重载插件配置");
            sender.sendMessage(prefix + "§a add - 将手持物品添加到保护列表");
            sender.sendMessage(prefix + "§a open - 打开物品寻回界面");
            sender.sendMessage(prefix + "§6====================");
            return false;
        }

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(prefix + "§6====================");
                sender.sendMessage(prefix + "§a help - 插件指令与描述");
                sender.sendMessage(prefix + "§a reload - 重载插件配置");
                sender.sendMessage(prefix + "§a add - 将手持物品添加到保护列表");
                sender.sendMessage(prefix + "§a open - 打开物品寻回界面");
                sender.sendMessage(prefix + "§6====================");
                return false;
            }

            if(args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(prefix + ip.getConfig().getString("messages.reload").replaceAll("&", "§"));
                ConfigFile.loadConfig();
                if(sender instanceof Player) {
                    Player p = ((Player) sender).getPlayer();
                    p.closeInventory();
                }
                return false;
            }

            if(args[0].equalsIgnoreCase("add")) {
                if(sender instanceof Player) {
                    Player p = ((Player) sender).getPlayer();
                    if(p.getInventory().getItemInMainHand() != null && !p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                            Material material = p.getInventory().getItemInMainHand().getType();
                            if (ConfigFile.protectItems.contains(material)) {
                                sender.sendMessage(prefix + ip.getConfig().getString("messages.onProtect").replaceAll("&", "§"));
                            } else {
                                ConfigFile.protectItems.add(material);
                                ConfigFile.saveProtectItems(ip);
                                sender.sendMessage(prefix + ip.getConfig().getString("messages.addItem").replaceAll("&", "§").replace("%item%", p.getInventory().getItemInMainHand().getType().toString()));
                            }
                    } else {
                        p.sendMessage(prefix + ip.getConfig().getString("messages.noneItem").replaceAll("&", "§"));
                    }
                } else {
                    sender.sendMessage(prefix + ip.getConfig().getString("messages.deny").replaceAll("&", "§"));
                }
                return false;
            }

            if(args[0].equalsIgnoreCase("open")) {
                if (sender instanceof Player) {
                    Player p = ((Player) sender).getPlayer();
                    ItemsInv.openInv(p, 1);
                    p.sendMessage(prefix + ItemsProtect.getInstance().getConfig().getString("messages.open").replaceAll("&", "§"));
                    return false;
                } else {
                    sender.sendMessage(prefix + ip.getConfig().getString("messages.deny").replaceAll("&", "§"));
                }
            }
        }
        return false;
    }
}
