package cordori.itemsprotect.utils;

import cordori.itemsprotect.ItemsProtect;
import cordori.itemsprotect.file.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlStorage {
    private final ItemsProtect plugin;
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    public SqlStorage(ItemsProtect plugin) {
        this.plugin = ItemsProtect.getInstance();
        this.host = plugin.getConfig().getString("MySQL.host");
        this.port = plugin.getConfig().getString("MySQL.port").toLowerCase();
        this.database = plugin.getConfig().getString("MySQL.database");
        this.username = plugin.getConfig().getString("MySQL.username");
        this.password = plugin.getConfig().getString("MySQL.password");
    }

    public void saveDespawnItemsToMySQL() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                //连接到MySQL数据库
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
                //创建表
                pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS despawn_items (id INT NOT NULL AUTO_INCREMENT, item_data BLOB, PRIMARY KEY (id))");
                pstmt.executeUpdate();

                //插入每个ItemStack
                for (ItemStack itemStack : ConfigFile.despawnItems) {
                    pstmt = conn.prepareStatement("INSERT INTO despawn_items (item_data) VALUES (?)");
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                    dataOutput.writeObject(itemStack);
                    dataOutput.flush();
                    dataOutput.close();
                    pstmt.setBytes(1, outputStream.toByteArray());
                    pstmt.executeUpdate();
                }
            } catch (SQLException | ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                //关闭连接
                try {
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadDespawnItemsFromMySQL(Consumer<List<ItemStack>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<ItemStack> itemList = new ArrayList<>();
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                //连接到MySQL数据库
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
                //从表中检索每个ItemStack
                pstmt = conn.prepareStatement("SELECT item_data FROM despawn_items");
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    byte[] bytes = rs.getBytes("item_data");
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                    ItemStack itemStack = (ItemStack) dataInput.readObject();
                    itemList.add(itemStack);
                }
                //将检索到的ItemStacks赋值给ConfigFile中的despawnItems
                ConfigFile.despawnItems = itemList;
                //执行回调函数
                callback.accept(itemList);
            } catch (SQLException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                //关闭连接和资源
                try {
                    if (rs != null) rs.close();
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void checkItemList() {
        SqlStorage sqlStorage = new SqlStorage(plugin);
        // 调用异步加载方法，并在回调函数中处理返回的 itemList
        sqlStorage.loadDespawnItemsFromMySQL((itemList) -> {
            // 判断 itemList 是否为空
            if (itemList.isEmpty()) {
                // 如果为空，说明没有从数据库中加载到任何物品
                plugin.getLogger().info("未从数据库中加载到任何物品");
            } else {
                // 如果不为空，遍历 itemList 并打印每个物品的类型和数量
                for (ItemStack itemStack : itemList) {
                    plugin.getLogger().info(itemStack.getType().toString() + " x " + itemStack.getAmount());
                }
            }
        });
    }
}