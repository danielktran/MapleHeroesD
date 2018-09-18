package net.server.channel.handlers.monster;

import client.MapleClient;
import client.MonsterStatus;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.data.LittleEndianAccessor;

public class MobBombHandler extends AbstractMaplePacketHandler {

	public MobBombHandler(RecvPacketOpcode recv) {
		super(recv);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		MapleMap map = chr.getMap();
        if (map == null) {
            return;
        }
        MapleMonster mobfrom = map.getMonsterByOid(lea.readInt());
        lea.skip(4);
        lea.readInt();

        if ((mobfrom != null) && (mobfrom.getBuff(MonsterStatus.MONSTER_BOMB) != null));
	}

}
