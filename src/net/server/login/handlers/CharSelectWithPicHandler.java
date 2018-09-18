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
import tools.FileoutputUtil;
import tools.data.LittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;

public class CharSelectWithPicHandler extends AbstractMaplePacketHandler {

	public CharSelectWithPicHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	private static boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 3;
    }
	
	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		boolean view = true;
		
		if (constants.ServerConfig.DISABLE_PIC) {
            CharLoginHandler.Character_login_noPIC((LittleEndianAccessor) lea, c, view, true);
        }
        final String password = lea.readMapleAsciiString();
        final int charId = lea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(lea.readByte());
        }
        if (!c.isLoggedIn() || loginFailCount(c) || c.getSecondPassword() == null || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) {
            c.getSession().close();
            return;
        }
        c.updateMacs(lea.readMapleAsciiString());
        if (c.CheckSecondPassword(password) && password.length() >= 6 && password.length() <= 16 || c.isGm() || c.isLocalhost()) {
            FileoutputUtil.logToFile("Secondary Passwords", "\r\nID: " + c.getAccountName() + " PIC: " + password);
            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }

            final String s = c.getSessionIPAddress();
            LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
            c.getSession().write(CField.getServerIP(Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), c.getWorld(), charId));
        } else {
            c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
        }

	}

}
