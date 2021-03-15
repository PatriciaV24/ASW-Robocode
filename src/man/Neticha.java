package man;
import java.awt.Color;
import java.awt.geom.Point2D;
import robocode.*;
import robocode.util.Utils;
import java.util.ArrayList;
import java.util.Random;

/**
 * <b> Neticha </b> is a destruction machine that consists in the following:
 *  
 * 	<p>-It moves when it detects a enemy bullet coming. It can move in a zigzag, or in a circle around the
 * 	enemy.</p>
 * <p> -The gun is based on the wavebullet algoritm. It creates a list that adjusts the possible angles of 
 * 	the gun, and shoots with the best angle in the moment.</p>
 *
 * @author Manuel Sá up201805273
 * @author Patrícia Vieira up201805238
 */
public class Neticha extends AdvancedRobot{
	double enemyEnergy = 100;
	int moveDirection = 1;
	int flagShoot=0;
	int firePow=1;
	double fireSpeed=0; 
	
	//list where are the gunwaves shot at the moment
	ArrayList<GunWave> gunWaves=new ArrayList<GunWave>();
	static double [] gunAngles=new double[16];

	public void run() {
		setColors(Color.white,Color.green,Color.white,Color.white,Color.green);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while(true){
			if(getRadarTurnRemaining()==0.0){
				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
			execute();	
		}
	}

	public void onScannedRobot(ScannedRobotEvent enemy) {		
		double changeInEnergy = enemyEnergy-enemy.getEnergy();
		double posEnemey= getHeadingRadians() +enemy.getBearingRadians();
		double radar=Utils.normalRelativeAngle(posEnemey-getRadarHeadingRadians());
		double extraT=Math.min(Math.atan(36.0/enemy.getDistance()),Rules.RADAR_TURN_RATE_RADIANS);
		if(radar < 0) radar+=-extraT;
		else radar+=extraT;
		setTurnRadarRightRadians(radar);

		checkLimits();

		//not have a repetitive movement 
		Random randonValue = new Random();
		int changeMovRandom= randonValue.nextInt(2);
		if(changeMovRandom==0) moveDirection=-moveDirection;
			

		if (changeInEnergy>0 &&changeInEnergy<=3) {
			setColors(Color.red,Color.black,Color.red,Color.red,Color.black);
			flagShoot=1;
			if(enemy.getDistance()<200){
				setAhead(-(enemy.getDistance()/4+200)*moveDirection);
				setTurnRightRadians(enemy.getBearingRadians()+Math.PI/2-Math.PI/6*moveDirection);
			}else{
				setAhead((enemy.getDistance()/4+25)*moveDirection);
			}

			moveDirection =-moveDirection;	
			
			if(enemy.getDistance()>450){
                setAhead((enemy.getDistance()/4+25)*moveDirection);
				setTurnRightRadians(enemy.getBearingRadians()+Math.PI/2-Math.PI/6*moveDirection);
            }
		}else{
			if(enemy.getDistance()<300 && flagShoot==0){
				setAhead(-(enemy.getDistance()/4+200)*moveDirection);
				setTurnRightRadians(enemy.getBearingRadians()+Math.PI/2-Math.PI/6*moveDirection);
			}
		}

		if(enemy.getDistance()<150) firePow=3;
		else{
			if(enemy.getDistance()<300) firePow=2;
			else firePow=1;
		}
		
		if(getGunHeat()==0){
            fireSpeed=20-3*firePow;
			addLogFiringWave(enemy);
        }
		
		checkFiringWaves(project(new Point2D.Double(getX(),getY()),enemy.getDistance(),posEnemey));
		setTurnGunRightRadians(Utils.normalRelativeAngle(posEnemey-getGunHeadingRadians())+gunAngles[8+(int)(enemy.getVelocity()*Math.sin(enemy.getHeadingRadians()-posEnemey))]);
		setFire(firePow);
		setTurnRadarRightRadians(radar*2);

		checkLimits();

		enemyEnergy = enemy.getEnergy();
	}

	public void onHitWall(HitWallEvent enemy){
		moveDirection=-moveDirection;
	}

	public void onHitByBullet(HitByBulletEvent enemy){
		setAhead(-300*moveDirection);
		setTurnRightRadians(enemy.getBearingRadians()+Math.PI/2-Math.PI/6*moveDirection);
	}

	public void onWin(WinEvent enemy){
		setColors(Color.white,Color.green,Color.white,Color.white,Color.green);
	}

	/**
	 * attempt so that the robot does not go to the wall, 18pixeis*2  
	 */
	public void checkLimits() {
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

	class GunWave{ 
		Point2D.Double origin;
		double speed;
		double startTime;
		double absBearing;
		int velSeg;
	}	

	/**
	 * verifies if the shot of the gunwave missed...if so, thw gunwave is removed from the list and the angle is adjusted
	*/
	public void checkFiringWaves(Point2D.Double posEnemy){
        GunWave w;
        for(int i=0;i<gunWaves.size();i++){
            w=gunWaves.get(i);
            if((getTime()-w.startTime)*w.speed>=w.origin.distance(posEnemy)){
                gunAngles[w.velSeg+8]=Utils.normalRelativeAngle(Utils.normalAbsoluteAngle(Math.atan2(posEnemy.x-w.origin.x, posEnemy.y-w.origin.y))-w.absBearing);
                gunWaves.remove(w);
            }
        }
    }

	public void addLogFiringWave(ScannedRobotEvent enemy){
        GunWave w=new GunWave();
		w.origin=new Point2D.Double(getX(),getY());
		w.speed=fireSpeed;
		w.startTime=getTime();
        w.absBearing=enemy.getBearingRadians()+getHeadingRadians();
        w.velSeg=(int)(enemy.getVelocity()*Math.sin(enemy.getHeadingRadians()-w.absBearing));
        gunWaves.add(w);
    }
	
	/**
	 * allows us to know a coordenate from an angle and another coordenate 
	*/
	public Point2D.Double project(Point2D.Double origin,double distance,double angle){
	    return new Point2D.Double(origin.x+distance*Math.sin(angle),origin.y+distance*Math.cos(angle));
	}

}	