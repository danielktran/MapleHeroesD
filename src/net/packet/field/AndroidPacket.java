package net.packet.field;

import java.awt.Point;
import java.util.List;

import client.character.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleAndroid;
import client.inventory.MapleInventoryType;
import net.SendPacketOpcode;
import net.packet.PacketHelper;
import server.movement.LifeMovementFragment;
import tools.data.MaplePacketWriter;

public class AndroidPacket {

	public static byte[] spawnAndroid(MapleCharacter cid, MapleAndroid android) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ANDROID_SPAWN);
		mpw.writeInt(cid.getID());
        mpw.write(android.getItemId() == 1662006 ? 5 : android.getItemId() - 1661999);
        mpw.writePos(android.getPos());
        mpw.write(android.getStance());
        mpw.writeShort(0);
        mpw.writeShort(0);
        mpw.writeShort(android.getHair() - 30000);
        mpw.writeShort(android.getFace() - 20000);
        mpw.writeMapleAsciiString(android.getName());
        for (short i = -1200; i > -1207; i = (short) (i - 1)) {
            Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
            mpw.writeInt(item != null ? item.getItemId() : 0);
        }

        return mpw.getPacket();
    }

    public static byte[] moveAndroid(int cid, Point pos, List<LifeMovementFragment> res) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ANDROID_MOVE);
		mpw.writeInt(cid);
        mpw.writeInt(0);
        mpw.writePos(pos);
        mpw.writeInt(2147483647);
        PacketHelper.serializeMovementList(mpw, res);
        return mpw.getPacket();
    }

    public static byte[] showAndroidEmotion(int cid, byte emotion) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ANDROID_EMOTION);
        //mpw.writeInt(cid);
        mpw.writeInt(emotion);
        mpw.writeInt(0); // tDuration
      
        return mpw.getPacket();
    }

    public static byte[] updateAndroidLook(boolean itemOnly, MapleCharacter cid, MapleAndroid android) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ANDROID_UPDATE);
		mpw.writeInt(cid.getID());
        mpw.write(itemOnly ? 1 : 0);
        if (itemOnly) {
            for (short i = -1200; i > -1207; i = (short) (i - 1)) {
                Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
                mpw.writeInt(item != null ? item.getItemId() : 0);
            }
        } else {
            mpw.writeShort(0);
            mpw.writeShort(android.getHair() - 30000);
            mpw.writeShort(android.getFace() - 20000);
            mpw.writeMapleAsciiString(android.getName());
        }

        return mpw.getPacket();
    }

    public static byte[] deactivateAndroid(int cid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ANDROID_DEACTIVATED);
		mpw.writeInt(cid);

        return mpw.getPacket();
    }

}
