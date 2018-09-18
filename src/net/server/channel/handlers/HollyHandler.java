package net.server.channel.handlers;

import java.awt.Point;

import client.MapleClient;
import client.SkillFactory;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import tools.data.LittleEndianAccessor;

public class HollyHandler extends AbstractMaplePacketHandler {

	public HollyHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final MapleMapObject obj = c.getCharacter().getMap().getMapObject(lea.readInt(), MapleMapObjectType.SUMMON);
        int skillid = lea.readInt();
        if (skillid == 3121013) {
            final MapleSummon sum = (MapleSummon) obj;
            Point poss = c.getCharacter().getPosition();
                    final MapleSummon tosummon = new MapleSummon(c.getCharacter(), SkillFactory.getSkill(3121013).getEffect(sum.getSkillLevel()), new Point(sum.getTruePosition().x, sum.getTruePosition().y), SummonMovementType.STATIONARY);
                    c.getCharacter().getMap().spawnSummon(tosummon);
                    c.getCharacter().addSummon(tosummon);
            return;
        }
        int HP = SkillFactory.getSkill(3121013).getEffect(c.getCharacter().getSkillLevel(3121013)).getX();
        int hp = c.getCharacter().getStat().getMaxHp() * HP / 100;
        c.getCharacter().addHP(hp);
	}

}
