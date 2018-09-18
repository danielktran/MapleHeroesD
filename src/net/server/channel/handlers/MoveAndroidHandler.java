package net.server.channel.handlers;

import java.awt.Point;
import java.util.List;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.MovementParse;
import net.packet.CField;
import net.packet.field.AndroidPacket;
import server.movement.LifeMovementFragment;
import tools.data.LittleEndianAccessor;

public class MoveAndroidHandler extends AbstractMaplePacketHandler {

	public MoveAndroidHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		lea.skip(12);
        final List<LifeMovementFragment> res = MovementParse.parseMovement(lea, 3, null, null);
       

        if ((res != null) && (chr != null) && (!res.isEmpty()) && (chr.getMap() != null) && (chr.getAndroid() != null)) {
            Point pos = new Point(chr.getAndroid().getPos());
            chr.getAndroid().updatePosition(res);
            chr.getMap().broadcastMessage(chr, AndroidPacket.moveAndroid(chr.getID(), pos, res), false);
        }
	}

}
