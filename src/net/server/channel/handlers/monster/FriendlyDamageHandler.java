package net.server.channel.handlers.monster;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import server.Randomizer;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.data.LittleEndianAccessor;

public class FriendlyDamageHandler extends AbstractMaplePacketHandler {

	public FriendlyDamageHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		MapleMap map = chr.getMap();
        if (map == null) {
            return;
        }
        MapleMonster mobfrom = map.getMonsterByOid(lea.readInt());
        lea.skip(4);
        MapleMonster mobto = map.getMonsterByOid(lea.readInt());

        if ((mobfrom != null) && (mobto != null) && (mobto.getStats().isFriendly())) {
            int damage = mobto.getStats().getLevel() * Randomizer.nextInt(mobto.getStats().getLevel()) / 2;
            mobto.damage(chr, damage, true);
            MobHelper.checkShammos(chr, mobto, map);
        }
	}

}
