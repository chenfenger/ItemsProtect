package cordori.itemsprotect.listener;

import cordori.itemsprotect.file.ConfigFile;
import cordori.itemsprotect.ItemsProtect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemsEvent implements Listener {
    boolean debug = ConfigFile.debug;
    String lore = ConfigFile.lore;
    boolean loreProtect = ConfigFile.loreProtect;
    String prefix = ConfigFile.prefix;
    String drop = ItemsProtect.getInstance().getConfig().getString("messages.drop").replaceAll("&", "§");

    //判断是否包含特定lore
    public boolean containLore(Item item) {
        if(item.getItemStack().hasItemMeta()) {
            if(item.getItemStack().getItemMeta().hasLore()) {
                //调试
                if(debug) {
                    System.out.println("containLore的结果是" + item.getItemStack().getItemMeta().getLore().contains(lore));
                }
                return item.getItemStack().getItemMeta().getLore().contains(lore);
            }
        }
        return false;
    }

    //判断是否属于保护列表的物品
    public boolean isProtectItem(Item item) {
        //调试
        if(debug) {
            System.out.println("protectItems的内容是: " + ConfigFile.protectItems);
            System.out.println("protectItems里是否包含事件中的物品: " + ConfigFile.protectItems.contains(item.getItemStack().getType()));
        }
        return ConfigFile.protectItems.contains(item.getItemStack().getType());
    }

    //物品消失事件
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        Item item = e.getEntity();
        Material material = item.getItemStack().getType();
        if(debug) {
            System.out.println("ItemDespawnEvent中的material为：" + material);
            System.out.println("loreCollect的结果是：" + ConfigFile.loreCollect);
        }
        //如果开启了loreCollect,只添加含特定lore的物品
        if(ConfigFile.loreCollect) {
            //开启了loreProtect并且消失的物品包含特定lore，将消失的物品添加到despawnItems集合
            if(loreProtect && containLore(item)) {
                ConfigFile.despawnItems.add(item.getItemStack());
                if (debug) {
                    System.out.println("保护了含特定lore的" + material + "免受虚无吞噬");
                }
            }
        } else {
            //如果消失的物品在列表内并且开启了VOID保护，将消失的物品添加到despawnItems集合
            if(ConfigFile.VOID && isProtectItem(item)) {
                ConfigFile.despawnItems.add(item.getItemStack());
                if(debug) {
                    System.out.println("保护了" + material + "免受虚无吞噬");
                }
            }
            //或者开启了loreProtect并且消失的物品包含特定lore，将消失的物品添加到despawnItems集合
            else if(loreProtect && containLore(item)) {
                ConfigFile.despawnItems.add(item.getItemStack());
                if(debug) {
                    System.out.println("保护了含特定lore的" + material + "免受虚无吞噬");
                }
            }
        }
    }

    @EventHandler
    public void onItemDamaged(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        //如果实体是物品类型
        if(entity instanceof Item) {
            Item item = (Item) e.getEntity();
            Material material = ((Item) entity).getItemStack().getType();
            if(debug) {
                System.out.println("EntityDamageEvent中的material为：" + material);
            }

            //如果是物品掉落岩浆
            if(e.getCause().equals(EntityDamageEvent.DamageCause.LAVA)) {
                //如果消失的物品在列表内并且开启了LAVA保护，取消本次事件
                if(ConfigFile.LAVA && isProtectItem(item)) {
                    e.setCancelled(true);
                    if(debug) {
                        System.out.println("保护了" + material + "免受岩浆破坏");
                    }
                }
                //或者开启了loreProtect并且消失的物品包含特定lore，取消本次事件
                else if(loreProtect && containLore(item)) {
                    e.setCancelled(true);
                    if(debug) {
                        System.out.println("保护了lore物品" + material + "免受岩浆破坏");
                    }
                }
            }

            //如果是物品被火焰灼烧破坏
            else if(e.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)) {
                //如果消失的物品在列表内并且开启了FIRE_TICK保护，取消本次事件
                if(ConfigFile.FIRE_TICK && isProtectItem(item)) {
                    e.setCancelled(true);
                    if(debug) {
                        System.out.println("保护了" + material + "免受火焰灼烧破坏");
                    }
                }
                //或者开启了loreProtect并且消失的物品包含特定lore，取消本次事件
                else if(loreProtect && containLore(item)) {
                    e.setCancelled(true);
                    if(debug) {
                        System.out.println("保护了lore物品" + material + "免受火焰灼烧破坏");
                    }
                }
            }

            //如果物品是被实体爆炸破坏
            else if(e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                //如果消失的物品在列表内并且开启了ENTITY_EXPLOSION保护，取消本次事件
                if(ConfigFile.ENTITY_EXPLOSION && isProtectItem(item)) {
                    e.setCancelled(true);
                    if (debug) {
                        System.out.println("保护了" + material + "免受实体爆炸破坏");
                    }
                }
                //或者开启了loreProtect并且消失的物品包含特定lore，取消本次事件
                else if(loreProtect && containLore(item)) {
                    e.setCancelled(true);
                    if(debug) {
                        System.out.println("保护了lore物品" + material + "免受实体爆炸破坏");
                    }
                }
            }

            //如果物品是被方块爆炸破坏
            else if(e.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
                //如果消失的物品在列表内并且开启了BLOCK_EXPLOSION保护，取消本次事件
                if(ConfigFile.BLOCK_EXPLOSION && isProtectItem(item)) {
                    e.setCancelled(true);
                    if (debug) {
                        System.out.println("保护了" + material + "免受方块爆炸破坏");
                    }
                }
                //或者开启了loreProtect并且消失的物品包含特定lore，取消本次事件
                else if(loreProtect && containLore(item)) {
                    e.setCancelled(true);
                    if (debug) {
                        System.out.println("保护了" + material + "免受方块爆炸破坏");
                    }
                }
            }

            //如果物品是被仙人掌破坏
            else if(e.getCause().equals(EntityDamageEvent.DamageCause.CONTACT)) {
                //如果消失的物品在列表内并且开启了CONTACT保护，取消本次事件
                if(ConfigFile.CONTACT && isProtectItem(item)) {
                    e.setCancelled(true);
                    if (debug) {
                        System.out.println("保护了" + material + "免受仙人掌破坏");
                    }
                }
                //或者开启了loreProtect并且消失的物品包含特定lore，取消本次事件
                else if(loreProtect && containLore(item)) {
                    e.setCancelled(true);
                    if (debug) {
                        System.out.println("保护了" + material + "免受仙人掌破坏");
                    }
                }
            }
        }
    }

    //物品丢弃事件
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Item item = e.getItemDrop();
        if(ConfigFile.loreDrop && containLore(item)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(prefix + drop);
        }
    }
}
