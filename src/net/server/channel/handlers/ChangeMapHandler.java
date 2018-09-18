package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import net.server.cashshop.CashShopServer;
import net.server.channel.ChannelServer;
import net.server.login.LoginServer;
import net.world.CharacterTransfer;
import net.world.World;
import server.MapleInventoryManipulator;
import server.MaplePortal;
import server.maps.MapleMap;
import tools.data.LittleEndianAccessor;

public class ChangeMapHandler extends AbstractMaplePacketHandler {

	public ChangeMapHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		if(c.getCharacter().getMap() == null) {
			CashShopServer.getPlayerStorage().deregisterPlayer(chr);
	        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());

	        try {

	            World.ChannelChange_Data(new CharacterTransfer(chr), chr.getID(), c.getChannel());
	            c.getSession().write(CField.getChannelChange(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1])));
	        } finally {
	            final String s = c.getSessionIPAddress();
	            LoginServer.addIPAuth(s.substring(s.indexOf('/') + 1, s.length()));
	            chr.saveToDB(false, true);
	            c.setPlayer(null);
	            c.setReceiving(false);
	            c.getSession().close();
	        }
		} else {
			if ((chr == null) || (chr.getMap() == null)) {
	            return;
	        }
	        if (lea.available() != 0L) {
	            lea.readByte();
	            int targetid = lea.readInt();
	            lea.readInt();
	            MaplePortal portal = chr.getMap().getPortal(lea.readMapleAsciiString());
	            if (lea.available() >= 7L) {
	                chr.updateTick(lea.readInt());
	            }
	            lea.skip(1);
	            boolean wheel = (lea.readShort() > 0) && (!GameConstants.isEventMap(chr.getMapId())) && (chr.haveItem(5510000, 1, false, true)) && (chr.getMapId() / 1000000 != 925);
	
	            if ((targetid != -1) && (!chr.isAlive())) {
	                chr.setStance(0);
	                if ((chr.getEventInstance() != null) && (chr.getEventInstance().revivePlayer(chr)) && (chr.isAlive())) {
	                    return;
	                }
	                if (chr.getPyramidSubway() != null) {
	                    chr.getStat().setHp(50, chr);
	                    chr.getPyramidSubway().fail(chr);
	                    return;
	                }
	                
	                        if (chr.getMapId() == 105200111) {
	                            chr.getStat().setHp(500000, chr);
	                            chr.getStat().setMp(500000, chr);
	                        }
	
	                if (!wheel) {
	                    chr.getStat().setHp(50, chr);
	
	                    MapleMap to = chr.getMap().getReturnMap();
	                    chr.changeMap(to, to.getPortal(0));
	                } else {
	                    c.getSession().write(CField.EffectPacket.useWheel((byte) (chr.getInventory(MapleInventoryType.CASH).countById(5510000) - 1)));
	                    chr.getStat().setHp(chr.getStat().getMaxHp() / 100 * 40, chr);
	                    MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);
	
	                    MapleMap to = chr.getMap();
	                    chr.changeMap(to, to.getPortal(0));
	                }
	            } else if ((targetid != -1) && (chr.isIntern())) {
	                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
	                if (to != null) {
	                    chr.changeMap(to, to.getPortal(0));
	                } else {
	                    chr.dropMessage(5, "Map is NULL. Use !warp <mapid> instead.");
	                }
	            } else if ((targetid != -1) && (!chr.isIntern())) {
	                int divi = chr.getMapId() / 100;
	                boolean unlock = false;
	                boolean warp = false;
	                if (divi == 9130401) {
	                    warp = (targetid / 100 == 9130400) || (targetid / 100 == 9130401);
	                    if (targetid / 10000 != 91304) {
	                        warp = true;
	                        unlock = true;
	                        targetid = 130030000;
	                    }
	                } else if (divi == 9130400) {
	                    warp = (targetid / 100 == 9130400) || (targetid / 100 == 9130401);
	                    if (targetid / 10000 != 91304) {
	                        warp = true;
	                        unlock = true;
	                        targetid = 130030000;
	                    }
	                } else if (divi == 9140900) {
	                    warp = (targetid == 914090011) || (targetid == 914090012) || (targetid == 914090013) || (targetid == 140090000);
	                } else if ((divi == 9120601) || (divi == 9140602) || (divi == 9140603) || (divi == 9140604) || (divi == 9140605)) {
	                    warp = (targetid == 912060100) || (targetid == 912060200) || (targetid == 912060300) || (targetid == 912060400) || (targetid == 912060500) || (targetid == 3000100);
	                    unlock = true;
	                } else if (divi == 9101500) {
	                    warp = (targetid == 910150006) || (targetid == 101050010);
	                    unlock = true;
	                } else if ((divi == 9140901) && (targetid == 140000000)) {
	                    unlock = true;
	                    warp = true;
	                } else if ((divi == 9240200) && (targetid == 924020000)) {
	                    unlock = true;
	                    warp = true;
	                } else if ((targetid == 980040000) && (divi >= 9800410) && (divi <= 9800450)) {
	                    warp = true;
	                } else if ((divi == 9140902) && ((targetid == 140030000) || (targetid == 140000000))) {
	                    unlock = true;
	                    warp = true;
	                } else if ((divi == 9000900) && (targetid / 100 == 9000900) && (targetid > chr.getMapId())) {
	                    warp = true;
	                } else if ((divi / 1000 == 9000) && (targetid / 100000 == 9000)) {
	                    unlock = (targetid < 900090000) || (targetid > 900090004);
	                    warp = true;
	                } else if ((divi / 10 == 1020) && (targetid == 1020000)) {
	                    unlock = true;
	                    warp = true;
	                } else if ((chr.getMapId() == 900090101) && (targetid == 100030100)) {
	                    unlock = true;
	                    warp = true;
	                } else if ((chr.getMapId() == 2010000) && (targetid == 104000000)) {
	                    unlock = true;
	                    warp = true;
	                } else if ((chr.getMapId() == 106020001) || (chr.getMapId() == 106020502)) {
	                    if (targetid == chr.getMapId() - 1) {
	                        unlock = true;
	                        warp = true;
	                    }
	                } else if ((chr.getMapId() == 0) && (targetid == 10000)) {
	                    unlock = true;
	                    warp = true;
	                } else if ((chr.getMapId() == 931000011) && (targetid == 931000012)) {
	                    unlock = true;
	                    warp = true;
	                } else if ((chr.getMapId() == 931000021) && (targetid == 931000030)) {
	                    unlock = true;
	                    warp = true;
	                } else if ((chr.getMapId() == 105040300) && (targetid == 105040000)) {
	                    unlock = true;
	                    warp = true;
	                }
	                if (unlock) {
	                    c.getSession().write(CField.UIPacket.IntroDisableUI(false));
	                    c.getSession().write(CField.UIPacket.IntroLock(false));
	                    c.getSession().write(CWvsContext.enableActions());
	                }
	                if (warp) {
	                    MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
	                    chr.changeMap(to, to.getPortal(0));
	                }
	            } else if ((portal != null) && (!chr.hasBlockedInventory())) {
	                portal.enterPortal(c);
	            } else {
	                c.getSession().write(CWvsContext.enableActions());
	            }
	        }
        }
	}
}
