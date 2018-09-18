package net.server.login.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import constants.WorldConstants.WorldOption;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.LoginPacket;
import net.server.channel.ChannelServer;
import net.server.login.LoginServer;
import net.server.login.handlers.deprecated.CharLoginHandler;
import tools.data.LittleEndianAccessor;

public class CharSelectWithNoPicHandler extends AbstractMaplePacketHandler {

	public CharSelectWithNoPicHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	private static boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 3;
    }
	
	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		boolean view = false, haspic = false;
		
		if (constants.ServerConfig.DISABLE_PIC) {
            CharLoginHandler.Character_login_noPIC((LittleEndianAccessor) lea, c, view, haspic);
        }
        lea.readByte(); // 1?
        lea.readByte(); // 1?
        final int charId = lea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(lea.readInt());
        }
        final String currentpw = c.getSecondPassword();
        
        if (!c.isLoggedIn() || loginFailCount(c) || (currentpw != null && (!currentpw.equals("") || haspic)) || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) {
            c.getSession().close();
            return;
        }
        
        c.updateMacs(lea.readMapleAsciiString());
        lea.readMapleAsciiString();
        if (lea.available() != 0) {
            final String setpassword = lea.readMapleAsciiString();

            if (setpassword.length() >= 6 && setpassword.length() <= 16) {
                c.setSecondPassword(setpassword);
                c.updateSecondPassword();
            } else {
                c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
                return;
            }
        } else if (haspic) {
            return;
        }
        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        final String s = c.getSessionIPAddress();
        LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
        c.getSession().write(CField.getServerIP(Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), c.getWorld(), charId));
	}

}
