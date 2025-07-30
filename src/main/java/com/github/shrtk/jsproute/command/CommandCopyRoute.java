package com.github.shrtk.jsproute.command;

import com.github.shrtk.jsproute.ModMain;
import com.github.shrtk.jsproute.room.BlockData;
import com.github.shrtk.jsproute.event.RoomDetectionHandler; // RoomDetectionHandlerをimport
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.client.gui.GuiScreen; // クリップボード操作のため

import java.util.List;

public class CommandCopyRoute extends CommandBase {

    @Override
    public String getCommandName() {
        return "copyroute";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/copyroute [room_name] - Detects and copies the route for a nearby room. If room_name is omitted, tries to copy the first detected room.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (sender.getEntityWorld().isRemote) { // クライアント側でのみ実行
            if (ModMain.TEMPLATES.isEmpty() || ModMain.ROUTES.isEmpty()) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: Room data not loaded. Check logs."));
                return;
            }

            String targetRoomName = null;
            if (args.length > 0) {
                targetRoomName = args[0].toLowerCase(); // コマンド引数で部屋名を指定できるようにする
            }

            List<BlockData> transformedRoute = null;

            if (targetRoomName != null) {
                // 指定された部屋名のルートを取得
                transformedRoute = RoomDetectionHandler.getTransformedRoute(targetRoomName);
                if (transformedRoute == null) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "No route detected for room '" + targetRoomName + "' or data not found."));
                    // 検出された部屋リストを表示することも可能
                    if (!RoomDetectionHandler.detectedRooms.isEmpty()) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Detected rooms: " + RoomDetectionHandler.detectedRooms.stream().map(d -> d.roomName).reduce((a, b) -> a + ", " + b).orElse("None")));
                    }
                    return;
                }
            } else if (!RoomDetectionHandler.detectedRooms.isEmpty()) {
                // 引数がない場合、最初に検出された部屋のルートを試す
                targetRoomName = RoomDetectionHandler.detectedRooms.get(0).roomName;
                transformedRoute = RoomDetectionHandler.getTransformedRoute(targetRoomName);
                if (transformedRoute == null) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "No route available for the first detected room (" + targetRoomName + ")."));
                    return;
                }
            } else {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "No room detected or specified. Run /copyroute [room_name] or wait for detection."));
                return;
            }


            if (transformedRoute != null && !transformedRoute.isEmpty()) {
                StringBuilder routeString = new StringBuilder();
                routeString.append("[");
                for (int i = 0; i < transformedRoute.size(); i++) {
                    BlockData data = transformedRoute.get(i);
                    routeString.append(String.format("{\"x\":%d,\"y\":%d,\"z\":%d,\"action\":\"%s\"}",
                            data.getX(), data.getY(), data.getZ(), data.getAction()));
                    if (i < transformedRoute.size() - 1) {
                        routeString.append(",");
                    }
                }
                routeString.append("]");

                // クリップボードにコピー
                GuiScreen.setClipboardString(routeString.toString());
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Route for " + targetRoomName + " copied to clipboard!"));
            } else {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "No transformed route available for " + targetRoomName + "."));
            }
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // 誰でも使用可能
    }
}