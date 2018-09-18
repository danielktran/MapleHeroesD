package net.server.channel.handlers;

import java.awt.Point;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import server.maps.MapleMapObject;
import server.maps.MechDoor;
import tools.data.LittleEndianAccessor;

public class MechDoorHandler extends AbstractMaplePacketHandler {

	public MechDoorHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final int oid = lea.readInt();
        final Point pos = lea.readPos();
        final int mode = lea.readByte(); // specifies if backwarp or not, 1 town to target, 0 target to town
        chr.getClient().getSession().write(CWvsContext.enableActions());
        for (MapleMapObject obj : chr.getMap().getAllMechDoorsThreadsafe()) {
            final MechDoor door = (MechDoor) obj;
            if (door.getOwnerId() == oid && door.getId() == mode) {
                chr.checkFollow();
                chr.getMap().movePlayer(chr, pos);
                break;
            }
        }
	}

}
