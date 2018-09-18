package net.server.channel.handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext.GuildPacket;
import net.world.World;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildResponse;
import server.MapleStatEffect;
import tools.Pair;
import tools.data.LittleEndianAccessor;

public class GuildOperationHandler extends AbstractMaplePacketHandler {

	private static final Map<String, Pair<Integer, Long>> invited = new HashMap<>();
    private static long nextPruneTime = System.currentTimeMillis() + 5 * 60 * 1000;
    
	public GuildOperationHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final long currentTime = System.currentTimeMillis();
		int guildId, charid;
		String charName;
        if (currentTime >= nextPruneTime) {
            Iterator<Entry<String, Pair<Integer, Long>>> itr = getInvited().entrySet().iterator();
            Entry<String, Pair<Integer, Long>> inv;
            while (itr.hasNext()) {
                inv = itr.next();
                if (currentTime >= inv.getValue().right) {
                    itr.remove();
                }
            }
            nextPruneTime += 5 * 60 * 1000;
        }

        switch (lea.readByte()) {
        	case 1: // Accept Guild Invitation
	            if (c.getCharacter().getGuildId() > 0) {
	            	c.getCharacter().dropMessage(1, "You have already joined a guild.");
	                return;
	            }
	            guildId = lea.readInt(); // guild leader id or inviter id?

	            charName = c.getCharacter().getName().toLowerCase();
	            Pair<Integer, Long> gid = getInvited().remove(charName);
	            if (gid != null && guildId == gid.left) {
	                c.getCharacter().setGuildId(guildId);
	                c.getCharacter().setGuildRank((byte) 5);
	                int s = World.Guild.addGuildMember(c.getCharacter().getMGC());
	                if (s == 0) {
	                    c.getCharacter().dropMessage(1, "The guild you are trying to join is already full.");
	                    c.getCharacter().setGuildId(0);
	                    return;
	                }
	                c.getSession().write(GuildPacket.showGuildInfo(c.getCharacter()));
	                final MapleGuild gs = World.Guild.getGuild(guildId);
	                for (byte[] pack : World.Alliance.getAllianceInfo(gs.getAllianceId(), true)) {
	                    if (pack != null) {
	                        c.getSession().write(pack);
	                    }
	                }
	                c.getCharacter().saveGuildStatus();
	                respawnPlayer(c.getCharacter());
	            }
	            break;
            case 4: // Create Guild
                if (c.getCharacter().getGuildId() > 0 || c.getCharacter().getMapId() != 200000301) {
                    c.getCharacter().dropMessage(1, "You cannot create a new guild while in one.");
                    return;
                } else if (c.getCharacter().getMeso() < 500000) {
                    c.getCharacter().dropMessage(1, "You do not have enough mesos to create a guild.");
                    return;
                }
                final String guildName = lea.readMapleAsciiString();

                if (!isGuildNameAcceptable(guildName)) {
                    c.getCharacter().dropMessage(1, "The guild name you have chosen is not acceptable.");
                    return;
                }
                guildId = World.Guild.createGuild(c.getCharacter().getID(), guildName);
                if (guildId == 0) {
                    c.getCharacter().dropMessage(1, "An error occured when trying to create a new guild. Please try again.");
                    return;
                }
                c.getCharacter().gainMeso(-5000000, true, true);
                c.getCharacter().setGuildId(guildId);
                c.getCharacter().setGuildRank((byte) 1);
                c.getCharacter().saveGuildStatus();
                c.getCharacter().finishAchievement(35);
                World.Guild.setGuildMemberOnline(c.getCharacter().getMGC(), true, c.getChannel());
                //c.getSession().write(GuildPacket.showGuildInfo(c.getPlayer()));
                c.getSession().write(GuildPacket.newGuildInfo(c.getCharacter()));
                World.Guild.gainGP(c.getCharacter().getGuildId(), 500, c.getCharacter().getID());
                //c.getPlayer().dropMessage(1, "You have successfully created a Guild.");
                respawnPlayer(c.getCharacter());
                break;
            case 7: // Invite Player to Guild
                if (c.getCharacter().getGuildId() <= 0 || c.getCharacter().getGuildRank() > 2) { // 1 == guild master, 2 == jr
                    return;
                }
                charName = lea.readMapleAsciiString().toLowerCase();
                if (getInvited().containsKey(charName)) {
                    c.getCharacter().dropMessage(5, "The player is currently handling an invitation.");
                    //return;
                }
                final MapleGuildResponse mgr = MapleGuild.sendInvite(c, charName);

                if (mgr != null) {
                    c.getSession().write(mgr.getPacket());
                } else {
                	Pair<Integer, Long> put = getInvited().put(charName, new Pair<>(c.getCharacter().getGuildId(), currentTime + (20 * 60000))); //20 mins expire
                }
                break;
            
            case 11: // Leave Guild
                charid = lea.readInt();
                charName = lea.readMapleAsciiString();

                if (charid != c.getCharacter().getID() || !charName.equals(c.getCharacter().getName()) || c.getCharacter().getGuildId() <= 0) {
                    return;
                }
                World.Guild.leaveGuild(c.getCharacter().getMGC());
                respawnPlayer(c.getCharacter());
                //c.getSession().write(GuildPacket.showGuildInfo(null));
                break;
            case 12: // Expel Member from Guild
                charid = lea.readInt();
                charName = lea.readMapleAsciiString();

                if (c.getCharacter().getGuildRank() > 2 || c.getCharacter().getGuildId() <= 0) {
                    return;
                }
                World.Guild.expelMember(c.getCharacter().getMGC(), charName, charid);
                break;
            case 18: // Change Guild Rank Titles
                if (c.getCharacter().getGuildId() <= 0 || c.getCharacter().getGuildRank() != 1) {
                    return;
                }
                String ranks[] = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = lea.readMapleAsciiString();
                }

                World.Guild.changeRankTitle(c.getCharacter().getGuildId(), ranks);
                break;
            case 19: // Change Guild Member's Rank
                charid = lea.readInt();
                byte newRank = lea.readByte();

                if ((newRank <= 1 || newRank > 5) || c.getCharacter().getGuildRank() > 2 || (newRank <= 2 && c.getCharacter().getGuildRank() != 1) || c.getCharacter().getGuildId() <= 0) {
                    return;
                }

                World.Guild.changeRank(c.getCharacter().getGuildId(), charid, newRank);
                break;
            case 20: // Change Guild Emblem
                if (c.getCharacter().getGuildId() <= 0 || c.getCharacter().getGuildRank() != 1) {
                    return;
                }

                if (c.getCharacter().getMeso() < 1500000) {
                    c.getCharacter().dropMessage(1, "You do not have enough mesos to create an emblem.");
                    return;
                }
                final short bg = lea.readShort();
                final byte bgcolor = lea.readByte();
                final short logo = lea.readShort();
                final byte logocolor = lea.readByte();

                World.Guild.setGuildEmblem(c.getCharacter().getGuildId(), bg, bgcolor, logo, logocolor);

                c.getCharacter().gainMeso(-1500000, true, true);
                respawnPlayer(c.getCharacter());
                break;
            case 0x11: // guild notice change
                final String notice = lea.readMapleAsciiString();
                if (notice.length() > 100 || c.getCharacter().getGuildId() <= 0 || c.getCharacter().getGuildRank() > 2) {
                    return;
                }
                World.Guild.setGuildNotice(c.getCharacter().getGuildId(), notice);
                break;
            case 0x1d: //guild skill purchase
                Skill skilli = SkillFactory.getSkill(lea.readInt());
                if (c.getCharacter().getGuildId() <= 0 || skilli == null || skilli.getId() < 91000000) {
                    return;
                }
                int eff = World.Guild.getSkillLevel(c.getCharacter().getGuildId(), skilli.getId()) + 1;
                if (eff > skilli.getMaxLevel()) {
                    return;
                }
                final MapleStatEffect skillid = skilli.getEffect(eff);
                if (skillid.getReqGuildLevel() <= 0 || c.getCharacter().getMeso() < skillid.getPrice()) {
                    return;
                }
                if (World.Guild.purchaseSkill(c.getCharacter().getGuildId(), skillid.getSourceId(), c.getCharacter().getName(), c.getCharacter().getID())) {
                    c.getCharacter().gainMeso(-skillid.getPrice(), true);
                }
                break;
            case 0x1e: //guild skill activation
                skilli = SkillFactory.getSkill(lea.readInt());
                if (c.getCharacter().getGuildId() <= 0 || skilli == null) {
                    return;
                }
                eff = World.Guild.getSkillLevel(c.getCharacter().getGuildId(), skilli.getId());
                if (eff <= 0) {
                    return;
                }
                final MapleStatEffect skillii = skilli.getEffect(eff);
                if (skillii.getReqGuildLevel() < 0 || c.getCharacter().getMeso() < skillii.getExtendPrice()) {
                    return;
                }
                if (World.Guild.activateSkill(c.getCharacter().getGuildId(), skillii.getSourceId(), c.getCharacter().getName())) {
                    c.getCharacter().gainMeso(-skillii.getExtendPrice(), true);
                }
                break;
            case 0x1f: //guild leader change
                charid = lea.readInt();
                if (c.getCharacter().getGuildId() <= 0 || c.getCharacter().getGuildRank() > 1) {
                    return;
                }
                World.Guild.setGuildLeader(c.getCharacter().getGuildId(), charid);
                break;
        }
	}
	
	private static boolean isGuildNameAcceptable(final String name) {
        if (name.length() < 3 || name.length() > 12) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLowerCase(name.charAt(i)) && !Character.isUpperCase(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }
	
	private static void respawnPlayer(final MapleCharacter mc) {
        if (mc.getMap() == null) {
            return;
        }
        mc.getMap().broadcastMessage(CField.loadGuildName(mc));
        mc.getMap().broadcastMessage(CField.loadGuildIcon(mc));
    }

	public static Map<String, Pair<Integer, Long>> getInvited() {
		return invited;
	}

}
