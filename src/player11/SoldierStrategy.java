package player11;

import battlecode.common.*;

strictfp class SoldierStrategy {

    static MapLocation backupTarget = RobotPlayer.getRandomMapLocation();
    static RobotType myType = RobotType.SOLDIER;
    final static int ATTACK_RADIUS_SQUARED = myType.actionRadiusSquared;
    final static int ATTACK_RADIUS_SQUARED_WITHIN_ONE_MOVE = 20;

    static void run(RobotController rc) throws GameActionException {
        /* Pseudocode:
        
        Start with global defensive target. If this is 0, use global offensive target. If this is 0,
        use our backup random target.

        Then, if there are enemies around us, check to see if any are in the shared target list.
        If one is, attack it. If not, choose the enemy soldier with the lowest health, attack it,
        and add it to the shared target list.

        If there are not enemies around us, determine the closest enemy in the shared target list
        and move toward it.

        If there are not enemies in the shared target list, we will simply move toward the global target.

        We get shared target list;

        If we are in range of a target list target, we should attack it.
        
        If not but we are in range of an unlisted enemy target, we should attack it and
        add it to the target list.

        If we are not in range of any enemy tarets, we should move toward a target in target
        list, including global target
        */
            
        
        MapLocation me = rc.getLocation();
        boolean moving = true;
        

        // Check for defensive, offensive, and random global target.
        MapLocation target = RobotPlayer.getDefensiveTarget(rc);
        if (RobotPlayer.targetDoesntExist(target)) {
            target = RobotPlayer.getOffensiveTarget(rc);
        }
        if (RobotPlayer.targetDoesntExist(target)) {
            target = backupTarget;
        }
        
        // Global targets and local targets
        MapLocation[] targetList = RobotPlayer.getTargetList(rc);
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, RobotPlayer.opponent);

        if (targetList.length > 0) {
            for (MapLocation targetLoc : targetList) {
                if (me.distanceSquaredTo(targetLoc) < me.distanceSquaredTo(target)) {
                    target = targetLoc;
                }
            }
        }


        // * Reconciling who to target: This basically just updates our target variable.
        //
        // First, we prioritize units in the global target list over those not in it.
        // Next, we prioritize soldiers/sages/watchtowers over other units.
        // Finally, we prioritize the lowest health enemy.
        //
        // We fuse loops to increase efficiency at the cost of readability.
        boolean foundTargetInTargetList = false;
        boolean foundWarriorType = false;
        boolean foundTargetNotInTargetList = false;
        int lowestHP = 100000;
        if (enemies.length > 0) {

            for (int i = 0; i < enemies.length; i++) {
                RobotInfo enemy = enemies[i];

                // If enemy in range is in target list, we will target that
                if (targetList.length > 0) {
                    for (MapLocation targetLoc : targetList) {
                        if (targetLoc.equals(enemy.location)) {
                            int distanceToEnemy = me.distanceSquaredTo(targetLoc);
                            if (distanceToEnemy <= ATTACK_RADIUS_SQUARED) {
                                target = targetLoc;
                                moving = false;
                                foundTargetInTargetList = true;
                                break;
                            }
                        }
                    }
                }
                
                int distanceToEnemy = me.distanceSquaredTo(enemy.location);
                if (!foundTargetInTargetList) {
                    // Otherwise, we will choose the lowest HP soldier

                    if (enemy.type.equals(RobotType.SOLDIER) || enemy.type.equals(RobotType.SAGE) || enemy.type.equals(RobotType.WATCHTOWER)) {
                        if (foundWarriorType) {
                            if (enemy.health < lowestHP && distanceToEnemy <= ATTACK_RADIUS_SQUARED) {
                                lowestHP = enemy.health;
                                target = enemy.location;
                                moving = false;
                                foundTargetNotInTargetList = true;
                            }
                        } else if (distanceToEnemy <= ATTACK_RADIUS_SQUARED) {
                            foundWarriorType = true;
                            target = enemy.location;
                            moving = false;
                            lowestHP = enemy.health;
                            foundTargetNotInTargetList = true;
                        }
                    }
                    else if (!foundWarriorType && distanceToEnemy <= ATTACK_RADIUS_SQUARED) {
                        if (enemy.health < lowestHP) {
                            foundTargetNotInTargetList = true;
                            lowestHP = enemy.health;
                            target = enemy.location;
                            moving = false;
                        }
                    }
                }
            }

            if (foundTargetNotInTargetList && !foundTargetInTargetList) {
                int idx = Math.min(targetList.length + 2, 63);
                // RobotPlayer.addLocationToSharedArray(rc, target, 0, 2);
            }
        }

        
        // Moving and attacking
        if (rc.canAttack(target)) {
            rc.attack(target);
        }
        
        if (moving) {
            RobotPlayer.move(rc, target);
        }
        
        rc.setIndicatorLine(me, target, 0, 1, 0);
    }
}