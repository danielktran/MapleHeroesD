package net.server.channel.handlers.summon;

import java.awt.Point;
import java.util.List;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.MaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.MovementParse;
import net.packet.CField.SummonPacket;
import server.maps.MapleDragon;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import server.movement.LifeMovementFragment;
import tools.data.LittleEndianAccessor;

public class MoveSummonHandler extends AbstractMaplePacketHandler {

	public MoveSummonHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (chr == null || chr.getMap() == null) {
            return;
        }
        final MapleMapObject obj = chr.getMap().getMapObject(lea.readInt(), MapleMapObjectType.SUMMON);
        if (obj == null) {
            return;
        }
        if (obj instanceof MapleDragon) {
        	MaplePacketHandler handler = new MoveDragonHandler(recv);
            handler.handlePacket(lea, c, chr);
            //MoveDragon(lea, chr);
            return;
        }
        final MapleSummon sum = (MapleSummon) obj;
        if (sum.getOwnerId() != chr.getID() || sum.getSkillLevel() <= 0 || sum.getMovementType() == SummonMovementType.STATIONARY) {
            return;
        }
        lea.skip(12); //startPOS
        final List<LifeMovementFragment> res = MovementParse.parseMovement(lea, 4, null, null);

        final Point pos = sum.getPosition();
        MovementParse.updatePosition(res, sum, 0);
        if (res.size() > 0) {
            chr.getMap().broadcastMessage(chr, SummonPacket.moveSummon(chr.getID(), sum.getObjectId(), pos, res), sum.getTruePosition());
        }
	}
	
}
