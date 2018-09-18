
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
package server.maps;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import client.MapleClient;
import client.character.MapleCharacter;
import net.packet.CField;
import net.packet.CWvsContext;
import net.packet.CWvsContext.PartyPacket;

import java.lang.ref.WeakReference;
import server.MaplePortal;

public class MapleDoor extends MapleMapObject {

    private final WeakReference<MapleCharacter> owner;
    private final MapleMap town;
    private final MaplePortal townPortal;
    private final MapleMap target;
    private final int skillId, ownerId;
    private final Point targetPosition;

    public MapleDoor(final MapleCharacter owner, final Point targetPosition, final int skillId) {
        super();
        this.owner = new WeakReference<>(owner);
        this.ownerId = owner.getID();
        this.target = owner.getMap();
        this.targetPosition = targetPosition;
        setPosition(this.targetPosition);
        this.town = this.target.getReturnMap();
        this.townPortal = getFreePortal();
        this.skillId = skillId;
    }

    public MapleDoor(final MapleDoor origDoor) {
        super();
        this.owner = new WeakReference<>(origDoor.owner.get());
        this.town = origDoor.town;
        this.townPortal = origDoor.townPortal;
        this.target = origDoor.target;
        this.targetPosition = new Point(origDoor.targetPosition);
        this.skillId = origDoor.skillId;
        this.ownerId = origDoor.ownerId;
        setPosition(townPortal.getPosition());
    }

    public final int getSkill() {
        return skillId;
    }

    public final int getOwnerId() {
        return ownerId;
    }

    private MaplePortal getFreePortal() {
        final List<MaplePortal> freePortals = new ArrayList<>();

        for (final MaplePortal port : town.getPortals()) {
            if (port.getType() == 6) {
                freePortals.add(port);
            }
        }
        Collections.sort(freePortals, new Comparator<MaplePortal>() {

            @Override
            public final int compare(final MaplePortal o1, final MaplePortal o2) {
                if (o1.getId() < o2.getId()) {
                    return -1;
                } else if (o1.getId() == o2.getId()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (final MapleMapObject obj : town.getAllDoorsThreadsafe()) {
            final MapleDoor door = (MapleDoor) obj;
            /// hmm
            if (door.getOwner() != null && door.getOwner().getParty() != null && getOwner() != null && getOwner().getParty() != null && getOwner().getParty().getId() == door.getOwner().getParty().getId()) {
                return null; //one per
            }
            freePortals.remove(door.getTownPortal());
        }
        if (freePortals.size() <= 0) {
            return null;
        }
        return freePortals.iterator().next();
    }

    @Override
    public final void sendSpawnData(final MapleClient client) {
        if (getOwner() == null || target == null || client.getCharacter() == null) {
            return;
        }
        if (target.getId() == client.getCharacter().getMapId() || getOwnerId() == client.getCharacter().getID() || (getOwner() != null && getOwner().getParty() != null && client.getCharacter().getParty() != null && getOwner().getParty().getId() == client.getCharacter().getParty().getId())) {
            client.getSession().write(CField.spawnMysticDoor(getOwnerId(), skillId, target.getId() == client.getCharacter().getMapId() ? targetPosition : townPortal.getPosition(), true)); //spawnDoor always has same position.
            if (getOwner() != null && getOwner().getParty() != null && client.getCharacter().getParty() != null && (getOwnerId() == client.getCharacter().getID() || getOwner().getParty().getId() == client.getCharacter().getParty().getId())) {
                client.getSession().write(PartyPacket.partyPortal(town.getId(), target.getId(), skillId, target.getId() == client.getCharacter().getMapId() ? targetPosition : townPortal.getPosition(), true));
            }
            client.getSession().write(CWvsContext.spawnPortal(town.getId(), target.getId(), skillId, target.getId() == client.getCharacter().getMapId() ? targetPosition : townPortal.getPosition()));
        }
    }

    @Override
    public final void sendDestroyData(final MapleClient client) {
        if (client.getCharacter() == null || getOwner() == null || target == null) {
            return;
        }
        if (target.getId() == client.getCharacter().getMapId() || getOwnerId() == client.getCharacter().getID() || (getOwner() != null && getOwner().getParty() != null && client.getCharacter().getParty() != null && getOwner().getParty().getId() == client.getCharacter().getParty().getId())) {
            client.getSession().write(CField.removeMysticDoor(getOwnerId(), false));
            if (getOwner() != null && getOwner().getParty() != null && client.getCharacter().getParty() != null && (getOwnerId() == client.getCharacter().getID() || getOwner().getParty().getId() == client.getCharacter().getParty().getId())) {
                client.getSession().write(PartyPacket.partyPortal(999999999, 999999999, 0, new Point(-1, -1), false));
            }
            client.getSession().write(CWvsContext.spawnPortal(999999999, 999999999, 0, null));
        }
    }

    public final void warp(final MapleCharacter chr, final boolean toTown) {
        if (chr.getID() == getOwnerId() || (getOwner() != null && getOwner().getParty() != null && chr.getParty() != null && getOwner().getParty().getId() == chr.getParty().getId())) {
            if (!toTown) {
                chr.changeMap(target, target.findClosestPortal(targetPosition));
            } else {
                chr.changeMap(town, townPortal);
            }
        } else {
            chr.getClient().getSession().write(CWvsContext.enableActions());
        }
    }

    public final MapleCharacter getOwner() {
        return owner.get();
    }

    public final MapleMap getTown() {
        return town;
    }

    public final MaplePortal getTownPortal() {
        return townPortal;
    }

    public final MapleMap getTarget() {
        return target;
    }

    public final Point getTargetPosition() {
        return targetPosition;
    }

    @Override
    public final MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}
