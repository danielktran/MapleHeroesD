package net.channel.handler;

import client.MapleClient;
import client.character.MapleCharacter;
import constants.GameConstants;
import net.packet.CField;
import net.packet.CWvsContext;
import net.server.channel.ChannelServer;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.World;
import net.world.World.Find;
import net.world.exped.ExpeditionType;
import net.world.exped.MapleExpedition;
import net.world.exped.PartySearch;
import net.world.exped.PartySearchType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import server.maps.Event_DojoAgent;
import server.maps.FieldLimitType;
import server.quest.MapleQuest;
import tools.data.LittleEndianAccessor;

public class PartyHandler {

    public static void MemberSearch(LittleEndianAccessor slea, MapleClient c) {
        if ((c.getCharacter().isInBlockedMap()) || (FieldLimitType.VipRock.check(c.getCharacter().getMap().getFieldLimit()))) {
            c.getCharacter().dropMessage(5, "You may not do party search here.");
            return;
        }
        c.getSession().write(CWvsContext.PartyPacket.showMemberSearch(c.getCharacter().getMap().getCharactersThreadsafe()));
    }

    public static final void PartySearch(LittleEndianAccessor slea, MapleClient c) {
        if ((c.getCharacter().isInBlockedMap()) || (FieldLimitType.VipRock.check(c.getCharacter().getMap().getFieldLimit()))) {
            c.getCharacter().dropMessage(5, "You may not do party search here.");
            return;
        }
        List parties = new ArrayList();
        for (MapleCharacter chr : c.getCharacter().getMap().getCharactersThreadsafe()) {
            if ((chr.getParty() != null)
                    && (chr.getParty().getId() != c.getCharacter().getParty().getId()) && (!parties.contains(chr.getParty()))) {
                parties.add(chr.getParty());
            }
        }

        c.getSession().write(CWvsContext.PartyPacket.showPartySearch(parties));
    }

    public static final void PartyListing(LittleEndianAccessor slea, MapleClient c) {
        int mode = slea.readByte();
        PartySearchType pst;
        switch (mode) {
            case -105:
            case -97:
            case 81:
            case 159:
                pst = PartySearchType.getById(slea.readInt());
                if ((pst == null) || (c.getCharacter().getLevel() > pst.maxLevel) || (c.getCharacter().getLevel() < pst.minLevel)) {
                    return;
                }
                if ((c.getCharacter().getParty() == null) && (World.Party.searchParty(pst).size() < 10)) {
                    MapleParty party = World.Party.createParty(new MaplePartyCharacter(c.getCharacter()), pst.id);
                    c.getCharacter().setParty(party);
                    c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                    PartySearch ps = new PartySearch(slea.readMapleAsciiString(), pst.exped ? party.getExpeditionId() : party.getId(), pst);
                    World.Party.addSearch(ps);
                    if (pst.exped) {
                        c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(World.Party.getExped(party.getExpeditionId()), true, false));
                    }
                    c.getSession().write(CWvsContext.PartyPacket.partyListingAdded(ps));
                } else {
                    c.getCharacter().dropMessage(1, "Unable to create. Please leave the party.");
                }
                break;
            case -103:
            case -95:
            case 83:
            case 161:
                pst = PartySearchType.getById(slea.readInt());
                if ((pst == null) || (c.getCharacter().getLevel() > pst.maxLevel) || (c.getCharacter().getLevel() < pst.minLevel)) {
                    return;
                }
                c.getSession().write(CWvsContext.PartyPacket.getPartyListing(pst));
                break;
            case -102:
            case -94:
            case 84:
            case 162:
                break;
            case -101:
            case -93:
            case 85:
            case 163:
                MapleParty party = c.getCharacter().getParty();
                MaplePartyCharacter partyplayer = new MaplePartyCharacter(c.getCharacter());
                if (party != null) {
                    break;
                }
                int theId = slea.readInt();
                party = World.Party.getParty(theId);
                if (party != null) {
                    PartySearch ps = World.Party.getSearchByParty(party.getId());
                    if ((ps != null) && (c.getCharacter().getLevel() <= ps.getType().maxLevel) && (c.getCharacter().getLevel() >= ps.getType().minLevel) && (party.getMembers().size() < 8)) {
                        c.getCharacter().setParty(party);
                        World.Party.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                        c.getCharacter().receivePartyMemberHP();
                        c.getCharacter().updatePartyMemberHP();
                    } else {
                        c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(21, null));
                    }
                } else {
                    MapleExpedition exped = World.Party.getExped(theId);
                    if (exped != null) {
                        PartySearch ps = World.Party.getSearchByExped(exped.getId());
                        if ((ps != null) && (c.getCharacter().getLevel() <= ps.getType().maxLevel) && (c.getCharacter().getLevel() >= ps.getType().minLevel) && (exped.getAllMembers() < exped.getType().maxMembers)) {
                            int partyId = exped.getFreeParty();
                            if (partyId < 0) {
                                c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(21, null));
                            } else if (partyId == 0) {
                                party = World.Party.createPartyAndAdd(partyplayer, exped.getId());
                                c.getCharacter().setParty(party);
                                c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                                c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionJoined(c.getCharacter().getName()), null);
                                World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionUpdate(exped.getIndex(party.getId()), party), null);
                            } else {
                                c.getCharacter().setParty(World.Party.getParty(partyId));
                                World.Party.updateParty(partyId, PartyOperation.JOIN, partyplayer);
                                c.getCharacter().receivePartyMemberHP();
                                c.getCharacter().updatePartyMemberHP();
                                c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionJoined(c.getCharacter().getName()), null);
                            }
                        } else {
                            c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(0, c.getCharacter().getName()));
                        }
                    }
                }
                break;
            default:
                if (!c.getCharacter().isGM()) {
                    break;
                }
                System.out.println("Unknown PartyListing : " + mode + "\n" + slea);
        }
    }

    public static final void Expedition(LittleEndianAccessor slea, MapleClient c) {
        if ((c.getCharacter() == null) || (c.getCharacter().getMap() == null)) {
            return;
        }
        int mode = slea.readByte();
        String name;
        MapleParty part;
        MapleExpedition exped;
        int cid;
        Iterator i$;

        switch (mode) {
            case 76://64
            case 134:
                ExpeditionType et = ExpeditionType.getById(slea.readInt());
                if ((et != null) && (c.getCharacter().getParty() == null) && (c.getCharacter().getLevel() <= et.maxLevel) && (c.getCharacter().getLevel() >= et.minLevel)) {
                    MapleParty party = World.Party.createParty(new MaplePartyCharacter(c.getCharacter()), et.exped);
                    c.getCharacter().setParty(party);
                    c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                    c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(World.Party.getExped(party.getExpeditionId()), true, false));
                } else {
                    c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(0, ""));
                }
                break;
            case 77://65
            case 135:
                name = slea.readMapleAsciiString();
                int theCh = World.Find.findChannel(name);
                if (theCh > 0) {
                    MapleCharacter invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(name);
                    MapleParty party = c.getCharacter().getParty();
                    if ((invited != null) && (invited.getParty() == null) && (party != null) && (party.getExpeditionId() > 0)) {
                        MapleExpedition me = World.Party.getExped(party.getExpeditionId());
                        if ((me != null) && (me.getAllMembers() < me.getType().maxMembers) && (invited.getLevel() <= me.getType().maxLevel) && (invited.getLevel() >= me.getType().minLevel)) {
                            c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(7, invited.getName()));
                            invited.getClient().getSession().write(CWvsContext.ExpeditionPacket.expeditionInvite(c.getCharacter(), me.getType().exped));
                        } else {
                            c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(3, invited.getName()));
                        }
                    } else {
                        c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(2, name));
                    }
                } else {
                    c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(0, name));
                }
                break;
            case 78://66
            case 136:
                name = slea.readMapleAsciiString();
                int action = slea.readInt();
                int theChh = World.Find.findChannel(name);
                if (theChh <= 0) {
                    break;
                }
                MapleCharacter cfrom = ChannelServer.getInstance(theChh).getPlayerStorage().getCharacterByName(name);
                if ((cfrom != null) && (cfrom.getParty() != null) && (cfrom.getParty().getExpeditionId() > 0)) {
                    MapleParty party = cfrom.getParty();
                    exped = World.Party.getExped(party.getExpeditionId());
                    if ((exped != null) && (action == 8)) {
                        if ((c.getCharacter().getLevel() <= exped.getType().maxLevel) && (c.getCharacter().getLevel() >= exped.getType().minLevel) && (exped.getAllMembers() < exped.getType().maxMembers)) {
                            int partyId = exped.getFreeParty();
                            if (partyId < 0) {
                                c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(21, null));
                            } else if (partyId == 0) {
                                party = World.Party.createPartyAndAdd(new MaplePartyCharacter(c.getCharacter()), exped.getId());
                                c.getCharacter().setParty(party);
                                c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                                c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionJoined(c.getCharacter().getName()), null);
                                World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionUpdate(exped.getIndex(party.getId()), party), null);
                            } else {
                                c.getCharacter().setParty(World.Party.getParty(partyId));
                                World.Party.updateParty(partyId, PartyOperation.JOIN, new MaplePartyCharacter(c.getCharacter()));
                                c.getCharacter().receivePartyMemberHP();
                                c.getCharacter().updatePartyMemberHP();
                                c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, false, false));
                                World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionJoined(c.getCharacter().getName()), null);
                            }
                        } else {
                            c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(3, cfrom.getName()));
                        }
                    } else if (action == 9) {
                        cfrom.getClient().getSession().write(CWvsContext.PartyPacket.partyStatusMessage(23, c.getCharacter().getName()));
                    }
                }
                break;
            case 79://67
            case 137:
                part = c.getCharacter().getParty();
                if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                exped = World.Party.getExped(part.getExpeditionId());
                if (exped != null) {
                    if (GameConstants.isDojo(c.getCharacter().getMapId())) {
                        Event_DojoAgent.failed(c.getCharacter());
                    }
                    if (exped.getLeader() == c.getCharacter().getID()) {
                        World.Party.disbandExped(exped.getId());
                        if (c.getCharacter().getEventInstance() != null) {
                            c.getCharacter().getEventInstance().disbandParty();
                        }
                    } else if (part.getLeader().getId() == c.getCharacter().getID()) {
                        World.Party.updateParty(part.getId(), PartyOperation.DISBAND, new MaplePartyCharacter(c.getCharacter()));
                        if (c.getCharacter().getEventInstance() != null) {
                            c.getCharacter().getEventInstance().disbandParty();
                        }
                        World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionLeft(c.getCharacter().getName()), null);
                    } else {
                        World.Party.updateParty(part.getId(), PartyOperation.LEAVE, new MaplePartyCharacter(c.getCharacter()));
                        if (c.getCharacter().getEventInstance() != null) {
                            c.getCharacter().getEventInstance().leftParty(c.getCharacter());
                        }
                        World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionLeft(c.getCharacter().getName()), null);
                    }
                    if (c.getCharacter().getPyramidSubway() != null) {
                        c.getCharacter().getPyramidSubway().fail(c.getCharacter());
                    }
                    c.getCharacter().setParty(null);
                }
                break;
            case 80://68
            case 138:
                part = c.getCharacter().getParty();
                if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                exped = World.Party.getExped(part.getExpeditionId());
                if ((exped != null) && (exped.getLeader() == c.getCharacter().getID())) {
                    cid = slea.readInt();
                    for (i$ = exped.getParties().iterator(); i$.hasNext();) {
                        int i = ((Integer) i$.next()).intValue();
                        MapleParty par = World.Party.getParty(i);
                        if (par != null) {
                            MaplePartyCharacter expelled = par.getMemberById(cid);
                            if (expelled != null) {
                                if ((expelled.isOnline()) && (GameConstants.isDojo(c.getCharacter().getMapId()))) {
                                    Event_DojoAgent.failed(c.getCharacter());
                                }
                                World.Party.updateParty(i, PartyOperation.EXPEL, expelled);
                                if ((c.getCharacter().getEventInstance() != null)
                                        && (expelled.isOnline())) {
                                    c.getCharacter().getEventInstance().disbandParty();
                                }

                                if ((c.getCharacter().getPyramidSubway() != null) && (expelled.isOnline())) {
                                    c.getCharacter().getPyramidSubway().fail(c.getCharacter());
                                }
                                World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionLeft(expelled.getName()), null);
                                break;
                            }
                        }
                    }
                }
                break;
            case 81://69
            case 139:
                part = c.getCharacter().getParty();
                if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                exped = World.Party.getExped(part.getExpeditionId());
                if ((exped != null) && (exped.getLeader() == c.getCharacter().getID())) {
                    MaplePartyCharacter newleader = part.getMemberById(slea.readInt());
                    if (newleader != null) {
                        World.Party.updateParty(part.getId(), PartyOperation.CHANGE_LEADER, newleader);
                        exped.setLeader(newleader.getId());
                        World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionLeaderChanged(0), null);
                    }
                }
                break;
            case 82://70
            case 140:
                part = c.getCharacter().getParty();
                if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                exped = World.Party.getExped(part.getExpeditionId());
                if ((exped != null) && (exped.getLeader() == c.getCharacter().getID())) {
                    cid = slea.readInt();
                    for (i$ = exped.getParties().iterator(); i$.hasNext();) {
                        int i = ((Integer) i$.next()).intValue();
                        MapleParty par = World.Party.getParty(i);
                        if (par != null) {
                            MaplePartyCharacter newleader = par.getMemberById(cid);
                            if ((newleader != null) && (par.getId() != part.getId())) {
                                World.Party.updateParty(par.getId(), PartyOperation.CHANGE_LEADER, newleader);
                            }
                        }
                    }
                }
                break;
            case 83://71
            case 141:
                part = c.getCharacter().getParty();
                if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                exped = World.Party.getExped(part.getExpeditionId());
                if ((exped != null) && (exped.getLeader() == c.getCharacter().getID())) {
                    int partyIndexTo = slea.readInt();
                    if ((partyIndexTo < exped.getType().maxParty) && (partyIndexTo <= exped.getParties().size())) {
                        cid = slea.readInt();
                        for (i$ = exped.getParties().iterator(); i$.hasNext();) {
                            int i = ((Integer) i$.next()).intValue();
                            MapleParty par = World.Party.getParty(i);
                            if (par != null) {
                                MaplePartyCharacter expelled = par.getMemberById(cid);
                                if ((expelled != null) && (expelled.isOnline())) {
                                    MapleCharacter chr = World.getStorage(expelled.getChannel()).getCharacterById(expelled.getId());
                                    if (chr == null) {
                                        break;
                                    }
                                    if (partyIndexTo < exped.getParties().size()) {
                                        MapleParty party = World.Party.getParty((exped.getParties().get(partyIndexTo)).intValue());
                                        if ((party == null) || (party.getMembers().size() >= 8)) {
                                            c.getCharacter().dropMessage(5, "Invalid party.");
                                            break;
                                        }
                                    }
                                    if (GameConstants.isDojo(c.getCharacter().getMapId())) {
                                        Event_DojoAgent.failed(c.getCharacter());
                                    }
                                    World.Party.updateParty(i, PartyOperation.EXPEL, expelled);
                                    if (partyIndexTo < exped.getParties().size()) {
                                        MapleParty party = World.Party.getParty((exped.getParties().get(partyIndexTo)).intValue());
                                        if ((party != null) && (party.getMembers().size() < 8)) {
                                            World.Party.updateParty(party.getId(), PartyOperation.JOIN, expelled);
                                            chr.receivePartyMemberHP();
                                            chr.updatePartyMemberHP();
                                            chr.getClient().getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                        }
                                    } else {
                                        MapleParty party = World.Party.createPartyAndAdd(expelled, exped.getId());
                                        chr.setParty(party);
                                        chr.getClient().getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                                        chr.getClient().getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                        World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionUpdate(exped.getIndex(party.getId()), party), null);
                                    }
                                    if ((c.getCharacter().getEventInstance() != null)
                                            && (expelled.isOnline())) {
                                        c.getCharacter().getEventInstance().disbandParty();
                                    }

                                    if (c.getCharacter().getPyramidSubway() == null) {
                                        break;
                                    }
                                    c.getCharacter().getPyramidSubway().fail(c.getCharacter());
                                    break;
                                }
                            }
                        }
                    }

                }

                break;
            default:
                if (!c.getCharacter().isGM()) {
                    break;
                }
                System.out.println("Unknown Expedition : " + mode + "\n" + slea);
        }
    }
}
