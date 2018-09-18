package net.server.channel.handlers;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import client.MapleClient;
import client.MapleStat;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import tools.Pair;
import tools.data.LittleEndianAccessor;

public class RespawnPVPHandler extends AbstractMaplePacketHandler {

	public RespawnPVPHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final Lock ThreadLock = new ReentrantLock();
	    /*if (c.getPlayer() == null || c.getPlayer().getMap() == null || !c.getPlayer().inPVP() || c.getPlayer().isAlive()) {
	     c.getSession().write(CWvsContext.enableActions());
	     return;
	     }*/
	    final int type = Integer.parseInt(c.getCharacter().getEventInstance().getProperty("type"));
	    byte lvl = 0;
	    c.getCharacter().getStat().heal_noUpdate(c.getCharacter());
	    c.getCharacter().updateSingleStat(MapleStat.MP, c.getCharacter().getStat().getMp());
	    //c.getPlayer().getEventInstance().schedule("broadcastType", 500);   
	    ThreadLock.lock();
	    try {
	        c.getCharacter().getEventInstance().schedule("updateScoreboard", 500);
	    } finally {
	        ThreadLock.unlock();
	    }
	    c.getCharacter().changeMap(c.getCharacter().getMap(), c.getCharacter().getMap().getPortal(type == 0 ? 0 : (type == 3 ? (c.getCharacter().getTeam() == 0 ? 3 : 1) : (c.getCharacter().getTeam() == 0 ? 2 : 3))));
	    c.getSession().write(CField.getPVPScore(Integer.parseInt(c.getCharacter().getEventInstance().getProperty(String.valueOf(c.getCharacter().getID()))), false));
	
	    if (c.getCharacter().getLevel() >= 30 && c.getCharacter().getLevel() < 70) {
	        lvl = 0;
	    } else if (c.getCharacter().getLevel() >= 70 && c.getCharacter().getLevel() < 120) {
	        lvl = 1;
	    } else if (c.getCharacter().getLevel() >= 120 && c.getCharacter().getLevel() < 180) {
	        lvl = 2;
	    } else if (c.getCharacter().getLevel() >= 180) {
	        lvl = 3;
	    }
	
	    List<MapleCharacter> players = c.getCharacter().getEventInstance().getPlayers();
	    List<Pair<Integer, String>> players1 = new LinkedList<>();
	    for (int xx = 0; xx < players.size(); xx++) {
	        players1.add(new Pair<>(players.get(xx).getID(), players.get(xx).getName()));
	    }
	    c.getSession().write(CField.getPVPType(type, players1, c.getCharacter().getTeam(), true, lvl));
	    c.getSession().write(CField.enablePVP(true));
	}

}
