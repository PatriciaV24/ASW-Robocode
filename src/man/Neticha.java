package man;
import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.*;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.Random;


/**
 * O robo Neticha é a máquina de destruiçao criada com as seguintes caracteristicas:
 *  
 * 	-O movimeto baseia-se na deteçao de disparos do adversrio, o que gera um movimento aleatorio nosso: 
 * 		ou andamos em zigzag ou numa circunferencia em volta do adversario, mantendo sempre uma
 * 		distancia de segurança;
 *  
 * 	-A arma consiste no conceito de wavebullet. Wavebullet e um sistema de deteçao de padroes de movimento
 * 		com pesos nos angulos possiveis para onde posso disparar, e gradualmente reunir informaçao.
 *
 * @author Manuel Sá
 * @author Patrícia Vieira
 */
public class Neticha extends AdvancedRobot{
	double energiaEnimigo = 100;
	int dirMovimento = 1;
	int flagshoot=0;
	int firePow=1;
	double fireSpeed=0; 
	

	//lista que guarda as possibilidades da arma para disparar 
	ArrayList<GunWave> gunWaves=new ArrayList<GunWave>();
	
	//array dos angulos possiveis da arma
	static double [] gunAngles=new double[16];

	public void run() {
	
		//coloca cores e separa o radar e a arma do movimento
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
		
		//variaçao da vida do inimigo
		double changeInEnergy = energiaEnimigo-e.getEnergy();
		
		//Radar do robo
		double posInimigo= getHeadingRadians() +e.getBearingRadians();
		double radar=Utils.normalRelativeAngle(posInimigo-getRadarHeadingRadians());
		double extraT=Math.min(Math.atan(36.0/e.getDistance()),Rules.RADAR_TURN_RATE_RADIANS);
		radar+= (radar < 0 ? -extraT : extraT);
		setTurnRadarRightRadians(radar);

		verificarlimites();

		//Movimento do Robo
		Random ran = new Random();
		int r= ran.nextInt(2);
		
		if(r==0) dirMovimento=-dirMovimento;
			

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
			firePow=3;
		else{
			if(e.getDistance()<300) 
				firePow=2;
			else 
				firePow=1;
			}
		
		
		//prepara o disparo
		if(getGunHeat()==0){
            fireSpeed=20-3*firePow;
			logFiringWave(e);
        }
		
		//verificar a lista das waves
		checkFiringWaves(project(new Point2D.Double(getX(),getY()),e.getDistance(),posInimigo));
		
		setTurnGunRightRadians(Utils.normalRelativeAngle(posInimigo-getGunHeadingRadians())+gunAngles[8+(int)(e.getVelocity()*Math.sin(e.getHeadingRadians()-posInimigo))]);
	        
		setFire(firePow);
		setTurnRadarRightRadians(radar*2);

		verificarlimites();

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

	public void verificarlimites() {
		if(getX()<=36){
			if(getHeadingRadians()>= Math.PI){
				setAhead(-200);
				setTurnRightRadians(Math.PI/4*dirMovimento);

			}else{
				setAhead(200);
				setTurnRightRadians(Math.PI/4*dirMovimento);
			}
		}
		if(getY()<=36){
			if(getHeadingRadians()>= Math.PI/2 && getHeadingRadians()<= (3*Math.PI)/2){
				setAhead(-200);
				setTurnRightRadians(Math.PI/4*dirMovimento);
			}else{
				setAhead(200);
				setTurnRightRadians(Math.PI/4*dirMovimento);
			}
		}
		if(getX()>=getBattleFieldWidth()-36){
			if(getHeadingRadians()>= 0 && getHeadingRadians()<=Math.PI){
				setAhead(-200);
				setTurnRightRadians(Math.PI/4*dirMovimento);
			}else{
				setAhead(200);
				setTurnRightRadians(Math.PI/4*dirMovimento);
			}
		}
		if(getY()>=getBattleFieldHeight()-36){
			if(getHeadingRadians()<= Math.PI/2 || getHeadingRadians()>=(3*Math.PI)/2){
				setAhead(-200);
				setTurnRightRadians(Math.PI/4*dirMovimento);
			}else{
				setAhead(200);
				setTurnRightRadians(Math.PI/4*dirMovimento);
			}
		}

	}

	 //Esta classe contem as informaçoes de uma preparaçao de disparo
	class GunWave{ 
		double speed;
		Point2D.Double origin;
		int velSeg;
		double absBearing;
		double startTime;
	}	

	//verifica se uma preparaçao falhou ...se falhou remove o da lista e ajusta o angulo
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

	//adiciona uma possivel preparaçao da arma a lista
	public void logFiringWave(ScannedRobotEvent e){
        GunWave w=new GunWave();
        w.absBearing=e.getBearingRadians()+getHeadingRadians();
        w.speed=fireSpeed;
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

	

