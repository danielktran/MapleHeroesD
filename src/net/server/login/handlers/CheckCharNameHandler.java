package net.server.login.handlers;

import client.MapleCharacterUtil;
import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.LoginPacket;
import net.server.login.LoginInformationProvider;
import tools.data.LittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;

public class CheckCharNameHandler extends AbstractMaplePacketHandler {

	public CheckCharNameHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		final String name = lea.readMapleAsciiString();
		boolean nameUsed = true;
		LoginInformationProvider li = LoginInformationProvider.getInstance();
        
        if (MapleCharacterUtil.canCreateChar(name, c.isGm())) {
            nameUsed = false;
        }
        if (li.isForbiddenName(name) && !c.isGm()) {
            nameUsed = false;
        }
        c.getSession().write(LoginPacket.charNameResponse(name, nameUsed));

	}

}
