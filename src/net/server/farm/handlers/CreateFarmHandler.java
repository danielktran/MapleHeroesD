package net.server.farm.handlers;

import client.MapleCharacterUtil;
import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.FarmPacket;
import server.farm.MapleFarm;
import tools.data.LittleEndianAccessor;

public class CreateFarmHandler extends AbstractMaplePacketHandler {

	public CreateFarmHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final String name = lea.readMapleAsciiString();
		
        if (!MapleCharacterUtil.canCreateChar(name, false)) {
            return;
        }
        MapleFarm farm = MapleFarm.getDefault(35549721, c, name);
        farm.setLevel(1);
        c.setFarm(farm);
        c.getSession().write(FarmPacket.updateQuestInfo(1111, 1, "A1/Z/"));
        //c.getSession().write(FarmPacket.updateFarmInfo(c, true));
        //c.getSession().write(CField.getPacketFromHexString("68 03 19 72 1E 02 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 65 73 6D 69 66 61 72 6D 7A 7A 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 01 00 00 00 00 0B 00 43 72 65 61 74 69 6E 67 2E 2E 2E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 FF FF FF FF 00"));
        c.getSession().write(FarmPacket.farmPacket4());
        c.getSession().write(FarmPacket.updateQuestInfo(30000, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30003, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30007, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30011, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30015, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30019, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30023, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30027, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30045, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30050, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30055, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30060, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30065, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30070, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30076, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30080, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30081, 0, "A1/"));
        c.getSession().write(FarmPacket.updateQuestInfo(30082, 0, "A1/"));
	}

}
