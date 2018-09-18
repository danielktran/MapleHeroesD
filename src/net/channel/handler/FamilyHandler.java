/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.channel.handler;

import client.MapleCharacterUtil;
import client.MapleClient;
import client.character.MapleCharacter;
import net.packet.CWvsContext;
import net.packet.CWvsContext.FamilyPacket;
import net.world.World;
import net.world.family.MapleFamily;
import net.world.family.MapleFamilyBuff;
import net.world.family.MapleFamilyCharacter;

import java.util.List;
import server.maps.FieldLimitType;
import tools.data.LittleEndianAccessor;

public class FamilyHandler {

    public static final void RequestFamily(final LittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
        if (chr != null) {
            c.getSession().write(FamilyPacket.getFamilyPedigree(chr));
        }
    }

    public static final void OpenFamily(final LittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(FamilyPacket.getFamilyInfo(c.getCharacter()));
    }

    public static final void UseFamily(final LittleEndianAccessor slea, MapleClient c) {
        int type = slea.readInt();
        if (MapleFamilyBuff.values().length <= type) {
            return;
        }
        MapleFamilyBuff entry = MapleFamilyBuff.values()[type];
        boolean success = c.getCharacter().getFamilyId() > 0 && c.getCharacter().canUseFamilyBuff(entry) && c.getCharacter().getCurrentRep() > entry.rep;
        if (!success) {
            return;
        }
        MapleCharacter victim = null;
        switch (entry) {
            case Teleport: //teleport: need add check for if not a safe place
                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                if (FieldLimitType.VipRock.check(c.getCharacter().getMap().getFieldLimit()) || c.getCharacter().isInBlockedMap()) {
                    c.getCharacter().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
                    success = false;
                } else if (victim == null || (victim.isGM() && !c.getCharacter().isGM())) {
                    c.getCharacter().dropMessage(1, "Invalid name or you are not on the same channel.");
                    success = false;
                } else if (victim.getFamilyId() == c.getCharacter().getFamilyId() && !FieldLimitType.VipRock.check(victim.getMap().getFieldLimit()) && victim.getID() != c.getCharacter().getID() && !victim.isInBlockedMap()) {
                    c.getCharacter().changeMap(victim.getMap(), victim.getMap().getPortal(0));
                } else {
                    c.getCharacter().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
                    success = false;
                }
                break;
            case Summon: // TODO give a check to the player being forced somewhere else..
                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                if (FieldLimitType.VipRock.check(c.getCharacter().getMap().getFieldLimit()) || c.getCharacter().isInBlockedMap()) {
                    c.getCharacter().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
                } else if (victim == null || (victim.isGM() && !c.getCharacter().isGM())) {
                    c.getCharacter().dropMessage(1, "Invalid name or you are not on the same channel.");
                } else if (victim.getTeleportName().length() > 0) {
                    c.getCharacter().dropMessage(1, "Another character has requested to summon this character. Please try again later.");
                } else if (victim.getFamilyId() == c.getCharacter().getFamilyId() && !FieldLimitType.VipRock.check(victim.getMap().getFieldLimit()) && victim.getID() != c.getCharacter().getID() && !victim.isInBlockedMap()) {
                    victim.getClient().getSession().write(FamilyPacket.familySummonRequest(c.getCharacter().getName(), c.getCharacter().getMap().getMapName()));
                    victim.setTeleportName(c.getCharacter().getName());
                } else {
                    c.getCharacter().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
                }
                return; //RETURN not break
            case Drop_12_15: // drop rate + 50% 15 min
            case EXP_12_15: // exp rate + 50% 15 min
            case Drop_12_30: // drop rate + 100% 15 min
            //case EXP_12_30: // exp rate + 100% 15 min
            case Drop_15_15:
                //case Drop_15_30:
                //c.getSession().write(FamilyPacket.familyBuff(entry.type, type, entry.effect, entry.duration*60000));
                entry.applyTo(c.getCharacter());
                break;
            case Bonding: // 6 family members in pedigree online Drop Rate & Exp Rate + 100% 30 minutes
                final MapleFamily fam = World.Family.getFamily(c.getCharacter().getFamilyId());
                List<MapleFamilyCharacter> chrs = fam.getMFC(c.getCharacter().getID()).getOnlineJuniors(fam);
                if (chrs.size() < 7) {
                    success = false;
                } else {
                    for (MapleFamilyCharacter chrz : chrs) {
                        int chr = World.Find.findChannel(chrz.getId());
                        if (chr == -1) {
                            continue; //STOP WTF?! take reps though..
                        }
                        MapleCharacter chrr = World.getStorage(chr).getCharacterById(chrz.getId());
                        entry.applyTo(chrr);
                        //chrr.getClient().getSession().write(FamilyPacket.familyBuff(entry.type, type, entry.effect, entry.duration*60000));
                    }
                }
                break;
            /*case EXP_Party:
             case Drop_Party_12: // drop rate + 100% party 30 min
             case Drop_Party_15: // exp rate + 100% party 30 min
             entry.applyTo(c.getPlayer());
             //c.getSession().write(FamilyPacket.familyBuff(entry.type, type, entry.effect, entry.duration*60000));
             if (c.getPlayer().getParty() != null ) {
             for (MaplePartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
             if (mpc.getId() != c.getPlayer().getId()) {
             MapleCharacter chr = c.getPlayer().getMap().getCharacterById(mpc.getId());
             if (chr != null) {
             entry.applyTo(chr);
             //chr.getClient().getSession().write(FamilyPacket.familyBuff(entry.type, type, entry.effect, entry.duration*60000));
             }
             }
             }
             }
             break;*/
        }
        if (success) { //again
            c.getCharacter().setCurrentRep(c.getCharacter().getCurrentRep() - entry.rep);
            c.getSession().write(FamilyPacket.changeRep(-entry.rep, c.getCharacter().getName()));
            c.getCharacter().useFamilyBuff(entry);
        } else {
            c.getCharacter().dropMessage(5, "An error occured.");
        }
    }

    public static final void FamilyOperation(final LittleEndianAccessor slea, MapleClient c) {
        if (c.getCharacter() == null) {
            return;
        }
        MapleCharacter addChr = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
        if (addChr == null) {
            c.getCharacter().dropMessage(1, "The name you requested is incorrect or he/she is currently not logged in.");
        } else if (addChr.getFamilyId() == c.getCharacter().getFamilyId() && addChr.getFamilyId() > 0) {
            c.getCharacter().dropMessage(1, "You belong to the same family.");
        } else if (addChr.getMapId() != c.getCharacter().getMapId()) {
            c.getCharacter().dropMessage(1, "The one you wish to add as a junior must be in the same map.");
        } else if (addChr.getSeniorId() != 0) {
            c.getCharacter().dropMessage(1, "The character is already a junior of another character.");
        } else if (addChr.getLevel() >= c.getCharacter().getLevel()) {
            c.getCharacter().dropMessage(1, "The junior you wish to add must be at a lower rank.");
        } else if (addChr.getLevel() < c.getCharacter().getLevel() - 20) {
            c.getCharacter().dropMessage(1, "The gap between you and your junior must be within 20 levels.");
            //} else if (c.getPlayer().getFamilyId() != 0 && c.getPlayer().getFamily().getGens() >= 1000) {
            //	c.getPlayer().dropMessage(5, "Your family cannot extend more than 1000 generations from above and below.");
        } else if (addChr.getLevel() < 10) {
            c.getCharacter().dropMessage(1, "The junior you wish to add must be over Level 10.");
        } else if (c.getCharacter().getJunior1() > 0 && c.getCharacter().getJunior2() > 0) {
            c.getCharacter().dropMessage(1, "You have 2 juniors already.");
        } else if (c.getCharacter().isGM() || !addChr.isGM()) {
            addChr.getClient().getSession().write(FamilyPacket.sendFamilyInvite(c.getCharacter().getID(), c.getCharacter().getLevel(), c.getCharacter().getJob(), c.getCharacter().getName()));
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void FamilyPrecept(final LittleEndianAccessor slea, MapleClient c) {
        MapleFamily fam = World.Family.getFamily(c.getCharacter().getFamilyId());
        if (fam == null || fam.getLeaderId() != c.getCharacter().getID()) {
            return;
        }
        fam.setNotice(slea.readMapleAsciiString());
    }

    public static final void FamilySummon(final LittleEndianAccessor slea, MapleClient c) {
        MapleFamilyBuff cost = MapleFamilyBuff.Summon;
        MapleCharacter tt = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
        if (c.getCharacter().getFamilyId() > 0 && tt != null && tt.getFamilyId() == c.getCharacter().getFamilyId() && !FieldLimitType.VipRock.check(tt.getMap().getFieldLimit())
                && !FieldLimitType.VipRock.check(c.getCharacter().getMap().getFieldLimit()) && tt.canUseFamilyBuff(cost)
                && c.getCharacter().getTeleportName().equals(tt.getName()) && tt.getCurrentRep() > cost.rep && !c.getCharacter().isInBlockedMap() && !tt.isInBlockedMap()) {
            //whew lots of checks
            boolean accepted = slea.readByte() > 0;
            if (accepted) {
                c.getCharacter().changeMap(tt.getMap(), tt.getMap().getPortal(0));
                tt.setCurrentRep(tt.getCurrentRep() - cost.rep);
                tt.getClient().getSession().write(FamilyPacket.changeRep(-cost.rep, tt.getName()));
                tt.useFamilyBuff(cost);
            } else {
                tt.dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
            }
        } else {
            c.getCharacter().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
        }
        c.getCharacter().setTeleportName("");
    }

    public static final void DeleteJunior(final LittleEndianAccessor slea, MapleClient c) {
        int juniorid = slea.readInt();
        if (c.getCharacter().getFamilyId() <= 0 || juniorid <= 0 || (c.getCharacter().getJunior1() != juniorid && c.getCharacter().getJunior2() != juniorid)) {
            return;
        }
        //junior is not required to be online.
        final MapleFamily fam = World.Family.getFamily(c.getCharacter().getFamilyId());
        final MapleFamilyCharacter other = fam.getMFC(juniorid);
        if (other == null) {
            return;
        }
        final MapleFamilyCharacter oth = c.getCharacter().getMFC();
        boolean junior2 = oth.getJunior2() == juniorid;
        if (junior2) {
            oth.setJunior2(0);
        } else {
            oth.setJunior1(0);
        }
        c.getCharacter().saveFamilyStatus();
        other.setSeniorId(0);
        //if (!other.isOnline()) {
        MapleFamily.setOfflineFamilyStatus(other.getFamilyId(), other.getSeniorId(), other.getJunior1(), other.getJunior2(), other.getCurrentRep(), other.getTotalRep(), other.getId());
        //}
        MapleCharacterUtil.sendNote(other.getName(), c.getCharacter().getName(), c.getCharacter().getName() + " has requested to sever ties with you, so the family relationship has ended.", 0);
        if (!fam.splitFamily(juniorid, other)) { //juniorid splits to make their own family. function should handle the rest
            if (!junior2) {
                fam.resetDescendants();
            }
            fam.resetPedigree();
        }
        c.getCharacter().dropMessage(1, "Broke up with (" + other.getName() + ").\r\nFamily relationship has ended.");
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void DeleteSenior(final LittleEndianAccessor slea, MapleClient c) {
        if (c.getCharacter().getFamilyId() <= 0 || c.getCharacter().getSeniorId() <= 0) {
            return;
        }
        //not required to be online
        final MapleFamily fam = World.Family.getFamily(c.getCharacter().getFamilyId()); //this is old family
        final MapleFamilyCharacter mgc = fam.getMFC(c.getCharacter().getSeniorId());
        final MapleFamilyCharacter mgc_ = c.getCharacter().getMFC();
        mgc_.setSeniorId(0);
        boolean junior2 = mgc.getJunior2() == c.getCharacter().getID();
        if (junior2) {
            mgc.setJunior2(0);
        } else {
            mgc.setJunior1(0);
        }
        //if (!mgc.isOnline()) {
        MapleFamily.setOfflineFamilyStatus(mgc.getFamilyId(), mgc.getSeniorId(), mgc.getJunior1(), mgc.getJunior2(), mgc.getCurrentRep(), mgc.getTotalRep(), mgc.getId());
        //}
        c.getCharacter().saveFamilyStatus();
        MapleCharacterUtil.sendNote(mgc.getName(), c.getCharacter().getName(), c.getCharacter().getName() + " has requested to sever ties with you, so the family relationship has ended.", 0);
        if (!fam.splitFamily(c.getCharacter().getID(), mgc_)) { //now, we're the family leader
            if (!junior2) {
                fam.resetDescendants();
            }
            fam.resetPedigree();
        }
        c.getCharacter().dropMessage(1, "Broke up with (" + mgc.getName() + ").\r\nFamily relationship has ended.");
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void AcceptFamily(LittleEndianAccessor slea, MapleClient c) {
        MapleCharacter inviter = c.getCharacter().getMap().getCharacterById(slea.readInt());
        if (inviter != null && c.getCharacter().getSeniorId() == 0 && (c.getCharacter().isGM() || !inviter.isHidden()) && inviter.getLevel() - 20 <= c.getCharacter().getLevel() && inviter.getLevel() >= 10 && inviter.getName().equals(slea.readMapleAsciiString()) && inviter.getNoJuniors() < 2 /*&& inviter.getFamily().getGens() < 1000*/ && c.getCharacter().getLevel() >= 10) {
            boolean accepted = slea.readByte() > 0;
            inviter.getClient().getSession().write(FamilyPacket.sendFamilyJoinResponse(accepted, c.getCharacter().getName()));
            if (accepted) {
                //c.getSession().write(FamilyPacket.sendFamilyMessage(0));
                c.getSession().write(FamilyPacket.getSeniorMessage(inviter.getName()));
                int old = c.getCharacter().getMFC() == null ? 0 : c.getCharacter().getMFC().getFamilyId();
                int oldj1 = c.getCharacter().getMFC() == null ? 0 : c.getCharacter().getMFC().getJunior1();
                int oldj2 = c.getCharacter().getMFC() == null ? 0 : c.getCharacter().getMFC().getJunior2();
                if (inviter.getFamilyId() > 0 && World.Family.getFamily(inviter.getFamilyId()) != null) {
                    MapleFamily fam = World.Family.getFamily(inviter.getFamilyId());
                    //if old isn't null, don't set the familyid yet, mergeFamily will take care of it
                    c.getCharacter().setFamily(old <= 0 ? inviter.getFamilyId() : old, inviter.getID(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2);
                    MapleFamilyCharacter mf = inviter.getMFC();
                    if (mf.getJunior1() > 0) {
                        mf.setJunior2(c.getCharacter().getID());
                    } else {
                        mf.setJunior1(c.getCharacter().getID());
                    }
                    inviter.saveFamilyStatus();
                    if (old > 0 && World.Family.getFamily(old) != null) { //has junior
                        MapleFamily.mergeFamily(fam, World.Family.getFamily(old));
                    } else {
                        c.getCharacter().setFamily(inviter.getFamilyId(), inviter.getID(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2);
                        fam.setOnline(c.getCharacter().getID(), true, c.getChannel());
                        c.getCharacter().saveFamilyStatus();
                    }
                    if (fam != null) {
                        if (inviter.getNoJuniors() == 1 || old > 0) {//just got their first junior whoopee
                            fam.resetDescendants();
                        }
                        fam.resetPedigree(); //is this necessary?
                    }
                } else {
                    int id = MapleFamily.createFamily(inviter.getID());
                    if (id > 0) {
                        //before loading the family, set sql
                        MapleFamily.setOfflineFamilyStatus(id, 0, c.getCharacter().getID(), 0, inviter.getCurrentRep(), inviter.getTotalRep(), inviter.getID());
                        MapleFamily.setOfflineFamilyStatus(id, inviter.getID(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2, c.getCharacter().getCurrentRep(), c.getCharacter().getTotalRep(), c.getCharacter().getID());
                        inviter.setFamily(id, 0, c.getCharacter().getID(), 0); //load the family
                        inviter.finishAchievement(36);
                        c.getCharacter().setFamily(id, inviter.getID(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2);
                        MapleFamily fam = World.Family.getFamily(id);
                        fam.setOnline(inviter.getID(), true, inviter.getClient().getChannel());
                        if (old > 0 && World.Family.getFamily(old) != null) { //has junior
                            MapleFamily.mergeFamily(fam, World.Family.getFamily(old));
                        } else {
                            fam.setOnline(c.getCharacter().getID(), true, c.getChannel());
                        }
                        fam.resetDescendants();
                        fam.resetPedigree();

                    }
                }
                c.getSession().write(FamilyPacket.getFamilyInfo(c.getCharacter()));
            }
        }
    }
}
