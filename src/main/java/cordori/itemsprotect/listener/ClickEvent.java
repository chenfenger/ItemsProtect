package cordori.itemsprotect.listener;

import cordori.itemsprotect.ItemsProtect;
import cordori.itemsprotect.file.ConfigFile;
import cordori.itemsprotect.utils.ItemsInv;
import cordori.itemsprotect.utils.SqlStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class ClickEvent implements Listener {
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        //如果不是玩家点击，终止
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        if(ConfigFile.debug) {
            boolean result = ItemsInv.isInv(e.getInventory());
            System.out.println("打开的界面是否是ItemsProtect的界面: " + result);
        }
        //如果界面符合
        if(ItemsInv.isInv(e.getInventory())) {
            Player p = (Player) e.getWhoClicked();
            //如果点击的不是GUI内的槽位则不处理
            if(e.getRawSlot() < 0 || e.getRawSlot() > 53) {
                return;
            }
            //禁止往GUI里放物品
            if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR || e.getAction() == InventoryAction.PLACE_ALL
                    || e.getAction() == InventoryAction.PLACE_ONE || e.getAction() == InventoryAction.PLACE_SOME) {
                e.setCancelled(true);
            }
            //如果点击的是上一页槽位并且当前页数大于1,禁止拿出物品，打开上一页,更新items集合
            if(e.getCurrentItem() != null && e.getRawSlot() == 46 && ItemsInv.currentPage > 1) {
                e.setCancelled(true);
                // 更新 items.yml 的内容
                if(ConfigFile.saveMode.equalsIgnoreCase("yaml")) {
                    ConfigFile.saveDespawnItems(ItemsProtect.getInstance());
                } else {
                    SqlStorage sql = new SqlStorage(ItemsProtect.getInstance());
                    sql.saveDespawnItemsToMySQL();
                }
                ItemsInv.openInv(p, ItemsInv.currentPage - 1);
            }
            //如果点击的是关闭槽位,禁止拿出物品，关闭界面,更新items.yml
            if(e.getCurrentItem() != null && e.getRawSlot() == 49) {
                e.setCancelled(true);
                // 更新 items.yml 的内容
                if(ConfigFile.saveMode.equalsIgnoreCase("yaml")) {
                    ConfigFile.saveDespawnItems(ItemsProtect.getInstance());
                } else {
                    SqlStorage sql = new SqlStorage(ItemsProtect.getInstance());
                    sql.saveDespawnItemsToMySQL();
                }
                p.closeInventory();
            }
            //如果点击的是下一页槽位,禁止拿出物品，打开下一页,更新items集合
            if(e.getCurrentItem() != null && e.getRawSlot() == 52 && ItemsInv.currentPage < (int) Math.ceil(ConfigFile.despawnItems.size() / 45.0)) {
                e.setCancelled(true);
                // 更新 items.yml 的内容
                if(ConfigFile.saveMode.equalsIgnoreCase("yaml")) {
                    ConfigFile.saveDespawnItems(ItemsProtect.getInstance());
                } else {
                    SqlStorage sql = new SqlStorage(ItemsProtect.getInstance());
                    sql.saveDespawnItemsToMySQL();
                }
                ItemsInv.openInv(p, ItemsInv.currentPage + 1);
            }
            //如果点击的是其他物品槽
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getRawSlot() != 46 && e.getRawSlot() != 49 && e.getRawSlot() != 52) {
                if(ConfigFile.debug) {
                    for (ItemStack item : ConfigFile.despawnItems) {
                        System.out.println("§a despawnItems修改前的内容为: " + item);
                    }
                    System.out.println("§b despawnItems修改前的长度为: " + ConfigFile.despawnItems.size());
                    boolean result = ConfigFile.despawnItems.contains(e.getCurrentItem());
                    System.out.println("本次移除的物品为:" + e.getCurrentItem());
                    System.out.println("§c集合中是否包含:" + result);
                }
                ConfigFile.despawnItems.remove(e.getCurrentItem());
                if(ConfigFile.debug) {
                    for (ItemStack item : ConfigFile.despawnItems) {
                        System.out.println("§e despawnItems修改后的内容为: " + item);
                    }
                    System.out.println("§6 despawnItems修改后的长度为: " + ConfigFile.despawnItems.size());
                }
            }
        }
    }
    //关闭GUI界面时更新一次items集合
    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        // 如果关闭的是 ItemsProtect 的界面
        if (ItemsInv.isInv(e.getInventory())) {
            Player p = (Player) e.getPlayer();
            p.sendMessage(ConfigFile.prefix + ItemsProtect.getInstance().getConfig().getString("messages.close").replaceAll("&", "§"));
            // 更新 items.yml 的内容
            if(ConfigFile.saveMode.equalsIgnoreCase("yaml")) {
                ConfigFile.saveDespawnItems(ItemsProtect.getInstance());
            } else {
                ConfigFile.sql.saveDespawnItemsToMySQL();
            }
        }
    }
}
