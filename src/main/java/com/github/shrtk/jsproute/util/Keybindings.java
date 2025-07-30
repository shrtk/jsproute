package com.github.shrtk.jsproute.util;

import com.github.shrtk.jsproute.ModMain;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard; // キーコードのため

public class Keybindings {

    public static KeyBinding scanRoomKey;

    public static void init() {
        // キーバインドの初期化
        // 第1引数: 表示名 (Minecraftのコントロール設定画面に表示される)
        // 第2引数: デフォルトのキー (Keyboard.KEY_ で指定)
        // 第3引数: カテゴリ名 (Modごとにまとめるため)
        scanRoomKey = new KeyBinding("key.jsperoute.scanroom", Keyboard.KEY_X, "key.categories.jsperoute");

        // キーバインドの登録
        ClientRegistry.registerKeyBinding(scanRoomKey);
    }
}