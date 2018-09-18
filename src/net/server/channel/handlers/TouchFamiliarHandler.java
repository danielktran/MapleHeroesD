package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import server.life.MapleMonster;
import tools.data.LittleEndianAccessor;

public class TouchFamiliarHandler extends AbstractMaplePacketHandler {

	public TouchFamiliarHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (chr.getSummonedFamiliar() == null) {
            return;
        }
        lea.skip(6);
        byte unk = lea.readByte();

        MapleMonster target = chr.getMap().getMonsterByOid(lea.readInt());
        if (target == null) {
            return;
        }
        int type = lea.readInt();
        lea.skip(4);
        int damage = lea.readInt();
        int maxDamage = chr.getSummonedFamiliar().getOriginalStats().getPhysicalAttack() * 5;
        if (damage < maxDamage) {
            damage = maxDamage;
        }
        if ((!target.getStats().isFriendly()) && (chr.getCheatTracker().checkFamiliarAttack(chr))) {
            chr.getMap().broadcastMessage(chr, CField.touchFamiliar(chr.getID(), unk, target.getObjectId(), type, 600, damage), chr.getTruePosition());
            target.damage(chr, damage, true);
            chr.getSummonedFamiliar().addFatigue(chr);
        }
	}

}
