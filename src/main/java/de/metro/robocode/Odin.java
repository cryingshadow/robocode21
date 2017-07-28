package de.metro.robocode;
import robocode.*;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static java.lang.Math.abs;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * OdinsHammerV2 - a robot by (your name here)
 */
public class Odin extends RateControlRobot
{
	private Map<String, List<ScannedRobotEvent>> targetMap = new HashMap <String, List<ScannedRobotEvent>>();
	private Map<String, Integer> lastScannedTargets = new HashMap <String, Integer>();
	private ScannedRobotEvent lastEvent; 
	
	private int count=0;
	/**
	 * run: OdinsHammerV2's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here
		initialize();

		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			count++;
			moveRadarAndGetTargets();
			moveGunAndFire();
			execute();
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		lastEvent = e;
		// Replace the next line with any behavior you would like
		if (!targetMap.containsKey(e.getName())){
			List<ScannedRobotEvent> targetList = new ArrayList<ScannedRobotEvent>();
			targetMap.put(e.getName(), targetList);
		}
		targetMap.get(e.getName()).add(e);
		out.println("Target: " + e.getName() + "; Bearing: " + e.getBearing() + "; Distance: " + e.getDistance() + "; Heading: " + e.getHeading() + "; Energy: " + e.getEnergy());
		
		if (lastScannedTargets == null) {
			lastScannedTargets = new HashMap <String, Integer>();			
		}
		lastScannedTargets.put(e.getName(), count);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(20);
	}	
	
    private void initialize(){
		// Set colors
		setBodyColor(Color.green);
		setGunColor(Color.black);
		setRadarColor(Color.white);
		setScanColor(Color.yellow);
		setBulletColor(Color.blue);
		
		//keep radar still on turn
		setAdjustRadarForGunTurn(true);
	}
	
	private void moveRadarAndGetTargets(){
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
	
	private void moveGunAndFire(){
		out.println("getGunHeading: " + getGunHeading()); 		
	}
}
