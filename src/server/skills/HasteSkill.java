package server.skills;

import java.util.Map;

import client.MapleBuffStat;
import constants.GameConstants;
import net.SendPacketOpcode;
import net.packet.PacketHelper;
import server.AbstractSkillHandler;
import tools.data.MaplePacketWriter;

public class HasteSkill extends AbstractSkillHandler {
	
	@Override
	public byte[] giveBuff(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups) {
		System.out.println("attempting to give haste");
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
        
        mplew.writeZeroBytes(13);
        
        switch(buffid) {
        case 1002: // Nimble Feet
        	mplew.write(1);
        	break;
        case 4001005: // Haste
        	mplew.write(0);
        	break;
        }
        
        mplew.writeShort(1);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        
        System.out.println(mplew.toString());
    	return mplew.getPacket();
    }

}
