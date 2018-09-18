package server;

import java.util.Map;

import client.MapleBuffStat;
import constants.GameConstants;
import net.SendPacketOpcode;
import net.packet.PacketHelper;
import tools.data.MaplePacketWriter;

public class AbstractSkillHandler implements SkillHandler {
	
	public AbstractSkillHandler() {
		
	}

	@Override
	public void handleBuff() {

	}

	@Override
	public byte[] giveBuff(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups) {
		System.out.println("trying for abstract");
		MaplePacketWriter mplew = new MaplePacketWriter();
    	
    	mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getOpcode());
    	PacketHelper.writeBuffMask(mplew, statups);

        for (Map.Entry<MapleBuffStat, Integer> stat : statups.entrySet()) {
            if (!stat.getKey().canStack()) {
                if (GameConstants.isSpecialBuff(buffid)) {
                    mplew.writeInt(stat.getValue());
                } else {
                    mplew.writeShort(stat.getValue());
                }
                mplew.writeInt(buffid);
                mplew.writeInt(bufflength);
            }
        }

        mplew.writeZeroBytes(13);
        
        for (Map.Entry<MapleBuffStat, Integer> stat : statups.entrySet()) {
            if (stat.getKey().canStack()) {
                mplew.writeInt(1); // stacks size
                mplew.writeInt(buffid);
                mplew.writeInt(stat.getValue());
                mplew.writeInt((int) (System.currentTimeMillis() % 1000000000)); // ?
                mplew.writeInt(1); 
                mplew.writeInt(bufflength);
            }
        } 
        
        if (statups.containsKey(MapleBuffStat.MAPLE_WARRIOR)) {
        	mplew.write(0);
        }
        if (statups.containsKey(MapleBuffStat.DARKSIGHT)) {
        	mplew.writeInt(0);
        }
 
        mplew.writeShort(1); // Buff count. Used 1 as a placeholder for now.
        mplew.write(0);
        mplew.write(0); // bJustBuffCheck
        mplew.write(0); // bFirstSet
        
    	return mplew.getPacket();
	}

}
