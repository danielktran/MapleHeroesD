package net.server.channel.handlers;

import java.util.ArrayList;
import java.util.List;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import scripting.event.EventInstanceManager;
import scripting.event.EventManager;
import server.Randomizer;
import tools.data.LittleEndianAccessor;

public class EnterPVPHandler extends AbstractMaplePacketHandler {

	public EnterPVPHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (c.getCharacter() == null || c.getCharacter().getMap() == null || c.getCharacter().getMapId() != 960000000) {
            c.getSession().write(CField.pvpBlocked(1));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (c.getCharacter().getParty() != null) {
            c.getSession().write(CField.pvpBlocked(9));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        c.getCharacter().updateTick(lea.readInt());
        lea.skip(1);
        int type = lea.readByte(), lvl = lea.readByte(), playerCount = 0;
        boolean passed = false;
        switch (lvl) {
            case 0:
                passed = c.getCharacter().getLevel() >= 30 && c.getCharacter().getLevel() < 70;
                break;
            case 1:
                passed = c.getCharacter().getLevel() >= 70;
                break;
            case 2:
                passed = c.getCharacter().getLevel() >= 120;
                break;
            case 3:
                passed = c.getCharacter().getLevel() >= 180;
                break;
        }
        final EventManager em = c.getChannelServer().getEventSM().getEventManager("PVP");
        if (!passed || em == null) {
            c.getSession().write(CField.pvpBlocked(1));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final List<Integer> maps = new ArrayList<>();
        switch (type) {
            case 0:
                maps.add(960010100);
                maps.add(960010101);
                maps.add(960010102);
                break;
            case 1:
                maps.add(960020100);
                maps.add(960020101);
                maps.add(960020102);
                maps.add(960020103);
                break;
            case 2:
                maps.add(960030100);
                break;
            case 3:
                maps.add(689000000);
                maps.add(689000010);
                break;
            default:
                passed = false;
                break;
        }
        if (!passed) {
            c.getSession().write(CField.pvpBlocked(1));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        c.getCharacter().getStat().heal(c.getCharacter());
        c.getCharacter().cancelAllBuffs();
        c.getCharacter().dispelDebuffs();
        c.getCharacter().changeRemoval();
        c.getCharacter().clearAllCooldowns();
        c.getCharacter().unequipAllPets();
        final StringBuilder key = new StringBuilder().append(lvl).append(" ").append(type).append(" ");
        //check if any of the maps are available
        for (int i : maps) {
            final EventInstanceManager eim = em.getInstance(new StringBuilder("PVP").append(key.toString()).append(i).toString().replace(" ", "").replace(" ", ""));
            if (eim != null && (eim.getProperty("started").equals("0") || eim.getPlayerCount() < 10)) {
                eim.registerPlayer(c.getCharacter());
                return;
            }
        }
        //make one
        em.startInstance_Solo(key.append(maps.get(Randomizer.nextInt(maps.size()))).toString(), c.getCharacter());
	}

}
