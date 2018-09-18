package net.packet.field;

import java.awt.Point;
import java.util.List;

import net.SendPacketOpcode;
import net.packet.PacketHelper;
import server.maps.MapleDragon;
import server.movement.LifeMovementFragment;
import tools.data.MaplePacketWriter;

/**
 * @author RichardL
 * @see CUser::OnDragonPacket()
 */
public class DragonPacket {
	
	/**
	 * @see CDragon::OnCreated()
	 */
	public static byte[] spawnDragon(MapleDragon d) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DRAGON_SPAWN);
		mpw.writeInt(d.getOwner());
        mpw.writeInt(d.getPosition().x);
        mpw.writeInt(d.getPosition().y);
        mpw.write(d.getStance());
        mpw.writeShort(0);
        mpw.writeShort(d.getJobId());

        return mpw.getPacket();
    }

    public static byte[] removeDragon(int chrid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DRAGON_REMOVE);
		mpw.writeInt(chrid);

        return mpw.getPacket();
    }

    /**
	 * @see CDragon::OnMove()
	 */
   public static byte[] moveDragon(MapleDragon d, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DRAGON_MOVE);
		mpw.writeInt(d.getOwner());
        mpw.writeInt(0);
        mpw.writePos(startPos);
        mpw.writeInt(0);
        PacketHelper.serializeMovementList(mpw, moves);

        return mpw.getPacket();
    }

}
