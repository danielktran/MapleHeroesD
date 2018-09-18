package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.World;
import net.world.World.Find;
import server.quest.MapleQuest;
import tools.data.LittleEndianAccessor;

public class PartyRequestHandler extends AbstractMaplePacketHandler {

	public PartyRequestHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		int action = lea.readByte();
        /*
        if ((action == 50)) {
        	System.out.println("Debug 0");
            MapleCharacter chr = c.getPlayer().getMap().getCharacterById(slea.readInt());
            if ((chr != null) && (chr.getParty() == null) && (c.getPlayer().getParty() != null) && (c.getPlayer().getParty().getLeader().getId() == c.getPlayer().getId()) && (c.getPlayer().getParty().getMembers().size() < 8) && (c.getPlayer().getParty().getExpeditionId() <= 0) && (chr.getQuestNoAdd(MapleQuest.getInstance(122901)) == null) && (c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(122900)) == null)) {
                chr.setParty(c.getPlayer().getParty());
                World.Party.updateParty(c.getPlayer().getParty().getId(), PartyOperation.JOIN, new MaplePartyCharacter(chr));
                chr.receivePartyMemberHP();
                chr.updatePartyMemberHP();
            }
            return;
        }
        */
        int charid = lea.readInt();
        if ((c.getCharacter().getParty() == null) && (c.getCharacter().getQuestNoAdd(MapleQuest.getInstance(122901)) == null)) {
        	int ch = Find.findChannel(charid);
        	MapleCharacter partyRequester = World.getStorage(ch).getCharacterById(charid);
            MapleParty party = World.Party.getParty(partyRequester.getParty().getId());
            if (party != null) {
                if (party.getExpeditionId() > 0) {
                    c.getCharacter().dropMessage(5, "You may not do party operations while in a raid.");
                    return;
                }
                if (action == 38) { // v171 = 38; This action is when a player accepts the party invitation.
                    if (party.getMembers().size() < 8) {
                        c.getCharacter().setParty(party);
                        World.Party.updateParty(partyRequester.getParty().getId(), PartyOperation.JOIN, new MaplePartyCharacter(c.getCharacter()));
                        c.getCharacter().receivePartyMemberHP();
                        c.getCharacter().updatePartyMemberHP();
                    } else {
                        c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(22, null));
                    }
                } else if (action == 37) { // v171 = 37; This action is when the player denies the party invitation.
                	if(partyRequester != null) {
                		//partyRequester.getClient().getPlayer().dropMessage(5, c.getPlayer().getName() + " has denied the party request.");
                		partyRequester.showMessage(11, c.getCharacter().getName() + " has denied the party request.");
                	}
                } else {
                    //MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterById(party.getLeader().getId());
                    if (partyRequester != null) {
                    	//partyRequester.getClient().getSession().write(CWvsContext.PartyPacket.partyStatusMessage(23, c.getPlayer().getName()));
                    }
                }
            } else {
                c.getCharacter().dropMessage(5, "The party you are trying to join does not exist");
            }
        } else {
            c.getCharacter().dropMessage(5, "You can't join the party as you are already in one");
        }
	}

}
