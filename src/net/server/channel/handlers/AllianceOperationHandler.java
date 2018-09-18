package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import net.packet.CWvsContext.AlliancePacket;
import net.world.World;
import net.world.guild.MapleGuild;
import tools.data.LittleEndianAccessor;

public class AllianceOperationHandler extends AbstractMaplePacketHandler {

	public AllianceOperationHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		boolean denied = recv == RecvPacketOpcode.ALLIANCE_OPERATION ? false : true;
		
		if (c.getCharacter().getGuildId() <= 0) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final MapleGuild gs = World.Guild.getGuild(c.getCharacter().getGuildId());
        if (gs == null) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        //System.out.println("Unhandled GuildAlliance \n" + slea.toString());
        byte op = lea.readByte();
        if (c.getCharacter().getGuildRank() != 1 && op != 1) { //only updating doesn't need guild leader
            return;
        }
        if (op == 22) {
            denied = true;
        }
        int leaderid = 0;
        if (gs.getAllianceId() > 0) {
            leaderid = World.Alliance.getAllianceLeader(gs.getAllianceId());
        }
        //accept invite, and deny invite don't need allianceid.
        if (op != 4 && !denied) {
            if (gs.getAllianceId() <= 0 || leaderid <= 0) {
                return;
            }
        } else if (leaderid > 0 || gs.getAllianceId() > 0) { //infact, if they have allianceid it's suspicious
            return;
        }
        if (denied) {
            denyInvite(c, gs);
            return;
        }
        int inviteid;
        switch (op) {
            case 1: //load... must be in world op

                for (byte[] pack : World.Alliance.getAllianceInfo(gs.getAllianceId(), false)) {
                    if (pack != null) {
                        c.getSession().write(pack);
                    }
                }
                break;
            case 3: //invite
                final int newGuild = World.Guild.getGuildLeader(lea.readMapleAsciiString());
                if (newGuild > 0 && c.getCharacter().getAllianceRank() == 1 && leaderid == c.getCharacter().getID()) {
                    chr = c.getChannelServer().getPlayerStorage().getCharacterById(newGuild);
                    if (chr != null && chr.getGuildId() > 0 && World.Alliance.canInvite(gs.getAllianceId())) {
                        chr.getClient().getSession().write(AlliancePacket.sendAllianceInvite(World.Alliance.getAlliance(gs.getAllianceId()).getName(), c.getCharacter()));
                        World.Guild.setInvitedId(chr.getGuildId(), gs.getAllianceId());
                    } else {
                        c.getCharacter().dropMessage(1, "Make sure the leader of the guild is online and in your channel.");
                    }
                } else {
                    c.getCharacter().dropMessage(1, "That Guild was not found. Please enter the correct Guild Name. (Not the player name)");
                }
                break;
            case 4: //accept invite... guildid that invited(int, a/b check) -> guildname that was invited? but we dont care about that
                inviteid = World.Guild.getInvitedId(c.getCharacter().getGuildId());
                if (inviteid > 0) {
                    if (!World.Alliance.addGuildToAlliance(inviteid, c.getCharacter().getGuildId())) {
                        c.getCharacter().dropMessage(5, "An error occured when adding guild.");
                    }
                    World.Guild.setInvitedId(c.getCharacter().getGuildId(), 0);
                }
                break;
            case 2: //leave; nothing
            case 6: //expel, guildid(int) -> allianceid(don't care, a/b check)
                final int gid;
                if (op == 6 && lea.available() >= 4) {
                    gid = lea.readInt();
                    if (lea.available() >= 4 && gs.getAllianceId() != lea.readInt()) {
                        break;
                    }
                } else {
                    gid = c.getCharacter().getGuildId();
                }
                if (c.getCharacter().getAllianceRank() <= 2 && (c.getCharacter().getAllianceRank() == 1 || c.getCharacter().getGuildId() == gid)) {
                    if (!World.Alliance.removeGuildFromAlliance(gs.getAllianceId(), gid, c.getCharacter().getGuildId() != gid)) {
                        c.getCharacter().dropMessage(5, "An error occured when removing guild.");
                    }
                }
                break;
            case 7: //change leader
                if (c.getCharacter().getAllianceRank() == 1 && leaderid == c.getCharacter().getID()) {
                    if (!World.Alliance.changeAllianceLeader(gs.getAllianceId(), lea.readInt())) {
                        c.getCharacter().dropMessage(5, "An error occured when changing leader.");
                    }
                }
                break;
            case 8: //title update
                if (c.getCharacter().getAllianceRank() == 1 && leaderid == c.getCharacter().getID()) {
                    String[] ranks = new String[5];
                    for (int i = 0; i < 5; i++) {
                        ranks[i] = lea.readMapleAsciiString();
                    }
                    World.Alliance.updateAllianceRanks(gs.getAllianceId(), ranks);
                }
                break;
            case 9:
                if (c.getCharacter().getAllianceRank() <= 2) {
                    if (!World.Alliance.changeAllianceRank(gs.getAllianceId(), lea.readInt(), lea.readByte())) {
                        c.getCharacter().dropMessage(5, "An error occured when changing rank.");
                    }
                }
                break;
            case 10: //notice update
                if (c.getCharacter().getAllianceRank() <= 2) {
                    final String notice = lea.readMapleAsciiString();
                    if (notice.length() > 100) {
                        break;
                    }
                    World.Alliance.updateAllianceNotice(gs.getAllianceId(), notice);
                }
                break;
            default:
                System.out.println("Unhandled GuildAlliance op: " + op + ", \n" + lea.toString());
                break;
        }
        //c.getSession().write(CWvsContext.enableActions());
	}
	
	private static final void denyInvite(MapleClient c, final MapleGuild gs) { //playername that invited -> guildname that was invited but we also don't care
        final int inviteid = World.Guild.getInvitedId(c.getCharacter().getGuildId());
        if (inviteid > 0) {
            final int newAlliance = World.Alliance.getAllianceLeader(inviteid);
            if (newAlliance > 0) {
                final MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(newAlliance);
                if (chr != null) {
                    chr.dropMessage(5, gs.getName() + " Guild has rejected the Guild Union invitation.");
                }
                World.Guild.setInvitedId(c.getCharacter().getGuildId(), 0);
            }
        }
        //c.getSession().write(CWvsContext.enableActions());
    }

}
