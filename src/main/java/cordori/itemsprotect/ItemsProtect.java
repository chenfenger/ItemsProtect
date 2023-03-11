package cordori.itemsprotect;

import cordori.itemsprotect.command.MainCommand;
import cordori.itemsprotect.file.ConfigFile;
import cordori.itemsprotect.listener.ClickEvent;
import cordori.itemsprotect.listener.ItemsEvent;
import cordori.itemsprotect.utils.SqlStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class ItemsProtect extends JavaPlugin {
    public static ItemsProtect Instance;

    public static ItemsProtect getInstance() {
        return Instance;
    }

    @Override
    public void onEnable() {
        Instance = this;
        //检测是否存在插件文件夹，如果没有则进行创建
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        //检查存储模式，如果是YAML则创建items.yml文件，如果是MySQL则跳过创建文件
        if (getConfig().getString("saveMode").equalsIgnoreCase("yaml")) {
            createItemsFile();
        } else {
            // 实例化SqlStorage并传入插件实例
            SqlStorage sql = new SqlStorage(this);
            sql.saveDespawnItemsToMySQL();
        }
        //保存默认配置并加载数据
        saveDefaultConfig();
        ConfigFile.loadConfig();
        //加一个测试物品
        ConfigFile.despawnItems.add(new ItemStack(Material.DIAMOND));
        //注册指令
        Bukkit.getPluginCommand("itemsprotect").setExecutor(new MainCommand());
        //注册事件监听器
        Bukkit.getPluginManager().registerEvents(new ItemsEvent(), this);
        Bukkit.getPluginManager().registerEvents(new ClickEvent(), this);
        //后台发送信息
        getLogger().info("§a 成功加载 [ ItemsProtect ] ");
        getLogger().info("§a 插件作者 [ Cordori ] ");
    }

    @Override
    public void onDisable() {
        // 关闭所有玩家的界面
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        getLogger().info("§c[ ItemsProtect ] 已卸载");
    }

    public void createItemsFile() {
        File file = new File(this.getDataFolder(), "items.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
