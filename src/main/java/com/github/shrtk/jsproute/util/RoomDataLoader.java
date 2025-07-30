package com.github.shrtk.jsproute.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.github.shrtk.jsproute.ModMain;
import com.github.shrtk.jsproute.room.BlockData;
import com.github.shrtk.jsproute.room.RoomRoute;
import com.github.shrtk.jsproute.room.RoomTemplate;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RoomDataLoader {

    /**
     * Modのアセットフォルダからすべての部屋のテンプレートとルートデータを読み込みます。
     * 開発環境とJAR環境の両方に対応しています。
     */
    public static void loadAllRoomData() {
        Gson gson = new Gson();
        ModContainer modContainer = Loader.instance().activeModContainer();

        if (modContainer == null) {
            System.err.println("RoomDataLoader: Mod container not found. Cannot load room data.");
            return;
        }

        File modFile = modContainer.getSource();
        if (modFile == null || !modFile.exists()) {
            System.err.println("RoomDataLoader: Mod file not found or not accessible. Cannot load room data.");
            return;
        }

        try {
            if (modFile.isDirectory()) {
                // 開発環境 (IDEから実行)
                File dataDir = new File(modFile, "assets/" + ModMain.MODID + "/data/");
                if (dataDir.exists() && dataDir.isDirectory()) {
                    loadFromDirectory(dataDir, gson);
                } else {
                    System.err.println("RoomDataLoader: Development data directory not found: " + dataDir.getAbsolutePath());
                }
            } else if (modFile.isFile() && modFile.getName().endsWith(".jar")) {
                // JARファイルとして実行
                loadFromJar(modFile, gson);
            }
        } catch (IOException e) {
            System.err.println("RoomDataLoader: Failed to load room data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 指定されたディレクトリからJSONファイルを読み込みます (開発環境用)。
     * @param dataDir JSONファイルが格納されているディレクトリ
     * @param gson Gsonインスタンス
     * @throws IOException ファイル読み込みエラーが発生した場合
     */
    private static void loadFromDirectory(File dataDir, Gson gson) throws IOException {
        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            System.out.println("RoomDataLoader: No JSON files found in " + dataDir.getAbsolutePath());
            return;
        }

        for (File file : files) {
            processFile(file.getName(), new java.io.FileInputStream(file), gson);
        }
    }

    /**
     * JARファイルからJSONファイルを読み込みます (JAR実行環境用)。
     * @param jarFile ModのJARファイル
     * @param gson Gsonインスタンス
     * @throws IOException JARファイル読み込みエラーが発生した場合
     */
    private static void loadFromJar(File jarFile, Gson gson) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // assets/jsperoute/data/ から始まるJSONファイルのみを対象とする
                if (entryName.startsWith("assets/" + ModMain.MODID + "/data/") && entryName.endsWith(".json")) {
                    String fileName = entryName.substring(entryName.lastIndexOf('/') + 1);
                    try (InputStream is = jar.getInputStream(entry)) {
                        processFile(fileName, is, gson);
                    }
                }
            }
        }
    }

    /**
     * 個々のJSONファイルを処理し、適切なマップにデータを格納します。
     * @param fileName 処理するファイル名
     * @param inputStream ファイルのInputStream
     * @param gson Gsonインスタンス
     * @throws IOException ファイル読み込みエラーが発生した場合
     */
    private static void processFile(String fileName, InputStream inputStream, Gson gson) throws IOException {
        String roomName = fileName.substring(0, fileName.lastIndexOf('_'));
        String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        if (fileName.endsWith("_template.json")) {
            // テンプレートJSONはBlockDataのリストとして読み込む
            Type templateListType = new TypeToken<List<BlockData>>(){}.getType();
            List<BlockData> blocks = gson.fromJson(json, templateListType);
            RoomTemplate template = new RoomTemplate();
            template.setName(roomName);
            template.setBlocks(blocks);
            ModMain.TEMPLATES.put(roomName, template);
            System.out.println("RoomDataLoader: Loaded template: " + fileName);
        } else if (fileName.endsWith("_route.json")) {
            // ルートJSONはRoomRouteオブジェクトとして読み込む
            Type routeType = new TypeToken<RoomRoute>(){}.getType();
            RoomRoute route = gson.fromJson(json, routeType);
            ModMain.ROUTES.put(roomName, route);
            System.out.println("RoomDataLoader: Loaded route: " + fileName);
        } else {
            System.out.println("RoomDataLoader: Skipped unknown file: " + fileName);
        }
    }
}