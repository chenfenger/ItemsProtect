package cordori.itemsprotect.file;

import cordori.itemsprotect.ItemsProtect;
import cordori.itemsprotect.utils.SqlStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigFile {
    public static boolean debug;
    public static String saveMode;
    public static String prefix;
    public static String title;
    public static boolean loreProtect;
    public static boolean loreDrop;
    public static boolean loreCollect;
    public static String lore;
    public static boolean VOID;
    public static boolean LAVA;
    public static boolean FIRE_TICK;
    public static boolean ENTITY_EXPLOSION;
    public static boolean BLOCK_EXPLOSION;
    public static boolean CONTACT;
    public static HashSet<Material> protectItems = new HashSet<>();
    public static List<ItemStack> despawnItems = new ArrayList<>();
    public static SqlStorage sql = new SqlStorage(ItemsProtect.getInstance());

    //重载config配置
    public static void loadConfig() {
        ItemsProtect ip = ItemsProtect.getInstance();
        ip.reloadConfig();
        checkConfig(ip);
        loadProtectItems(ip);
        debug = ip.getConfig().getBoolean("debug");
        saveMode = ip.getConfig().getString("saveMode");
        prefix = ip.getConfig().getString("prefix").replaceAll("&", "§");
        title = ip.getConfig().getString("title").replaceAll("&", "§");
        loreProtect = ip.getConfig().getBoolean("loreProtect");
        loreCollect = ip.getConfig().getBoolean("loreCollect");
        loreDrop = ip.getConfig().getBoolean("loreDrop");
        lore = ip.getConfig().getString("lore", "此物品已被保护").replaceAll("&", "§");
        VOID = ip.getConfig().getBoolean("protectTypes.VOID");
        LAVA = ip.getConfig().getBoolean("protectTypes.LAVA");
        FIRE_TICK = ip.getConfig().getBoolean("protectTypes.FIRE_TICK");
        ENTITY_EXPLOSION = ip.getConfig().getBoolean("protectTypes.ENTITY_EXPLOSION");
        BLOCK_EXPLOSION = ip.getConfig().getBoolean("protectTypes.BLOCK_EXPLOSION");
        CONTACT = ip.getConfig().getBoolean("protectTypes.CONTACT");
        if (saveMode.equalsIgnoreCase("yaml")) {
            loadDespawnItems(ip);
        } else {
            sql.loadDespawnItemsFromMySQL(items -> {
                // 处理物品列表
                ConfigFile.despawnItems = items;
            });
        }

    }

    // 检查并更新配置文件
    public static void checkConfig(ItemsProtect ip) {
        File configFile = new File(ip.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            // 配置文件存在，读取配置
            ip.getConfig().options().copyDefaults(true);
            ip.saveConfig();
        } else {
            // 配置文件不存在，复制默认配置到插件目录
            ip.saveDefaultConfig();
        }
    }

    //从config配置文件中获取保护物品列表
    public static void loadProtectItems(ItemsProtect ip) {
        List<String> itemNames = ip.getConfig().getStringList("protectItems");
        //判断物品是否符合bukkit材料名称，是则添加到集合中
        HashSet<Material> protectItemsTemp = new HashSet<>();
        for (String itemName : itemNames) {
            Material material = Material.matchMaterial(itemName);
            if (material != null) {
                protectItemsTemp.add(material);
            }
        }
        protectItems = protectItemsTemp;
        //调试
        if(debug) {
            System.out.println("protectItems的内容是: " + protectItems);
        }
    }

    //将保护物品的集合保存至保护列表中
    public static void saveProtectItems(ItemsProtect ip) {
        File configFile = new File(ip.getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.set("protectItems", new ArrayList<>(protectItems).stream().map(Material::toString).collect(Collectors.toList()));
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ip.reloadConfig();
    }

    public static void loadDespawnItems(ItemsProtect ip) {
        Bukkit.getScheduler().runTaskAsynchronously(ip, () -> {
            File file = new File(ip.getDataFolder(), "items.yml");
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
                List<?> itemList = config.getList("Items");
                if (itemList != null) {
                    despawnItems = itemList.stream()
                            .filter(item -> item instanceof ItemStack)
                            .map(item -> (ItemStack) item)
                            .collect(Collectors.toList());
                }
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        });
    }

    public static void saveDespawnItems(ItemsProtect ip) {
        Bukkit.getScheduler().runTaskAsynchronously(ip, () -> {
            YamlConfiguration config = new YamlConfiguration();
            config.set("Items", despawnItems);
            try {
                config.save(new File(ip.getDataFolder(),"items.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
