package com.github.shrtk.jsproute;

import com.github.shrtk.jsproute.command.CommandCopyRoute;
import com.github.shrtk.jsproute.event.RoomDetectionHandler;
import com.github.shrtk.jsproute.room.RoomRoute;
import com.github.shrtk.jsproute.room.RoomTemplate;
import com.github.shrtk.jsproute.util.Keybindings; // 追加
import com.github.shrtk.jsproute.util.RoomDataLoader;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = ModMain.MODID, version = ModMain.VERSION, clientSideOnly = true)
public class ModMain {
    public static final String MODID = "jsperoute";
    public static final String VERSION = "1.0.2";

    public static final Map<String, RoomTemplate> TEMPLATES = new HashMap<>();
    public static final Map<String, RoomRoute> ROUTES = new HashMap<>();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // コマンドの登録
        ClientCommandHandler.instance.registerCommand(new CommandCopyRoute());

        // イベントハンドラの登録 (TickEvent は RoomDetectionHandler から削除されるため、キー入力イベントに置き換えられる)
        MinecraftForge.EVENT_BUS.register(new RoomDetectionHandler()); // RoomDetectionHandler は引き続きイベントを登録する必要がある

        // キーバインドの登録
        Keybindings.init(); // 追加

        // アセットからの部屋データの読み込み
        RoomDataLoader.loadAllRoomData();
    }
}