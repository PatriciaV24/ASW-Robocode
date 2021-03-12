package man;
import java.awt.Color;
import robocode.*;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.geom.*;


/**
 * Olaaa
 *
 * @author Manuel Sá
 * @author Patrícia Vieira
 */

public class Neticha extends AdvancedRobot {
    final static double BULLET_POWER=3;//Our bulletpower.
    final static double BULLET_DAMAGE=BULLET_POWER*4;//Formula for bullet damage.
    final static double BULLET_SPEED=20-3*BULLET_POWER;//Formula for bullet speed.

	//Variables
    static int direcao=1;
    static double oldEnemyHeading;
    static double enemyEnergy;
    static double energiaEnimigo=100;

	List<WaveBullet> waves = new ArrayList<WaveBullet>();
	static int[] stats = new int[31]; 

    public void run(){
        setBodyColor(Color.red);
        setGunColor(Color.red);
        setRadarColor(Color.red);

        setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		while(true){
			if(getRadarTurnRemaining()==0.0){
				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
			execute();	
		}
    }
	@Override
    public void onScannedRobot(ScannedRobotEvent e){
		double changeInEnergy = energiaEnimigo-e.getEnergy();

		//RADAR
        double posInimigo= getHeadingRadians() +e.getBearingRadians();
		double radar=Utils.normalRelativeAngle(posInimigo-getRadarHeadingRadians());
		double extraT=Math.min(Math.atan(36.0/e.getDistance()),Rules.RADAR_TURN_RATE_RADIANS);
		radar+= (radar < 0 ? -extraT : extraT);
		setTurnRadarRightRadians(radar);
		
		double poslateral=posInimigo+Math.PI/2;
		
		if (changeInEnergy>0 && changeInEnergy<=3) {
			Random ran = new Random();
			int r= ran.nextInt(2);

			setAhead(100*direcao);
            setTurnRightRadians(Utils.normalRelativeAngle(poslateral-getHeadingRadians())+Math.PI/6*-direcao);
            setMaxVelocity(400/getTurnRemaining());
			if(r==0) 
				direcao=-direcao;

        }
		if(e.getVelocity()==0 && e.getEnergy()==0){
			fire(3);
		}
		
		// Enemy absolute bearing, you can use your one if you already declare it.
		double absBearing = getHeadingRadians() + e.getBearingRadians();

		// find our enemy's location:
		double ex = getX() + Math.sin(absBearing) * e.getDistance();
		double ey = getY() + Math.cos(absBearing) * e.getDistance();
		
		// Let's process the waves now:
		for (int i=0; i < waves.size(); i++){
			WaveBullet currentWave = (WaveBullet)waves.get(i);
			if (currentWave.checkHit(ex, ey, getTime())){
				waves.remove(currentWave);
				i--;
			}
		}
		
		double power = 3;
		// don't try to figure out the direcao they're moving 
		// they're not moving, just use the direcao we had before
		if (e.getVelocity() != 0){
			if (Math.sin(e.getHeadingRadians()-absBearing)*e.getVelocity() < 0)
				direcao = -1;
			else
				direcao = 1;
		}
		int[] currentStats = stats; // This seems silly, but I'm using it to
					    // show something else later
		WaveBullet newWave = new WaveBullet(getX(), getY(), absBearing, power,direcao, getTime(), currentStats);

		int bestindex = 15;	// initialize it to be in the middle, guessfactor 0.
		for (int i=0; i<31; i++)
			if (currentStats[bestindex] < currentStats[i])
				bestindex = i;
		
		// this should do the opposite of the math in the WaveBullet:
		double guessfactor = (double)(bestindex - (stats.length - 1) / 2)/ ((stats.length - 1) / 2);
		double angleOffset = direcao * guessfactor * newWave.maxEscapeAngle();
				double gunAdjust = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + angleOffset);
				setTurnGunRightRadians(gunAdjust);


		//TENTAR PERCEBER ESTA PARTE
		if (setFireBullet(3) != null)
			waves.add(newWave);
			if (getGunHeat() == 0 && gunAdjust < Math.atan2(9, e.getDistance()) && setFireBullet(3) != null) {
				//fire(3);
			}


			for(int i=0; i<31;i++) 
			System.out.print(stats[i]+" ");
			System.out.println();


		/*
        //Aim and fire.
		if(e.getDistance()<100 && getEnergy()>=30){
			fire(3);
			System.out.println(e.getDistance()+":3");
		}else {
			if(e.getDistance()<100){
				setAhead(-300);
			}else{
				if(e.getDistance()<200){
					fire(2);
					System.out.println(e.getDistance()+":2");
				}else{
						if(e.getDistance()<400)
							fire(1);
					}
				}
			}
		*/
		
		energiaEnimigo = e.getEnergy();
	}
	public void onHitByBullet(HitByBulletEvent e){
        //setTurnRight(180);
		direcao=-direcao;
		setAhead(100*direcao);

	}
    public void onHitWall(HitWallEvent e){
		stop();
		setTurnRight(90*direcao);
		direcao=-direcao;
		scan();
	}
	
	public class WaveBullet{
		private double startX, startY, startBearing, power;
		private long   fireTime;
		private int    direcao;
		private int[]  returnSegment;
		
		public WaveBullet(double x, double y, double bearing, double power,int direcao, long time, int[] segment){
			startX         = x;
			startY         = y;
			startBearing   = bearing;
			this.power     = power;
			this.direcao = direcao;
			fireTime       = time;
			returnSegment  = segment;
		}
		public double getBulletSpeed(){
			return 20 - 3 * power;
		}
		
		public double maxEscapeAngle(){
			return Math.asin(8 / getBulletSpeed());
		}
		public boolean checkHit(double enemyX, double enemyY, long currentTime){
		// if the distance from the wave origin to our enemy has passed
		// the distance the bullet would have traveled...
			if (Point2D.distance(startX, startY, enemyX, enemyY) <= 
					(currentTime - fireTime) * getBulletSpeed())
			{
				double desiredDirection = Math.atan2(enemyX - startX, enemyY - startY);
				double angleOffset = Utils.normalRelativeAngle(desiredDirection - startBearing);
				double guessFactor =
					Math.max(-1, Math.min(1, angleOffset / maxEscapeAngle())) * direcao;
				int index = (int) Math.round((returnSegment.length - 1) /2 * (guessFactor + 1));
				returnSegment[index]++;
				return true;
			}
			return false;
		}
	}
}

