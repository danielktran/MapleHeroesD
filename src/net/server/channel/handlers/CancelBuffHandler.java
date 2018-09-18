package net.server.channel.handlers;

import client.MapleClient;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import tools.data.LittleEndianAccessor;

public class CancelBuffHandler extends AbstractMaplePacketHandler {

	public CancelBuffHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
		
		final int sourceid = lea.readInt();
		
        Skill skill = SkillFactory.getSkill(sourceid);
            switch (sourceid) {
//               // case 33001001: //��Ծ� ���̵�
//              //  chr.send(CWvsContext.cancelJaguarRiding());
//              //  break;
       //    case 13101024:
     //          chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid), false);
      //         break;
      //     case 13111023:
     //      case 13120008:
     //          chr.cancelAllBuffs();
           //     chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.ALBATROSS});
             //   chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.INDIE_PAD});
            //    chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.HP_BOOST});
            //    chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.ATTACK_SPEED});
            //    chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.CRITICAL_PERCENT_UP});
         //      chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid), false);
               
      //         break;
           case 4341052: 
           chr.getStat().setHp(0, chr);
           chr.updateSingleStat(MapleStat.HP, 0);
           chr.getClient().getSession().write(CWvsContext.enableActions());
                break;
    }
        
        if (skill.isChargeSkill()) {
            chr.setKeyDownSkill_Time(0L);
            chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid), false);
        } else {
            chr.cancelEffect(skill.getEffect(1), false, -1L);
        }
	}

}
