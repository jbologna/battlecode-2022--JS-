package player14;

import battlecode.common.*;

strictfp class SoldierStrategy {

    static RobotType myType = RobotType.SOLDIER;
    final static int ATTACK_RADIUS_SQUARED = myType.actionRadiusSquared;
    final static int ATTACK_RADIUS_SQUARED_WITHIN_ONE_MOVE = 20;
    static int longestTime = 0;
    static MapLocation repairLocation;
    static int aliveTime = 0;
    static boolean healing = false;

    static BFS bfs;

    static void run(RobotController rc) throws GameActionException { 
        MapLocation me = rc.getLocation();

        // if (aliveTime == 0) {
        //     bfs = new AdvancedMove(rc);
        // }

        aliveTime++;
        if (aliveTime == 2) {
            repairLocation = me;
        }
        

        MapLocation target = RobotPlayer.locateCombatTarget(rc, me);
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, RobotPlayer.opponent);

        RobotPlayer.attackGlobalTargetIfAble(rc, target, me);



        TripleTarget localTargets = RobotPlayer.acquireLocalTargets(rc, target, enemies, me);

        MapLocation primaryTarget = localTargets.primary;
        MapLocation secondaryTarget = localTargets.secondary;
        MapLocation tertiaryTarget = localTargets.tertiary;

        // if (rc.getRoundNum() < 45) {
        //     tertiaryTarget = rc.adjacentLocation(RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)]);
        // }


        if (rc.senseNearbyRobots(2, rc.getTeam()).length > 4) {
            RobotPlayer.move2(rc, primaryTarget, 3);
        }
        if (rc.canAttack(primaryTarget)) {
            rc.attack(primaryTarget);
            Comms.setEnemyLocation(rc, primaryTarget);
        }
        if (rc.canAttack(secondaryTarget)) {
            rc.attack(secondaryTarget);
        }
        if (rc.canAttack(tertiaryTarget)) {
            rc.attack(tertiaryTarget);
        }

        // if (rc.senseNearbyRobots(-1, rc.getTeam()).length < 5 /* && rc.canSenseLocation(primaryTarget) && rc.canSenseRobotAtLocation(primaryTarget) && rc.senseRobotAtLocation(primaryTarget).type.equals(RobotType.SOLDIER) */ ) {
        //     RobotPlayer.move2(rc, rc.adjacentLocation(me.directionTo(primaryTarget).opposite()).add(me.directionTo(primaryTarget)), 3);
        // }

        if (rc.isActionReady() && rc.isMovementReady()) {

            if (healing || rc.getHealth() < (rc.getType().health / 4)) {

                ArchonLocation[] archLocs = Comms.getArchonLocations(rc);

                MapLocation repairLoc = null;

                boolean foundRepairSpot = false;

                while (!foundRepairSpot) {
                    for (ArchonLocation archLoc : archLocs) {
                        // if (repairLoc == null || (archLoc.exists && me.distanceSquaredTo(repairLoc) > me.distanceSquaredTo(archLoc.location))) {
                        //     repairLoc = archLoc.location;
                        // }
                        if (archLoc.exists && RobotPlayer.rng.nextInt(4) == 0) {
                            repairLoc = archLoc.location;
                            foundRepairSpot = true;
                        }
                    }
                }

                tertiaryTarget = repairLoc;
                healing = true;
            }

            if (rc.getHealth() > rc.getType().health - 3) {
                healing = false;
            }

            // Experimental move.
            int recursionLimit = 4;
            int startTime = Clock.getBytecodeNum();
            if (Clock.getBytecodesLeft() <= longestTime + 1000) {
                recursionLimit = 3;
            }



            // double normalDistance = Math.sqrt(rc.getType().visionRadiusSquared);
            // double angle = Math.tan((tertiaryTarget.x - me.x) / (tertiaryTarget.y - me.y));
            // int xDistance = Math.round((float)(Math.cos(angle) * normalDistance));
            // int yDistance = Math.round((float)(Math.sin(angle) * normalDistance));

            // MapLocation newTarget = new MapLocation(me.x + xDistance, me.y + yDistance);


            
            try {
                Direction dir = AdvancedMove.getBestDir(rc, tertiaryTarget);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            } catch (Exception e) {
                //TODO: handle exception
                System.out.println("Move returned null");;
            }
            // RobotPlayer.move2(rc, tertiaryTarget, recursionLimit);


            int end = Clock.getBytecodeNum();

            if ((end - startTime) > longestTime) {
                longestTime = (end - startTime);
            }
            rc.setIndicatorString("" + longestTime);
            rc.setIndicatorLine(me, tertiaryTarget, 1000, 0, 1000);;

            // Fall back to simple move incase other move doesn't work.
            RobotPlayer.move(rc, tertiaryTarget);


            // RobotPlayer.move(rc, tertiaryTarget);

            if (rc.canAttack(tertiaryTarget)) {
                rc.attack(tertiaryTarget);
            }
        } else {
            RobotPlayer.stepOffRubble(rc, me);
        }
        if (!rc.isActionReady() && rc.isMovementReady()) {
            MapLocation retreatMove = rc.adjacentLocation(me.directionTo(primaryTarget).opposite());
            retreatMove = rc.adjacentLocation(me.directionTo(primaryTarget).opposite());
            retreatMove = rc.adjacentLocation(me.directionTo(primaryTarget).opposite());
            retreatMove = rc.adjacentLocation(me.directionTo(primaryTarget).opposite());
            retreatMove = rc.adjacentLocation(me.directionTo(primaryTarget).opposite());
            RobotPlayer.move(rc, retreatMove);
        }
        
        // rc.setIndicatorLine(me, target, 0, 1, 0);
    }
}