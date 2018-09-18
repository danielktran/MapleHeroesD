package net.server.channel.handlers;

import client.MapleClient;
import client.SkillMacro;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;

public class SkillMacroHandler extends AbstractMaplePacketHandler {

	public SkillMacroHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		int num = lea.readByte();

        for (int i = 0; i < num; i++) {
            String name = lea.readMapleAsciiString();
            int shout = lea.readByte();
            int skill1 = lea.readInt();
            int skill2 = lea.readInt();
            int skill3 = lea.readInt();

            SkillMacro macro = new SkillMacro(skill1, skill2, skill3, name, shout, i);
            chr.updateMacros(i, macro);
        }
	}

}
