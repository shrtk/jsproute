package com.github.shrtk.jsproute.event;

import com.github.shrtk.jsproute.ModMain;
import com.github.shrtk.jsproute.room.BlockData;
import com.github.shrtk.jsproute.room.RoomTemplate;
import com.github.shrtk.jsproute.room.RoomRoute;
import com.github.shrtk.jsproute.util.Keybindings; // 追加
import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent; // 追加
// import net.minecraftforge.fml.common.gameevent.TickEvent; // TickEvent は不要になるので削除またはコメントアウト

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomDetectionHandler {

    // private long lastScanTime = 0; // 不要になる
    // private static final long SCAN_INTERVAL_MS = 5000; // 不要になる

    public static List<DetectedRoomInfo> detectedRooms = new ArrayList<>();

    // TickEventの代わりにInputEventを購読
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        // scanRoomKeyが押されたかどうかを確認
        if (Keybindings.scanRoomKey.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Scanning for rooms..."));
            }
            detectRooms();
        }
    }

    // detectRooms() メソッドは変更なし
    private void detectRooms() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            System.out.println("RoomDetectionHandler: World or player is null. Skipping scan.");
            return;
        }

        detectedRooms.clear();
        System.out.println("RoomDetectionHandler: Cleared previously detected rooms.");

        BlockPos playerPos = mc.thePlayer.getPosition();
        int scanRadius = 50;
        System.out.println("RoomDetectionHandler: Player position: " + playerPos.getX() + "," + playerPos.getY() + "," + playerPos.getZ() + ", Scan radius: " + scanRadius);

        if (ModMain.TEMPLATES.isEmpty()) {
            System.out.println("RoomDetectionHandler: No room templates loaded. Check RoomDataLoader logs.");
            return;
        }

        for (Map.Entry<String, RoomTemplate> entry : ModMain.TEMPLATES.entrySet()) {
            String roomName = entry.getKey();
            RoomTemplate template = entry.getValue();

            System.out.println("RoomDetectionHandler: Processing template for room: " + roomName);

            if (template.getBlocks().isEmpty()) {
                System.out.println("RoomDetectionHandler: Template for " + roomName + " has no blocks. Skipping.");
                continue;
            }

            BlockData firstTemplateBlock = template.getBlocks().get(0);
            System.out.println("RoomDetectionHandler: First template block for " + roomName + ": " + firstTemplateBlock.getAction() + " at (" + firstTemplateBlock.getX() + ", " + firstTemplateBlock.getY() + ", " + firstTemplateBlock.getZ() + ")");

            for (int x = playerPos.getX() - scanRadius; x <= playerPos.getX() + scanRadius; x++) {
                for (int y = playerPos.getY() - scanRadius; y <= playerPos.getY() + scanRadius; y++) {
                    for (int z = playerPos.getZ() - scanRadius; z <= playerPos.getZ() + scanRadius; z++) {
                        BlockPos currentWorldBlockPos = new BlockPos(x, y, z);

                        boolean templateExpectsAir = firstTemplateBlock.getAction().equals("air") || firstTemplateBlock.getAction().equals("minecraft:air");
                        boolean isWorldBlockAir = mc.theWorld.isAirBlock(currentWorldBlockPos);

                        Block worldBlock = null;
                        String worldBlockName = "unknown";

                        if (!isWorldBlockAir) {
                            worldBlock = mc.theWorld.getBlockState(currentWorldBlockPos).getBlock();
                            worldBlockName = Block.blockRegistry.getNameForObject(worldBlock).getResourcePath();
                        }

                        boolean baseBlockMatches = false;
                        if (templateExpectsAir && isWorldBlockAir) {
                            baseBlockMatches = true;
                        } else if (!templateExpectsAir && !isWorldBlockAir && worldBlockName.equals(firstTemplateBlock.getAction())) {
                            baseBlockMatches = true;
                        }

                        if (baseBlockMatches) {
                            System.out.println("RoomDetectionHandler: Potential base block match for " + roomName + " at World: (" + x + ", " + y + ", " + z + ") - Block: " + (isWorldBlockAir ? "air" : worldBlockName));

                            int offsetX = x - firstTemplateBlock.getX();
                            int offsetY = y - firstTemplateBlock.getY();
                            int offsetZ = z - firstTemplateBlock.getZ();
                            System.out.println("RoomDetectionHandler: Calculated offset: (" + offsetX + ", " + offsetY + ", " + offsetZ + ")");

                            int matchCount = 0;
                            for (BlockData tmplBlock : template.getBlocks()) {
                                BlockPos checkPos = new BlockPos(tmplBlock.getX() + offsetX, tmplBlock.getY() + offsetY, tmplBlock.getZ() + offsetZ);

                                Block checkWorldBlock = mc.theWorld.getBlockState(checkPos).getBlock();
                                String checkWorldBlockName = Block.blockRegistry.getNameForObject(checkWorldBlock).getResourcePath();

                                boolean tmplExpectsAir = tmplBlock.getAction().equals("air") || tmplBlock.getAction().equals("minecraft:air");
                                boolean isCheckWorldBlockAir = mc.theWorld.isAirBlock(checkPos);

                                boolean currentBlockMatches = false;
                                if (tmplExpectsAir && isCheckWorldBlockAir) {
                                    currentBlockMatches = true;
                                } else if (!tmplExpectsAir && !isCheckWorldBlockAir && checkWorldBlockName.equals(tmplBlock.getAction())) {
                                    currentBlockMatches = true;
                                }

                                if (currentBlockMatches) {
                                    matchCount++;
                                }
                            }

                            double matchPercentage = (double) matchCount / template.getBlocks().size() * 100.0;
                            System.out.println("RoomDetectionHandler: Match count for " + roomName + " at offset (" + offsetX + ", " + offsetY + ", " + offsetZ + "): " + matchCount + "/" + template.getBlocks().size() + " (" + String.format("%.2f", matchPercentage) + "%)");

                            if (matchPercentage >= 80.0) {
                                detectedRooms.add(new DetectedRoomInfo(roomName, offsetX, offsetY, offsetZ, matchPercentage));
                                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Detected " + roomName + " with " + String.format("%.2f", matchPercentage) + "% match at offset (" + offsetX + ", " + offsetY + ", " + offsetZ + ")"));
                                System.out.println("RoomDetectionHandler: Room " + roomName + " detected successfully!");
                                // ここで return すると、1つの部屋が見つかったらそれ以上スキャンしない (複数検出の必要がなければ)
                                // return;
                            }
                        }
                    }
                }
            }
        }
    }

    public static class DetectedRoomInfo {
        public String roomName;
        public int offsetX;
        public int offsetY;
        public int offsetZ;
        public double matchPercentage;

        public DetectedRoomInfo(String roomName, int offsetX, int offsetY, int offsetZ, double matchPercentage) {
            this.roomName = roomName;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.matchPercentage = matchPercentage;
        }
    }

    public static List<BlockData> getTransformedRoute(String roomName) {
        // このメソッドは変更なし
        // ... (省略、前回のコードと同じ)
        if (detectedRooms.isEmpty()) {
            System.out.println("RoomDetectionHandler: No rooms detected for route transformation.");
            return null;
        }

        DetectedRoomInfo targetDetectedRoom = null;
        for (DetectedRoomInfo info : detectedRooms) {
            if (info.roomName.equals(roomName)) {
                targetDetectedRoom = info;
                break;
            }
        }

        if (targetDetectedRoom == null) {
            System.out.println("RoomDetectionHandler: Target room '" + roomName + "' not found in detected list.");
            return null;
        }

        RoomRoute originalRoute = ModMain.ROUTES.get(targetDetectedRoom.roomName);
        if (originalRoute == null || originalRoute.getPositions().isEmpty()) {
            System.out.println("RoomDetectionHandler: Original route for '" + targetDetectedRoom.roomName + "' not found or is empty.");
            return null;
        }

        System.out.println("RoomDetectionHandler: Transforming route for '" + targetDetectedRoom.roomName + "' with offset (" + targetDetectedRoom.offsetX + ", " + targetDetectedRoom.offsetY + ", " + targetDetectedRoom.offsetZ + ")");

        List<BlockData> transformedRoute = new ArrayList<>();
        for (BlockData originalBlock : originalRoute.getPositions()) {
            transformedRoute.add(new BlockData(
                    originalBlock.getX() + targetDetectedRoom.offsetX,
                    originalBlock.getY() + targetDetectedRoom.offsetY,
                    originalBlock.getZ() + targetDetectedRoom.offsetZ,
                    originalBlock.getAction()
            ));
        }
        System.out.println("RoomDetectionHandler: Route transformed successfully. Total points: " + transformedRoute.size());
        return transformedRoute;
    }
}