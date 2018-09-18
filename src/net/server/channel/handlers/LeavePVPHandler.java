package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import tools.data.LittleEndianAccessor;

public class LeavePVPHandler extends AbstractMaplePacketHandler {

	public LeavePVPHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (c.getCharacter() == null || c.getCharacter().getMap() == null || !c.getCharacter().inPVP()) {
            c.getSession().write(CField.pvpBlocked(6));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        int x = Integer.parseInt(c.getCharacter().getEventInstance().getProperty(String.valueOf(c.getCharacter().getID())));
        final int lv = Integer.parseInt(c.getCharacter().getEventInstance().getProperty("lvl"));
        if (lv < 2 && c.getCharacter().getLevel() >= 120) { //gladiator, level 120+
            x /= 2;
        }
        c.getCharacter().setTotalBattleExp(c.getCharacter().getTotalBattleExp() + ((x / 10) * 3 / 2));
        c.getCharacter().setBattlePoints(c.getCharacter().getBattlePoints() + ((x / 10) * 3 / 2)); //PVP 1.5 EVENT!
        c.getCharacter().cancelAllBuffs();
        c.getCharacter().changeRemoval();
        c.getCharacter().dispelDebuffs();
        c.getCharacter().clearAllCooldowns();
        c.getCharacter().updateTick(lea.readInt());
        c.getSession().write(CWvsContext.clearMidMsg());
        c.getCharacter().changeMap(c.getChannelServer().getMapFactory().getMap(960000000));
        c.getCharacter().getStat().recalcLocalStats(c.getCharacter());
        c.getCharacter().getStat().heal(c.getCharacter());
	}

}
