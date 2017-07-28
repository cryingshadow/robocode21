package de.metro.robocode;
import robocode.*;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static java.lang.Math.abs;
import static robocode.util.Utils.normalRelativeAngleDegrees;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * OdinsHammerV2 - a robot by (your name here)
 */
public class Odin extends RateControlRobot
{
	private Map<String, ScannedRobotEvent> targetMap = new HashMap <String, ScannedRobotEvent>();
	private Map<String, Integer> lastScannedTargets = new HashMap <String, Integer>();
	private ScannedRobotEvent lastEvent;
	private boolean doFire = false; 
	
	private int count=0;
	private int direction = 1; // 1 - up, 2 - right, 3 - down, 4 - left
	private double maxX = -1;
	private double maxY = -1;
	private boolean moving = true;
	
	/**
	 * run: OdinsHammerV2's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here
		initialize();
		maxX = getBattleFieldWidth();
		maxY = getBattleFieldHeight();
		
		setTurnGunLeft(0);
		setVelocityRate(7);

		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			count++;
			
//			if (count % 7 == 0) {
//				if (getVelocity() == 7) {
//					setVelocityRate(2);
//				} else {
//					setVelocityRate(7);
//				}
//			}
			
			moveRadarAndGetTargets();
			moveGunAndFire();
			moveRobot();
			execute();
		}
	}
	
	@Override
	public void onHitRobot(HitRobotEvent event) {
		direction += 1;
		if (direction > 4) {
			direction = 1;
		}
		setTurnRate(90);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		lastEvent = e;
		// Replace the next line with any behavior you would like
		if (!targetMap.containsKey(e.getName())){
//			List<ScannedRobotEvent> targetList = new ArrayList<ScannedRobotEvent>();
			targetMap.put(e.getName(), e);
		}
//		targetMap.get(e.getName()).add(e);
		out.println("Target: " + e.getName() + "; Bearing: " + e.getBearing() + "; Distance: " + e.getDistance() + "; Heading: " + e.getHeading() + "; Energy: " + e.getEnergy());

		if (lastScannedTargets == null) {
			lastScannedTargets = new HashMap <String, Integer>();			
		}
		lastScannedTargets.put(e.getName(), count);
	}
	
	/**
	 * onHitByBullet: What to do when you're hit by a bullet

	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		out.println("WE ARE HIT !\nMOVE BACK WITH 10 pixels.");
		setBack(10);
	}
	 */	
	/**
	 * onHitWall: What to do when you hit a wall

	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(20);
	}	
	 */
	
    private void initialize(){
		// Set colors
		setBodyColor(Color.green);
		setGunColor(Color.black);
		setRadarColor(Color.white);
		setScanColor(Color.yellow);
		setBulletColor(Color.blue);
		
		//keep radar still on turn
		setAdjustRadarForGunTurn(true);
//		setVelocityRate(10);
	}
	
	private void moveRadarAndGetTargets() {
		final int defaultRadarRotation = 45;
		setRadarRotationRate(defaultRadarRotation);
		if (getRadarHeading() + defaultRadarRotation > 360){
			// remove not visible targets after one turnaround
			Iterator it = targetMap.entrySet().iterator();
			while (it.hasNext()){
				Map.Entry pair = (Map.Entry)it.next();
				if (lastScannedTargets == null || !lastScannedTargets.containsKey(pair.getKey())){
					it.remove();
				}
			}
		}
		lastScannedTargets = null;
	}
	
	public void onBulletMissed (BulletMissedEvent e){
		out.println("Bullet Missed " );
		doFire = false;
	}	
	
	private void moveRobot() {
		ScannedRobotEvent sre = getClosestTarget();
		if (sre != null && sre.getDistance() <= 100) {
			moving = false;
			setVelocityRate(0);
			setTurnRate(0);
		} else if (sre == null || sre.getDistance() >= 300) {
			moving = true;
			setVelocityRate(5);
		}
		if (!moving) {
			return;
		}
		if (direction == 1) {
			if (getHeading() != 0) {
				setTurnRate(getHeading() < 360 - getHeading() ? -getHeading() : 360 - getHeading());
			} else {
				setTurnRate(0);
			}
			if (getY() >= maxY - 160) {
				direction = 2;
				setTurnRate(90);
			}
		}
		if (direction == 2) {
			if (getHeading() != 90) {
				setTurnRate(-1 * (getHeading() - 90));
			} else {
				setTurnRate(0);
			}
			if (getX() >= maxX - 160) { 
				direction = 3;
				setTurnRate(90);
			}
				
		}
		if (direction == 3) {
			if (getHeading() != 180) {
				setTurnRate(-1 * (getHeading() - 180));
			} else {
				setTurnRate(0);
			}
			if (getY() <= 160) {
				direction = 4;
				setTurnRate(90);
			}
		}
		if (direction == 4) {
			if (getHeading() != 270) {
				setTurnRate(-1 * (getHeading() - 270));
			} else {
				setTurnRate(0);
			}
			if (getX() <= 160) {
				direction = 1;
				setTurnRate(90);
			}
		}		
	}
	
	private void moveGunAndFire() {
		ScannedRobotEvent closestTarget = getClosestTarget();
		if (closestTarget != null){
			double gunRotation = normalRelativeAngleDegrees(closestTarget.getBearing() + (getHeading() - getGunHeading()));
			
			setGunRotationRate (gunRotation);
			if (gunRotation < 3){
				setFireBullet(5);
			}
			out.println("getHeading: " + getHeading() + "getGunHeading: " + getGunHeading() + "gunRotation: " + gunRotation);
		}
	}
	
	private ScannedRobotEvent getClosestTarget() {
		double minVal = 10000;
		ScannedRobotEvent retVal = null;
		
		for (Map.Entry<String, ScannedRobotEvent> entry : targetMap.entrySet()) {
		    String key = entry.getKey();
		    ScannedRobotEvent value = entry.getValue();
		    
		    if (value.getDistance() < minVal){
		    	minVal = value.getDistance();
		    	retVal = value;
		    }
		}
		
		return retVal;
	}
}
