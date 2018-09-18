/**
 * Map ID: 4000000
 * @author RichardL
 */

function start(em) {
    em.enableDirectionStatus();
    em.introEnableUI(true);
    em.sleep(2100);

    em.forceMoveCharacter(0);
    em.delayEvent(30);

    em.showDirectionEffect("Effect/Direction3.img/effect/tuto/BalloonMsg0/0", 2100, 0, -120);
    em.delayEvent(2100);

    em.forceMoveCharacter(2);
    em.delayEvent(420);

    em.forceMoveCharacter(1);
    em.delayEvent(420);

    em.forceMoveCharacter(2);
    em.delayEvent(420);

    em.forceMoveCharacter(0);
    em.showDirectionEffect("Effect/Direction12.img/effect/tuto/BalloonMsg0/1", 2100, 0, -120);
    em.delayEvent(1800);

    em.showDirectionEffect("Effect/Direction3.img/effect/tuto/BalloonMsg0/1", 2100, 0, -120);
    em.delayEvent(2100);

    em.showDirectionEffect("Effect/Direction3.img/effect/tuto/key/0", 3000000, -300, 0);
    em.showDirectionEffect("Effect/Direction3.img/effect/tuto/key/0", 3000000, 0, 0);
    em.showDirectionEffect("Effect/Direction3.img/effect/tuto/key/0", 3000000, 300, 0);
    em.showDirectionEffect("Effect/Direction3.img/effect/tuto/key/1", 3000000, 540, 70);
    em.delayEvent(1200);

    em.showDirectionEffect("Effect/Direction3.img/effect/tuto/BalloonMsg0/2", 2100, 0, -120);
    em.delayEvent(2100);

    em.topMsg("Press the left and right keys to move.");
    em.delayEvent(3000);

    em.topMsg("Go to where the portal is and press the up key to move to the next map.");

    em.introEnableUI(false);
}