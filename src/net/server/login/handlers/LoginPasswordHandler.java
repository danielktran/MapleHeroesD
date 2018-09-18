package net.server.login.handlers;

import java.util.Calendar;

import client.MapleClient;
import client.character.MapleCharacter;
import constants.ServerConfig;
import constants.ServerConstants;
import constants.WorldConstants.WorldOption;
import net.MaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import net.packet.LoginPacket;
import net.packet.PacketHelper;
import net.server.channel.ChannelServer;
import net.server.login.LoginWorker;
import net.server.login.handlers.deprecated.AutoRegister;
import tools.FileoutputUtil;
import tools.data.LittleEndianAccessor;

public class LoginPasswordHandler implements MaplePacketHandler {
	private RecvPacketOpcode recv;
	
	public LoginPasswordHandler(RecvPacketOpcode recv) {
		this.recv = recv;
	}

	private static boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 3;
    }

	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		lea.readByte();
    	String pwd = c.isLocalhost() ? "admin" : lea.readMapleAsciiString();
        String login = c.isLocalhost() ? "admin" : lea.readMapleAsciiString();

        //login = login.replace("NP12:auth07:5:0:", "");
        System.out.println("Username: " + login);
        //System.out.println("Replaced pw: " + pwd);

        final boolean ipBan = c.hasBannedIP();
        final boolean macBan = c.hasBannedMac();

        int loginok = 0;
        if (AutoRegister.autoRegister && !AutoRegister.getAccountExists(login) && (!c.hasBannedIP() || !c.hasBannedMac())) {
            if (pwd.equalsIgnoreCase("disconnect") || pwd.equalsIgnoreCase("fixme")) {
                c.getSession().write(CWvsContext.broadcastMsg(1, "This password is invalid."));
                c.getSession().write(LoginPacket.getLoginFailed(1)); //Shows no message, used for unstuck the login button
                return;
            }
            AutoRegister.createAccount(login, pwd, c.getSession().getRemoteAddress().toString());
            if (AutoRegister.success) {
                c.getSession().write(CWvsContext.broadcastMsg(1, "Account has been successfully registered!\r\nPlease login again to enter your new account."));
                c.getSession().write(LoginPacket.getLoginFailed(1)); //Shows no message, used for unstuck the login button
                return;
            }
        } else if (pwd.equalsIgnoreCase("disconnect")) {
            for (WorldOption servers : WorldOption.values()) {
                if (servers.show() && servers.isAvailable()) {
                    for (MapleCharacter character : c.loadCharacters(servers.getWorld())) {
                        for (ChannelServer cs : ChannelServer.getAllInstances()) {
                            MapleCharacter victim = cs.getPlayerStorage().getCharacterById(character.getID());
                            if (victim != null) {
                                victim.getClient().getSession().close();
                                victim.getClient().disconnect(true, false);
                            }
                        }
                    }
                }
            }
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, c.getSessionIPAddress());
            c.getSession().write(CWvsContext.broadcastMsg(1, "Your characters have been disconnected successfully."));
            c.getSession().write(LoginPacket.getLoginFailed(1)); //Shows no message, used for unstuck the login button
            return;
        } else {
            loginok = c.login(login, pwd, ipBan || macBan);
        }

        final Calendar tempbannedTill = c.getTempBanCalendar();

        if (!c.isGm() && !c.isLocalhost() && ServerConstants.Use_Localhost) {
            c.getSession().write(CWvsContext.broadcastMsg(1, "We are sorry, but the server is under a maintenance, please check the forums for more information."));
            c.getSession().write(LoginPacket.getLoginFailed(1)); //Shows no message, used for unstuck the login button
        }

        if (loginok == 0 && (ipBan || macBan) && !c.isGm()) {
            loginok = 3;
            if (macBan) {
                // this is only an ipban o.O" - maybe we should refactor this a bit so it's more readable
                MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0], "Enforcing account ban, account " + login, false, 4, false);
            }
        }
        if (loginok != 0) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                if (loginok == 3) {
                    c.getSession().write(CWvsContext.broadcastMsg(1, c.showBanReason(login, true)));
                    c.getSession().write(LoginPacket.getLoginFailed(1)); //Shows no message, used for unstuck the login button
                } else {
                    c.getSession().write(LoginPacket.getLoginFailed(loginok));
                }
            } else {
                c.getSession().close();
            }
        } else if (tempbannedTill.getTimeInMillis() != 0) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                c.getSession().write(LoginPacket.getTempBan(PacketHelper.getTime(tempbannedTill.getTimeInMillis()), c.getBanReason()));
            } else {
                c.getSession().close();
            }
        } else {
            if (ServerConfig.logAccounts) {
                FileoutputUtil.logToFile("Accounts", "\r\nID: " + login + " Password: " + pwd);
            }
            c.loginAttempt = 0;
            LoginWorker.registerClient(c);
        }
	}

    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

	public RecvPacketOpcode getRecvOpcode() {
		return recv;
	}

    

}
