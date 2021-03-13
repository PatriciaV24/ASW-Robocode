package man;
import java.awt.Color;
import robocode.*;
import robocode.util.Utils;
import java.util.Random;

import javax.swing.plaf.synth.SynthStyle;

/**
 * Olaaa
 *
 * @author Manuel Sá
 * @author Patrícia Vieira
 */

public class Neticha extends AdvancedRobot{
	double energiaEnimigo = 100;
	int dirMovimento = 1;
	int gunDirection = 1;
	int flagshoot=0;

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
		
		//RADAR
		double posInimigo= getHeadingRadians() +e.getBearingRadians();
		double radar=Utils.normalRelativeAngle(posInimigo-getRadarHeadingRadians());
		double extraT=Math.min(Math.atan(36.0/e.getDistance()),Rules.RADAR_TURN_RATE_RADIANS);
		radar+= (radar < 0 ? -extraT : extraT);
		setTurnRadarRightRadians(radar);



		Random ran = new Random();
		int r= ran.nextInt(2);

		setTurnRight(e.getBearing()+90-30*dirMovimento);
		
		if(r==0) dirMovimento=-dirMovimento;

		if (changeInEnergy>0 &&changeInEnergy<=3) {
			flagshoot=1;
			if(e.getDistance()<400){
				setAhead(-(e.getDistance()/4+200)*dirMovimento);
				setTurnRight(e.getBearing()+90-30*dirMovimento);
			}else{
				setAhead((e.getDistance()/4+25)*dirMovimento);
			}
			dirMovimento =-dirMovimento;	
		}else{
			if(e.getDistance()<500 && flagshoot==0){
				setAhead(-(e.getDistance()/4+200)*dirMovimento);
				setTurnRight(e.getBearing()+90-30*dirMovimento);

			}
		}

		gunDirection = -gunDirection;

		

		energiaEnimigo = e.getEnergy();
	}

	public void onHitWall(HitWallEvent e){
		stop();

		
		if(getX()==18){
			if(getHeadingRadians()> -Math.PI/2 && getHeadingRadians()<Math.PI/2){
				System.out.println("Lado Esquerdo, Frente Parede");
				setAhead(-200);
				setTurnRight(45*dirMovimento);

			}else{
				System.out.println("Lado Esquerdo, Tras Parede");
				setAhead(150);
				setTurnRight(45*dirMovimento);
			}
		}
		if(getY()==18){
			if(getHeadingRadians()> -Math.PI/2 && getHeadingRadians()<Math.PI/2){
				System.out.println("Baixo, Frente Parede");
				setAhead(-200);
				setTurnRight(45*dirMovimento);
			}else{
				System.out.println("Baixo, Tras Parede");
				setAhead(150);
				setTurnRight(45*dirMovimento);
			}
		}
		if(getX()==getBattleFieldWidth()-18){
			if(getHeadingRadians()> -Math.PI/2 && getHeadingRadians()<Math.PI/2){
				System.out.println("Lado Direito, Frente Parede");
				setAhead(-200);
				setTurnRight(45*dirMovimento);
			}else{
				System.out.println("Lado Direito, Tras Parede");
				setAhead(150);
				setTurnRight(45*dirMovimento);
			}
		}
		if(getY()==getBattleFieldHeight()-18){
			if(getHeadingRadians()>-Math.PI/2 && getHeadingRadians()<Math.PI/2){
				System.out.println("Cima, Frente Parede");
				setAhead(-200);
				setTurnRight(45*dirMovimento);
			}else{
				System.out.println("Cima, Tras Parede");
				setAhead(150);
				setTurnRight(45*dirMovimento);
			}
		}
		//setAhead(-150*dirMovimento);
		//setTurnRight(45*dirMovimento);
		scan();
	}

	public void onHitByBullet(HitByBulletEvent e){
		setAhead(-300*dirMovimento);
		setTurnRight(-e.getBearing()+90-30*dirMovimento);
	}

	public void onWin(WinEvent e){

	}
}