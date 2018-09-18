package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.World;
import server.maps.Event_DojoAgent;
import server.quest.MapleQuest;
import tools.data.LittleEndianAccessor;

public class PartyOperationHandler extends AbstractMaplePacketHandler {

	public PartyOperationHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		int operation = lea.readByte();
        MapleParty party = c.getCharacter().getParty();
        MaplePartyCharacter player = new MaplePartyCharacter(c.getCharacter());

        switch (operation) {
            case 1: // Create party
            	lea.skip(1);
            	String partyName = lea.readMapleAsciiString();
                if (party == null) {
                    party = World.Party.createParty(player, partyName);
                    c.getCharacter().setParty(party);
                    c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId(), party.getName()));
                } else {
                    if (party.getExpeditionId() > 0) {
                        c.getCharacter().dropMessage(5, "You may not create a party while in a raid.");
                        return;
                    }
                    if ((player.equals(party.getLeader())) && (party.getMembers().size() == 1)) {
                        c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId(), party.getName()));
                    } else {
                        c.getCharacter().dropMessage(5, "You can't create a party because you are already in one.");
                    }
                }
                break;
            case 2: // Disband or leave party
                if (party == null) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getCharacter().dropMessage(5, "You may not leave a party while in a raid.");
                    return;
                }
                if (player.equals(party.getLeader()) && party.getMembers().size() > 1) {
                	//int leaderid = partyMember.getId();
                	//MaplePartyCharacter newLeader = null;
                	for(MaplePartyCharacter member : party.getMembers()) {
                		if(member.getId() != player.getId() && member.getId() != 0) {
                			//newLeader = member;
                			World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, member);
                			break;
                		}
                	}
                    //World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newLeader);
                    World.Party.updateParty(party.getId(), PartyOperation.LEAVE, player);
                    if (c.getCharacter().getEventInstance() != null) {
                        c.getCharacter().getEventInstance().leftParty(c.getCharacter());
                    }
                } else if (player.equals(party.getLeader())) {
                    if (GameConstants.isDojo(c.getCharacter().getMapId())) {
                        Event_DojoAgent.failed(c.getCharacter());
                    }
                    if (c.getCharacter().getPyramidSubway() != null) {
                        c.getCharacter().getPyramidSubway().fail(c.getCharacter());
                    }
                    World.Party.updateParty(party.getId(), PartyOperation.DISBAND, player);
                    if (c.getCharacter().getEventInstance() != null) {
                        c.getCharacter().getEventInstance().disbandParty();
                    }
                } else {
                    if (GameConstants.isDojo(c.getCharacter().getMapId())) {
                        Event_DojoAgent.failed(c.getCharacter());
                    }
                    if (c.getCharacter().getPyramidSubway() != null) {
                        c.getCharacter().getPyramidSubway().fail(c.getCharacter());
                    }
                    World.Party.updateParty(party.getId(), PartyOperation.LEAVE, player);
                    if (c.getCharacter().getEventInstance() != null) {
                        c.getCharacter().getEventInstance().leftParty(c.getCharacter());
                    }
                }
                c.getCharacter().setParty(null);
                break;
            case 3:
                int partyid = lea.readInt();
                if (party == null) {
                    party = World.Party.getParty(partyid);
                    if (party != null) {
                        if (party.getExpeditionId() > 0) {
                            c.getCharacter().dropMessage(5, "You may not do party operations while in a raid.");
                            return;
                        }
                        if ((party.getMembers().size() < 8) && (c.getCharacter().getQuestNoAdd(MapleQuest.getInstance(122901)) == null)) {
                            c.getCharacter().setParty(party);
                            World.Party.updateParty(party.getId(), PartyOperation.JOIN, player);
                            c.getCharacter().receivePartyMemberHP();
                            c.getCharacter().updatePartyMemberHP();
                        } else {
                            c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(22, null));
                        }
                    } else {
                        c.getCharacter().dropMessage(5, "The party you are trying to join does not exist");
                    }
                } else {
                    c.getCharacter().dropMessage(5, "You can't join the party as you are already in one");
                }
                break;
            case 4: // Party invitation
                if (party == null) {
                    party = World.Party.createParty(player);
                    c.getCharacter().setParty(party);
                    c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId(), party.getName()));
                }

                String inviteName = lea.readMapleAsciiString();
                MapleCharacter partyInvitee = World.getCharacterFromPlayerStorage(inviteName);
                if (partyInvitee != null) {
                	if ((partyInvitee.getParty() == null) && (partyInvitee.getQuestNoAdd(MapleQuest.getInstance(122901)) == null)) {
                        if (party.getExpeditionId() > 0) {
                            c.getCharacter().dropMessage(5, "You may not do party operations while in a raid.");
                            return;
                        }
                        if (party.getMembers().size() < 8) {
                            //c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(30, partyInvitee.getName()));
                        	c.getSession().write(CWvsContext.PartyPacket.partyRequestInvite(partyInvitee));
                            partyInvitee.getClient().getSession().write(CWvsContext.PartyPacket.partyInvite(c.getCharacter()));
                        } else {
                            c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(22, null));
                        }
                    } else {
                        //c.getPlayer().dropMessage(5, partyInvitee.getName() + " is already in a party.");
                        c.getCharacter().showMessage(11, partyInvitee.getName() + " is already in a party.");
                    }
                } else {
                    c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(17, null));
                }
                break;
            case 6: // Expel party member
                if ((party == null) || (player == null) || (!player.equals(party.getLeader()))) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getCharacter().dropMessage(5, "You may not do party operations while in a raid.");
                    return;
                }
                MaplePartyCharacter expelled = party.getMemberById(lea.readInt());
                if (expelled != null) {
                    if ((GameConstants.isDojo(c.getCharacter().getMapId())) && (expelled.isOnline())) {
                        Event_DojoAgent.failed(c.getCharacter());
                    }
                    if ((c.getCharacter().getPyramidSubway() != null) && (expelled.isOnline())) {
                        c.getCharacter().getPyramidSubway().fail(c.getCharacter());
                    }
                    World.Party.updateParty(party.getId(), PartyOperation.EXPEL, expelled);
                    if (c.getCharacter().getEventInstance() != null) {
                        if (expelled.isOnline()) {
                            c.getCharacter().getEventInstance().disbandParty();
                        }
                    }
                }
                break;
            case 7: // Change leader
                if (party == null) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getCharacter().dropMessage(5, "You may not do party operations while in a raid.");
                    return;
                }
                MaplePartyCharacter newLeader = party.getMemberById(lea.readInt());
                if ((newLeader != null) && (player.equals(party.getLeader()))) {
                    World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newLeader);
                }
                break;
            case 66://was 7
                if (party != null) {
                    if ((c.getCharacter().getEventInstance() != null) || (c.getCharacter().getPyramidSubway() != null) || (party.getExpeditionId() > 0) || (GameConstants.isDojo(c.getCharacter().getMapId()))) {
                        c.getCharacter().dropMessage(5, "You may not do party operations while in a raid.");
                        return;
                    }
                    if (player.equals(party.getLeader())) {
                        World.Party.updateParty(party.getId(), PartyOperation.DISBAND, player);
                    } else {
                        World.Party.updateParty(party.getId(), PartyOperation.LEAVE, player);
                    }
                    c.getCharacter().setParty(null);
                }
                int partyid_ = lea.readInt();
                party = World.Party.getParty(partyid_);
                if ((party == null) || (party.getMembers().size() >= 8)) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getCharacter().dropMessage(5, "You may not do party operations while in a raid.");
                    return;
                }
                MapleCharacter cfrom = c.getCharacter().getMap().getCharacterById(party.getLeader().getId());
                if ((cfrom != null) && (cfrom.getQuestNoAdd(MapleQuest.getInstance(122900)) == null)) {
                    c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(50, c.getCharacter().getName()));
                    cfrom.getClient().getSession().write(CWvsContext.PartyPacket.partyRequestInvite(c.getCharacter()));
                } else {
                    c.getCharacter().dropMessage(5, "Player was not found or player is not accepting party requests.");
                }
                break;
            case 8:
                if (lea.readByte() > 0) {
                    c.getCharacter().getQuestRemove(MapleQuest.getInstance(122900));
                } else {
                    c.getCharacter().getQuestNAdd(MapleQuest.getInstance(122900));
                }
                break;
            default:
                System.out.println("Unhandled Party function." + operation);
        }
	}

}
