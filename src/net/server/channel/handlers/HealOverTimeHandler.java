package net.server.channel.handlers;

import client.MapleClient;
import client.PlayerStats;
import client.character.MapleCharacter;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;

public class HealOverTimeHandler extends AbstractMaplePacketHandler {

	public HealOverTimeHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		if (chr == null) {
            return;
        }
        chr.updateTick(lea.readInt());
        if (lea.available() >= 8L) {
            lea.skip(lea.available() >= 12L ? 8 : 4);
        }
        int healHP = lea.readShort();
        int healMP = lea.readShort();

        PlayerStats stats = chr.getStat();

        if (stats.getHp() <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        if ((healHP != 0) && (chr.canHP(now + 1000L))) {
            if (healHP > stats.getHealHP()) {
                healHP = (int) stats.getHealHP();
            }
            chr.addHP(healHP);
        }
        if ((healMP != 0) && (!GameConstants.isDemonSlayer(chr.getJob())) && (chr.canMP(now + 1000L))) {
            if (healMP > stats.getHealMP()) {
                healMP = (int) stats.getHealMP();
            }
            chr.addMP(healMP);
        }
	}

}
