package man;
import java.awt.Color;
import robocode.*;

public class Neticha extends AdvancedRobot{
double energiaEnimigo = 100;
int dirMovimento = 1;
int gunDirection = 1;
public void run() {
    setColors(Color.black,Color.cyan, Color.white);
  setTurnGunRight(9999);
}
public void onScannedRobot(ScannedRobotEvent e) {
    // Stay at right angles to the opponent
   // setTurnRight(e.getBearing()+90-
     //  30dirMovimento);

      setTurnRight(e.getBearing()+90-30*dirMovimento);

   // If the bot has small energy drop,
  // assume it fired
  double changeInEnergy =
    energiaEnimigo-e.getEnergy();
  if (changeInEnergy>0 &&
      changeInEnergy<=3) {
       // Dodge!
       dirMovimento =
       -dirMovimento;
       setAhead((e.getDistance()/4+25)*dirMovimento);
   }
  // When a bot is spotted,
  // sweep the gun and radar
  gunDirection = -gunDirection;
  setTurnGunRight(99999*gunDirection);

  // Fire directly at target
  if(e.getDistance()<300){
    if(e.getDistance()<50 && getEnergy()>=30) fire(Rules.MAX_BULLET_POWER);
    else fire(2) ;
  }

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
