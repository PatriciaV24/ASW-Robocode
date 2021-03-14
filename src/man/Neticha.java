package man;
import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.*;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.Random;


/**
 * -Neticha is a destruction machine that consists in the following:
 *  
 * 	-It moves when it detects a enemy bullet coming. It can move in a zigzag, or in a circle around the
 * 	enemy.
 *  -The gun is based on the wavebullet algoritm. It creates a list that adjusts the possible angles of 
 * 	the gun, and shoots with the best angle in the moment.
 *
 * @author Manuel Sá
 * @author Patrícia Vieira
 */
public class Neticha extends AdvancedRobot{
	double EnemyEnergy = 100;
	int moveDirection = 1;
	int flagshoot=0;
	int firePow=1;
	double fireSpeed=0; 
	

	//list where are the gunwaves shot at the moment
	ArrayList<GunWave> gunWaves=new ArrayList<GunWave>();
	
	//array that has the possible angles of the gun
	static double [] gunAngles=new double[16];

	public void run() {
	
		
		setColors(Color.white, Color.white, Color.white);
		
		//makes the body,radar and gun move separetly
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while(true){
			if(getRadarTurnRemaining()==0.0){
				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
			execute();	
		}
	}


	public void onScannedRobot(ScannedRobotEvent e) {
		
		
		double changeInEnergy = EnemyEnergy-e.getEnergy();
		
		//Robot Radar
		double posInimigo= getHeadingRadians() +e.getBearingRadians();
		double radar=Utils.normalRelativeAngle(posInimigo-getRadarHeadingRadians());
		double extraT=Math.min(Math.atan(36.0/e.getDistance()),Rules.RADAR_TURN_RATE_RADIANS);
		radar+= (radar < 0 ? -extraT : extraT);
		setTurnRadarRightRadians(radar);

		verificarlimites();

		//Robot movement
		Random ran = new Random();
		int r= ran.nextInt(2);
		
		if(r==0) moveDirection=-moveDirection;
			

		if (changeInEnergy>0 &&changeInEnergy<=3) {
			setColors(Color.black, Color.black, Color.black);
			flagshoot=1;
			if(e.getDistance()<200){
				setAhead(-(e.getDistance()/4+200)*moveDirection);
				setTurnRightRadians(e.getBearingRadians()+Math.PI/2-Math.PI/6*moveDirection);
			}else{
				setAhead((e.getDistance()/4+25)*moveDirection);
			}

			moveDirection =-moveDirection;	
			
			if(e.getDistance()>450){
                setAhead((e.getDistance()/4+25)*moveDirection);
				setTurnRightRadians(e.getBearingRadians()+Math.PI/2-Math.PI/6*moveDirection);
            }
		}else{
			if(e.getDistance()<300 && flagshoot==0){
				setAhead(-(e.getDistance()/4+200)*moveDirection);
				setTurnRightRadians(e.getBearingRadians()+Math.PI/2-Math.PI/6*moveDirection);
			}
		}

	
		//robot gun
		if(e.getDistance()<150) 
			firePow=3;
		else{
			if(e.getDistance()<300) 
				firePow=2;
			else 
				firePow=1;
			}
		
		if(getGunHeat()==0){
            fireSpeed=20-3*firePow;
			logFiringWave(e);
        }
		
		checkFiringWaves(project(new Point2D.Double(getX(),getY()),e.getDistance(),posInimigo));
		
		setTurnGunRightRadians(Utils.normalRelativeAngle(posInimigo-getGunHeadingRadians())+gunAngles[8+(int)(e.getVelocity()*Math.sin(e.getHeadingRadians()-posInimigo))]);
	        
		setFire(firePow);
		setTurnRadarRightRadians(radar*2);

		verificarlimites();

		EnemyEnergy = e.getEnergy();
	}

	public void onHitWall(HitWallEvent e){
		moveDirection=-moveDirection;
	}

	public void onHitByBullet(HitByBulletEvent e){
		setAhead(-300*moveDirection);
		setTurnRightRadians(e.getBearingRadians()+Math.PI/2-Math.PI/6*moveDirection);
	}

	public void onWin(WinEvent e){
		setColors(Color.white, Color.white, Color.white);

	}

	public void verificarlimites() {
		if(getX()<=36){
			if(getHeadingRadians()>= Math.PI){
				setAhead(-200);
				setTurnRightRadians(Math.PI/4*moveDirection);

			}else{
				setAhead(200);
				setTurnRightRadians(Math.PI/4*moveDirection);
			}
		}
		if(getY()<=36){
			if(getHeadingRadians()>= Math.PI/2 && getHeadingRadians()<= (3*Math.PI)/2){
				setAhead(-200);
				setTurnRightRadians(Math.PI/4*moveDirection);
			}else{
				setAhead(200);
				setTurnRightRadians(Math.PI/4*moveDirection);
			}
		}
		if(getX()>=getBattleFieldWidth()-36){
			if(getHeadingRadians()>= 0 && getHeadingRadians()<=Math.PI){
				setAhead(-200);
				setTurnRightRadians(Math.PI/4*moveDirection);
			}else{
				setAhead(200);
				setTurnRightRadians(Math.PI/4*moveDirection);
			}
		}
		if(getY()>=getBattleFieldHeight()-36){
			if(getHeadingRadians()<= Math.PI/2 || getHeadingRadians()>=(3*Math.PI)/2){
				setAhead(-200);
				setTurnRightRadians(Math.PI/4*moveDirection);
			}else{
				setAhead(200);
				setTurnRightRadians(Math.PI/4*moveDirection);
			}
		}

	}

	 //the class that has all the information of a shot
	class GunWave{ 
		double speed;
		Point2D.Double origin;
		int velSeg;
		double absBearing;
		double startTime;
	}	

	//verifies if the shot of the gunwave missed...if so, thw gunwave is removed from the list and the angle is adjusted
	public void checkFiringWaves(Point2D.Double ePos){
        GunWave w;
        for(int i=0;i<gunWaves.size();i++){
            w=gunWaves.get(i);
            if((getTime()-w.startTime)*w.speed>=w.origin.distance(ePos)){
                gunAngles[w.velSeg+8]=Utils.normalRelativeAngle(Utils.normalAbsoluteAngle(Math.atan2(ePos.x-w.origin.x, ePos.y-w.origin.y))-w.absBearing);
                gunWaves.remove(w);
            }
        }
    }

	//adds a gunwave to the list
	public void logFiringWave(ScannedRobotEvent e){
        GunWave w=new GunWave();
        w.absBearing=e.getBearingRadians()+getHeadingRadians();
        w.speed=fireSpeed;
        w.origin=new Point2D.Double(getX(),getY());
        w.velSeg=(int)(e.getVelocity()*Math.sin(e.getHeadingRadians()-w.absBearing));
        w.startTime=getTime();
        gunWaves.add(w);
    }
	
	//this method allows us to know a coordenate from an angle and another coordenate 
	public Point2D.Double project(Point2D.Double origin,double dist,double angle){
	    return new Point2D.Double(origin.x+dist*Math.sin(angle),origin.y+dist*Math.cos(angle));
	}

}	

	

