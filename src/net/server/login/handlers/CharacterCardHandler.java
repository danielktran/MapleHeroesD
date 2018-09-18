package net.server.login.handlers;

import java.util.LinkedHashMap;
import java.util.Map;

import client.MapleClient;
import client.character.MapleCharacter;
import constants.WorldConstants.WorldOption;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.server.channel.ChannelServer;
import tools.data.LittleEndianAccessor;

public class CharacterCardHandler extends AbstractMaplePacketHandler {

	public CharacterCardHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (lea.available() != 36 || !c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        final Map<Integer, Integer> cids = new LinkedHashMap<>();
        for (int i = 1; i <= 6; i++) { // 6 chars
            final int charId = lea.readInt();
            if ((!c.login_Auth(charId) && charId != 0) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) {
                c.getSession().close();
                return;
            }
            cids.put(i, charId);
        }
        c.updateCharacterCards(cids);
	}

}
