package man;
import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.*;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.Random;


/**
 * Olaaa
 *
 * @author Manuel Sá
 * @author Patrícia Vieira
 */

 //Esta classe cria uma estrutura de disparo
class GunWave{
    double speed;
    Point2D.Double origin;
    int velSeg;
    double absBearing;
    double startTime;
}	

public class Neticha extends AdvancedRobot{
	double energiaEnimigo = 100;
	int dirMovimento = 1;
	int flagshoot=0;
	int FIRE_POW=1;
	double FIRE_SPEED=20-3*FIRE_POW;
	
	//lista que guarda os tiros dados 
	ArrayList<GunWave> gunWaves=new ArrayList<GunWave>();
	static double [] gunAngles=new double[16];

	public void run() {
		setColors(Color.white, Color.white, Color.white);
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
		double changeInEnergy = energiaEnimigo-e.getEnergy();
		
		//Radar do robo
		double posInimigo= getHeadingRadians() +e.getBearingRadians();
		double radar=Utils.normalRelativeAngle(posInimigo-getRadarHeadingRadians());
		double extraT=Math.min(Math.atan(36.0/e.getDistance()),Rules.RADAR_TURN_RATE_RADIANS);
		radar+= (radar < 0 ? -extraT : extraT);
		setTurnRadarRightRadians(radar);

		//Movimento do Robo
		Random ran = new Random();
		int r= ran.nextInt(2);

		setTurnRightRadians(e.getBearingRadians()+Math.PI/2-Math.PI/6*dirMovimento);
		
		if(r==0) {
			dirMovimento=-dirMovimento;
			
		}

		if (changeInEnergy>0 &&changeInEnergy<=3) {
			setColors(Color.black, Color.black, Color.black);
			flagshoot=1;
			if(e.getDistance()<200){
				setAhead(-(e.getDistance()/4+200)*dirMovimento);
				setTurnRightRadians(e.getBearingRadians()+Math.PI/2-Math.PI/6*dirMovimento);
			}else{
				setAhead((e.getDistance()/4+25)*dirMovimento);
			}
			dirMovimento =-dirMovimento;	
			if(e.getDistance()>450){
                setAhead((e.getDistance()/4+25)*dirMovimento);
				setTurnRightRadians(e.getBearingRadians()+Math.PI/2-Math.PI/6*dirMovimento);
            }
		}else{
			if(e.getDistance()<300 && flagshoot==0){
				setAhead(-(e.getDistance()/4+200)*dirMovimento);
				setTurnRightRadians(e.getBearingRadians()+Math.PI/2-Math.PI/6*dirMovimento);
			}
		}

	
		//Arma do robo
		if(e.getDistance()<150) 
			FIRE_POW=3;
		else{
			if(e.getDistance()<300) 
				FIRE_POW=2;
			else 
				FIRE_POW=1;
			}
		
		
		if(getGunHeat()==0){
            FIRE_SPEED=20-3*FIRE_POW;
			logFiringWave(e);
        }
		
		checkFiringWaves(project(new Point2D.Double(getX(),getY()),e.getDistance(),posInimigo));
		
		setTurnGunRightRadians(Utils.normalRelativeAngle(posInimigo-getGunHeadingRadians())+gunAngles[8+(int)(e.getVelocity()*Math.sin(e.getHeadingRadians()-posInimigo))]);
	        
			setFire(FIRE_POW);

		setTurnRadarRightRadians(radar*2);

		energiaEnimigo = e.getEnergy();
	}

	public void onHitWall(HitWallEvent e){
		dirMovimento=-dirMovimento;
	}

	public void onHitByBullet(HitByBulletEvent e){
		setAhead(-300*dirMovimento);
		setTurnRightRadians(e.getBearingRadians()+Math.PI/2-Math.PI/6*dirMovimento);
	}

	public void onWin(WinEvent e){
		setColors(Color.white, Color.white, Color.white);

	}

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

	public void logFiringWave(ScannedRobotEvent e){
        GunWave w=new GunWave();
        w.absBearing=e.getBearingRadians()+getHeadingRadians();
        w.speed=FIRE_SPEED;
        w.origin=new Point2D.Double(getX(),getY());
        w.velSeg=(int)(e.getVelocity()*Math.sin(e.getHeadingRadians()-w.absBearing));
        w.startTime=getTime();
        gunWaves.add(w);
    }
	
	//este metodo permite nos saber uma posiçao a partir de um angulo e outra coordenada 
	public Point2D.Double project(Point2D.Double origin,double dist,double angle){
	    return new Point2D.Double(origin.x+dist*Math.sin(angle),origin.y+dist*Math.cos(angle));
	}

}	

	

