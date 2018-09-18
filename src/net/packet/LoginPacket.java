package net.packet;

import client.MapleClient;
import client.PartTimeJob;
import client.character.MapleCharacter;
import constants.GameConstants;
import constants.JobConstants;
import constants.JobConstants.LoginJob;
import constants.ServerConfig;
import constants.ServerConstants;
import constants.WorldConstants.WorldOption;
import net.SendPacketOpcode;
import net.server.login.LoginServer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import server.Randomizer;
import tools.HexTool;
import tools.Triple;
import tools.data.MaplePacketWriter;

public class LoginPacket {

    public static byte[] getHello(byte[] sendIv, byte[] recvIv) {
        MaplePacketWriter mpw = new MaplePacketWriter();
        
        mpw.writeShort(0x0F);                                        // Packet Size
        mpw.writeShort(ServerConstants.CLIENT_VERSION);              // MapleStory Version
        mpw.writeMapleAsciiString(ServerConstants.CLIENT_SUBVERSION);// MapleStory Patch Location/Subversion
        if (ServerConfig.USE_FIXED_IV) {
        	mpw.write(ServerConfig.Static_RemoteIV);
        	mpw.write(ServerConfig.Static_LocalIV);
        } else {
            mpw.write(recvIv);                                        // Local Initializing Vector 
            mpw.write(sendIv);                                        // Remote Initializing Vector
        }
        mpw.write(8);                                                 // MapleStory Locale 8 = GMS
        mpw.write(0);                                                 // Unknown

        return mpw.getPacket(false);
    }
    
    public static byte[] getClientResponse(int request) {
    	MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CLIENT_AUTH);
    	mpw.write(0);
    	
    	return mpw.getPacket();
    }
    
    /**
     * Sends an authentication response every 15 seconds which allows the client to keep a connection to the server.
     * @param response
     * @return
     */
    public static byte[] sendAuthResponse(int response) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.AUTH_RESPONSE);
		mpw.writeInt(response);
        
        return mpw.getPacket();
    }

    public static final byte[] getPing() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PING);
        return mpw.getPacket();
    }

    public static byte[] getLoginSuccess(MapleClient client) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOGIN_STATUS);
		mpw.writeZeroBytes(6);
        mpw.writeMapleAsciiString(client.getAccountName());
        mpw.writeInt(client.getAccountID());
        //mpw.writeShort(2);
        mpw.write(0);
        mpw.writeInt(0);
        mpw.writeZeroBytes(6);
        mpw.writeMapleAsciiString(client.getAccountName());
        mpw.writeZeroBytes(11);
        mpw.writeZeroBytes(6);
        mpw.write(1);
        mpw.writeInt(0);
        mpw.write(JobConstants.enableJobs ? 1 : 0); //toggle
        mpw.write(JobConstants.jobOrder); //Job Order (orders are located in wz)
        for (LoginJob j : LoginJob.values()) {
            mpw.write(j.getFlag());
            mpw.writeShort(1);
        }
    	mpw.write(1);
        mpw.writeInt(-1);
        mpw.writeShort(1);
        mpw.writeLong(client.getSession().getCreationTime());
        
        return mpw.getPacket();
    }

    /**
     * location: UI.wz/Login.img/Notice/text
     * reasons:
     * useful:
     * 32 - server under maintenance check site for updates
     * 35 - your computer is running thirdy part programs close them and play again
     * 36 - due to high population char creation has been disabled
     * 43 - revision needed your ip is temporary blocked
     * 75-78 are cool for auto register
     
     */
    public static final byte[] getLoginFailed(int reason) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOGIN_STATUS);
		mpw.write(reason);
        mpw.write(0);
        mpw.writeInt(0);

        return mpw.getPacket();
    }

    public static byte[] getPermBan(byte reason) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOGIN_STATUS);
		mpw.write(2);
        mpw.write(0);
        mpw.writeInt(0);
        mpw.writeShort(reason);
        mpw.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));

        return mpw.getPacket();
    }

    public static byte[] getTempBan(long timestampTill, byte reason) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOGIN_STATUS);
		mpw.write(2);
        mpw.write(0);
        mpw.writeInt(0);
        mpw.write(reason);
        mpw.writeLong(timestampTill);

        return mpw.getPacket();
    }
    
    public static byte[] sendPicResponse(int response) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PIC_RESPONSE);
		mpw.write(response);
        
        System.out.println(mpw.toString());
        return mpw.getPacket();
    }

    public static final byte[] getSecondAuthSuccess(MapleClient client) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOGIN_SECOND);
		mpw.write(0);
        mpw.writeInt(client.getAccountID());
        mpw.writeZeroBytes(5);
        mpw.writeMapleAsciiString(client.getAccountName());
        mpw.writeLong(2L);
        mpw.writeZeroBytes(3);
        mpw.writeInt(Randomizer.nextInt());
        mpw.writeInt(Randomizer.nextInt());
        mpw.writeInt(28);
        mpw.writeInt(Randomizer.nextInt());
        mpw.writeInt(Randomizer.nextInt());
        mpw.write(1);

        return mpw.getPacket();
    }

    public static final byte[] deleteCharResponse(int cid, int state) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DELETE_CHAR_RESPONSE);
		mpw.writeInt(cid);
        mpw.write(state);

        return mpw.getPacket();
    }

    public static byte[] secondPwError(byte mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SECONDPW_ERROR);
		mpw.write(mode);

        return mpw.getPacket();
    }

    public static byte[] enableRecommended(int world) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ENABLE_RECOMMENDED);
		mpw.writeInt(world);
        
        return mpw.getPacket();
    }

    public static byte[] sendRecommended(int world, String message) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SEND_RECOMMENDED);
		mpw.write(message != null ? 1 : 0);
        if (message != null) {
            mpw.writeInt(world);
            mpw.writeMapleAsciiString(message);
        }
        
        return mpw.getPacket();
    }

    public static byte[] ResetScreen() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.RESET_SCREEN);
		mpw.write(HexTool.getByteArrayFromHexString("02 08 00 32 30 31 32 30 38 30 38 00 08 00 32 30 31 32 30 38 31 35 00"));

        return mpw.getPacket();
    }

    public static byte[] getServerList(int serverId, Map<Integer, Integer> channelLoad) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVERLIST);
		mpw.write(serverId);
        String worldName = LoginServer.getTrueServerName();
        mpw.writeMapleAsciiString(worldName);
        mpw.write(WorldOption.getById(serverId).getFlag());
        mpw.writeMapleAsciiString(LoginServer.getEventMessage());
        mpw.writeShort(100);
        mpw.writeShort(100);
        mpw.write(0);
        int lastChannel = 1;
        Set<Integer> channels = channelLoad.keySet();
        for (int i = 30; i > 0; i--) {
            if (channels.contains(Integer.valueOf(i))) {
                lastChannel = i;
                break;
            }
        }
        mpw.write(lastChannel);

        for (int i = 1; i <= lastChannel; i++) {
            int load;

            if (channels.contains(i)) {
            	load = channelLoad.get(i);      
            } else {
                load = 1;
            }
            mpw.writeMapleAsciiString(worldName + "-" + i);
            mpw.writeInt(load);
            mpw.write(serverId);
            mpw.writeShort(i - 1);
        }
        mpw.writeShort(0);
        mpw.writeInt(0);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] getEndOfServerList() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVERLIST);
		mpw.write(-1);
        mpw.write(4);
        mpw.writeMapleAsciiString("http://maplestory.nexon.com/maplestory/news/2015/login_banner.html");
        mpw.writeMapleAsciiString("http://maplestory2.nexon.com/event/2015/OnLineFestival?");
        mpw.writeInt(5000);
        mpw.writeInt(415);
        mpw.writeInt(80);
        mpw.writeInt(192);
        mpw.writeInt(452);
        mpw.writeMapleAsciiString("http://s.nx.com/s2/Game/Maplestory/Maple2013/image/banner/ingame_bn/MS2_Festival_1.jpg");
        mpw.writeMapleAsciiString("http://maplestory2.nexon.com/live/20150627/OnlineFestival?st=maple1&bn=cla_ser&ev=live&dt=20150627");
        mpw.writeInt(5000);
        mpw.writeInt(370);
        mpw.writeInt(70);
        mpw.writeLong(0);
        mpw.writeMapleAsciiString("http://s.nx.com/s2/Game/Maplestory/Maple2013/image/banner/ingame_bn/MS2_Festival_2.jpg");
        mpw.writeMapleAsciiString("http://maplestory2.nexon.com/event/2015/StarterPack?st=maple1&bn=cla_ser&ev=starter&dt=20150623");
        mpw.writeInt(5000);
        mpw.writeInt(370);
        mpw.writeInt(70);
        mpw.writeLong(0);
        mpw.writeMapleAsciiString("http://s.nx.com/s2/Game/Maplestory/Maple2013/image/banner/ingame_bn/ingame_150701_01.jpg");
        mpw.writeMapleAsciiString("http://closers.nexon.com/news/events/view.aspx?n4articlesn=117&st=maple&bn=login&ev=20150624");
        mpw.writeInt(5000);
        mpw.writeInt(370);
        mpw.writeInt(70);
        mpw.writeLong(0);
        mpw.writeShort(0);
        
        return mpw.getPacket();
    }

    public static final byte[] getLoginWelcome() {
        List flags = new LinkedList();

        return CField.spawnFlags(flags);
    }

    public static byte[] getServerStatus(int status) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVERSTATUS);
		mpw.writeShort(status);

        return mpw.getPacket();
    }

    public static byte[] changeBackground(Triple<String, Integer, Boolean>[] backgrounds) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CHANGE_BACKGROUND);
        //mpw.write(HexTool.getByteArrayFromHexString("04 02 00 61 33 00 02 00 61 32 00 02 00 61 31 00 02 00 61 30 01"));
        mpw.write(backgrounds.length); //number of bgs
        for (Triple<String, Integer, Boolean> background : backgrounds) {
            mpw.writeMapleAsciiString(background.getLeft());
            mpw.write(background.getRight() ? Randomizer.nextInt(2) : background.getMid());
        }
        
        /* Map.wz/Obj/login.img/WorldSelect/background/background number
         Backgrounds ids sometime have more than one background anumation */
        /* Background are like layers, backgrounds in the packets are
         removed, so the background which was hiden by the last one
         is shown.
         */
        
        return mpw.getPacket();
    }

    public static byte[] getChannelSelected() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CHANNEL_SELECTED);
		mpw.writeZeroBytes(3);

        return mpw.getPacket();
    }

    public static byte[] getCharList(String secondpw, List<MapleCharacter> chars, int charslots) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CHARLIST);
		mpw.write(0);
        mpw.writeMapleAsciiString("normal");
        mpw.writeInt(4);
        mpw.write(0);
        mpw.writeInt(0);
        mpw.writeReversedLong(PacketHelper.getTime(System.currentTimeMillis()));
        mpw.write(0);
        mpw.writeInt(chars.size());
        for (MapleCharacter chr: chars) {
        	mpw.writeInt(chr.getID());
        }
        mpw.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mpw, chr, (!chr.isGM()) && (chr.getLevel() >= 30), false);
        }
        if (constants.ServerConfig.DISABLE_PIC) {
            mpw.writeShort(2);
        } else {
            mpw.writeShort((secondpw != null) && (secondpw.length() <= 0) ? 2 : (secondpw != null) && (secondpw.length() > 0) ? 1 : 0);
        }
        mpw.writeInt(charslots);
        mpw.writeInt(0);
        mpw.writeInt(-1);
        mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mpw.writeZeroBytes(6);
        
        return mpw.getPacket();
    }

    public static byte[] charNameResponse(String charname, boolean nameUsed) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CHAR_NAME_RESPONSE);
		mpw.writeMapleAsciiString(charname);
        mpw.write(nameUsed ? 1 : 0);

        return mpw.getPacket();
    }
    
    public static byte[] addNewCharEntry(MapleCharacter chr, boolean worked) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ADD_NEW_CHAR_ENTRY);
		mpw.write(worked ? 0 : 1);
        addCharEntry(mpw, chr, false, false);
        
        return mpw.getPacket();
    }

    private static void addCharEntry(MaplePacketWriter mpw, MapleCharacter chr, boolean ranking, boolean viewAll) {
        PacketHelper.addCharStats(mpw, chr);
        PacketHelper.addCharLook(mpw, chr, true, false);
        if (GameConstants.isZero(chr.getJob())) {
            PacketHelper.addCharLook(mpw, chr, true, true);
        }
        if (!viewAll) {
            mpw.write(0);
        }
        mpw.write(ranking ? 1 : 0);
        if (ranking) {
            mpw.writeInt(chr.getRank());
            mpw.writeInt(chr.getRankMove());
            mpw.writeInt(chr.getJobRank());
            mpw.writeInt(chr.getJobRankMove());
        }
    }

    public static byte[] enableSpecialCreation(int accid, boolean enable) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPECIAL_CREATION);
		mpw.writeInt(accid);
        mpw.write(enable ? 0 : 1);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] partTimeJob(int cid, short type, long time) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PART_TIME);
		mpw.writeInt(cid);
        mpw.write(0);
        mpw.write(type);
        //1) 0A D2 CD 01 70 59 9F EA
        //2) 0B D2 CD 01 B0 6B 9C 18
        mpw.writeReversedLong(PacketHelper.getTime(time));
        mpw.writeInt(0);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] updatePartTimeJob(PartTimeJob partTime) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PART_TIME);
		mpw.writeInt(partTime.getCharacterId());
        mpw.write(0);
        PacketHelper.addPartTimeJob(mpw, partTime);
        return mpw.getPacket();
    }

    public static byte[] sendLink() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SEND_LINK);
		mpw.write(1);
        mpw.write(CField.Nexon_IP);
        mpw.writeShort(0x2057);
        mpw.write(0);

        return mpw.getPacket();
    }

	public static final byte[] sendUnknown() {
		MaplePacketWriter mpw = new MaplePacketWriter(3);
	    mpw.writeShort(0x16);
	    mpw.write(0x07);
	       
	    return mpw.getPacket();
	}
}
