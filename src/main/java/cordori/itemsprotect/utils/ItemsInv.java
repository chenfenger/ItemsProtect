package cordori.itemsprotect.utils;

import cordori.itemsprotect.file.ConfigFile;
import cordori.itemsprotect.ItemsProtect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemsInv {
    public static int currentPage;
    private static Inventory inv;

    public static boolean isInv(Inventory currentInv) {
        return currentInv.equals(inv);
    }

    public static void openInv(Player player, int page) {

        currentPage = page;
        int maxPage = (int) Math.ceil(ConfigFile.despawnItems.size() / 45.0);

        List<ItemStack> items = ConfigFile.despawnItems;
        int start = (page - 1) * 45; // 计算当前页物品的起始下标
        int end = Math.min(start + 45, items.size()); // 计算当前页物品的结束下标

        if (end > items.size()) {
            end = items.size(); // 判断end是否超出items范围
        }


        int finalEnd = end;
        Bukkit.getScheduler().runTaskAsynchronously(ItemsProtect.getInstance(), () -> {
            // 添加物品到集合
            List<ItemStack> itemsToAdd = new ArrayList<>();
            for (int i = start; i < finalEnd; i++) {
                itemsToAdd.add(items.get(i));
            }


            Bukkit.getScheduler().runTask(ItemsProtect.getInstance(), () -> {
                inv = Bukkit.createInventory(player, 54, ConfigFile.title);
                for (int i = 0; i < itemsToAdd.size(); i++) {
                    inv.setItem(i, itemsToAdd.get(i)); // 添加物品到GUI界面
                }
                if (page > 1) {
                    inv.setItem(46, createPrevPageItem()); // 添加上一页按钮
                }
                if (page < maxPage) {
                    inv.setItem(52, createNextPageItem()); // 添加下一页按钮
                }
                inv.setItem(49, createCloseItem()); // 添加关闭按钮

                player.openInventory(inv);
            });
        });
    }

    public static ItemStack createPrevPageItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a上一页");
        meta.setLore(Collections.singletonList("§e当前页数: " + currentPage));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createNextPageItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a下一页");
        meta.setLore(Collections.singletonList("§e当前页数: " + currentPage));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c关闭界面");
        meta.setLore(Collections.singletonList("§e当前页数: " + currentPage));
        item.setItemMeta(meta);
        return item;
    }

}
