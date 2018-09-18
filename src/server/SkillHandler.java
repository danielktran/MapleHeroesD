package server;

import java.util.Map;

import client.MapleBuffStat;

public interface SkillHandler {
	void handleBuff();
	byte[] giveBuff(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups); 
}
