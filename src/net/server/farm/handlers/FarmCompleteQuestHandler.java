package net.server.farm.handlers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.FarmPacket;
import tools.data.LittleEndianAccessor;

public class FarmCompleteQuestHandler extends AbstractMaplePacketHandler {

	public FarmCompleteQuestHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		int questId = lea.readInt();
        if (questId == 1111) {
            c.getSession().write(FarmPacket.updateQuestInfo(1111, 1, ""));
            SimpleDateFormat sdfGMT = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            sdfGMT.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
            String timeStr = sdfGMT.format(Calendar.getInstance().getTime()).replaceAll("-", "");
            c.getSession().write(FarmPacket.updateQuestInfo(1111, 2, timeStr));
            System.out.println(timeStr);
            c.getSession().write(FarmPacket.alertQuest(1111, 0));
            c.getSession().write(FarmPacket.updateQuestInfo(1112, 0, "A1/"));
            c.getSession().write(FarmPacket.updateQuestInfo(1112, 1, "A1/Z/"));
        }
	}

}
