package man;
import java.awt.Color;
import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;


public class Neticha extends AdvancedRobot{
	double energiaEnimigo = 100;
	int dirMovimento = 1;
	int gunDirection = 1;
	double gunTurnAmt; // How much to turn our gun when searching

	public void run() {
		setColors(Color.black,Color.cyan, Color.white);
		/*MUDAR AS CORES NAS DIFERENTES OPÇOES
		setBodyColor(Color.);
		setGunColor(Color.);
		setRadarColor(Color.);
		setScanColor(Color.);
		setBulletColor(Color.);		
		*/
		setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
        setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
		while(true){
			scan();
			turnGunRight(360);
		}
	}
	public void onScannedRobot(ScannedRobotEvent e) {
		double changeInEnergy = energiaEnimigo-e.getEnergy();
		
		//normalRelativeAngleDegrees -> Coloca o angulo entre 180 a -180
		//e.getBearing-> angulo que o robot ad esta
		//getHeading() -> 
		gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
		System.out.println("Adversario" + e.getBearing());
		System.out.println("Robot em relão ao campo"+ getHeading());
		System.out.println("Radar do Robo"+ getRadarHeading());
		
		

		setTurnGunRight(gunTurnAmt); // and see how much Tracker improves...
		fire(.5);

		/*
		if(gunTurnAmt<45 && gunTurnAmt>-45){
			setTurnRight(90-gunTurnAmt);
			setTurnRadarRight(-90-gunTurnAmt);
		}
			
		

		// If the bot has small energy drop,
		// assume it fired
		if (changeInEnergy>0 && changeInEnergy<=3) {
			dirMovimento =-dirMovimento;
			setAhead((e.getDistance()/4+25)*dirMovimento);
		}
		
		// When a bot is spotted,
		// sweep the gun and radar
		//gunDirection = -gunDirection;
		//setTurnGunRight(gunTurnAmt+30-90*dirMovimento); 
		
		if(e.getDistance()>500)
			fire(1);
		if(e.getDistance()<200){
			if(e.getDistance()<50 && getEnergy()>=30) fire(3);
			else fire(2) ;
		}
		*/
		// Track the energy level
		energiaEnimigo = e.getEnergy();

	}

	public void onHitWall(HitWallEvent e){
		setTurnRight(45);
	}

	public void onHitByBullet(HitByBulletEvent e){
		//setTurnRigth(e.getBearing());
	}

	public void onWin(WinEvent e){

	}
}

