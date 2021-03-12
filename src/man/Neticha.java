package man;
import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.*;
import robocode.util.Utils;
/**
 * SuperSpinBot - a Super Sample Robot by CrazyBassoonist based on the robot RamFire by Mathew Nelson and maintained by Flemming N. Larsen.
 *
 * This robot tries to ram it's opponents.
 *
 */
public class Neticha extends AdvancedRobot {

    //These are constants. One advantage of these is that the logic in them (such as 20-3*BULLET_POWER)
    //does not use codespace, making them cheaper than putting the logic in the actual code.

    final static double BULLET_POWER=3;//Our bulletpower.
    final static double BULLET_DAMAGE=BULLET_POWER*4;//Formula for bullet damage.
    final static double BULLET_SPEED=20-3*BULLET_POWER;//Formula for bullet speed.

    //Variables
    static double direcao=1;
    static double oldEnemyHeading;
    static double enemyEnergy;
    static double energiaEnimigo=100;


    public void run(){
        setBodyColor(Color.red);
        setGunColor(Color.red);
        setRadarColor(Color.red);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    }
    public void onScannedRobot(ScannedRobotEvent e){
        double changeInEnergy = energiaEnimigo-e.getEnergy();
        double posInimigo=e.getBearingRadians()+getHeadingRadians();

        //This makes the amount we want to turn be perpendicular to the enemy.
        double poslateral=posInimigo+Math.PI/2;

        //This formula is used because the 1/e.getDistance() means that as we get closer to the enemy, we will turn to them more sharply.
        //We want to do this because it reduces our chances of being defeated before we reach the enemy robot.
        //poslateral-=Math.max(0.5,(1/e.getDistance())*100)*direcao;
        if (changeInEnergy>0 && changeInEnergy<=3) {
            setTurnRightRadians(Utils.normalRelativeAngle(poslateral-getHeadingRadians()));
            setMaxVelocity(400/getTurnRemaining());
            setAhead(100*direcao);
            direcao=-direcao;
        }
  

        //Finding the heading and heading change.
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        oldEnemyHeading = enemyHeading;

        /*This method of targeting is know as circular targeting; you assume your enemy will
           *keep moving with the same speed and turn rate that he is using at fire time.The
           *base code comes from the wiki.
          */
        double deltaTime = 0;
        double predictedX = getX()+e.getDistance()*Math.sin(posInimigo);
        double predictedY = getY()+e.getDistance()*Math.cos(posInimigo);
        while((++deltaTime) * BULLET_SPEED <  Point2D.Double.distance(getX(), getY(), predictedX, predictedY)){

            //Add the movement we think our enemy will make to our enemy's current X and Y
            predictedX += Math.sin(enemyHeading) * e.getVelocity();
            predictedY += Math.cos(enemyHeading) * e.getVelocity();


            //Find our enemy's heading changes.
            enemyHeading += enemyHeadingChange;

            //If our predicted coordinates are outside the walls, put them 18 distance units away from the walls as we know
            //that that is the closest they can get to the wall (Bots are non-rotating 36*36 squares).
            predictedX=Math.max(Math.min(predictedX,getBattleFieldWidth()-18),18);
            predictedY=Math.max(Math.min(predictedY,getBattleFieldHeight()-18),18);

        }
        //Find the bearing of our predicted coordinates from us.
        double aim = Utils.normalAbsoluteAngle(Math.atan2(  predictedX - getX(), predictedY - getY()));

        //Aim and fire.
        setTurnGunRightRadians(Utils.normalRelativeAngle(aim - getGunHeadingRadians()));
       if(e.getDistance()>300 && e.getDistance()<500){
            fire(1);
            System.out.println(e.getDistance()+": 1");
        }else{
            if(e.getDistance()<100 && getEnergy()>=30){ 
                fire(3);
                System.out.println(e.getDistance()+":3");
            }else {                        fire(2);
                System.out.println(e.getDistance()+": 2");
            }
        }

        setTurnRadarRightRadians(Utils.normalRelativeAngle(posInimigo-getRadarHeadingRadians())*2);
    
        energiaEnimigo = e.getEnergy();
    }
    public void onBulletHit(BulletHitEvent e){
        enemyEnergy-=BULLET_DAMAGE;
        
    }
    public void onHitByBullet(HitByBulletEvent e){
        setAhead(100*direcao);
        setTurnRight(180);
        direcao=-direcao;
    }
    public void onHitWall(HitWallEvent e){
        direcao=-direcao;
        /*if(getY()==0 && getHeadingRadians() < -Math.PI/2 && getHeadingRadians()<Math.PI/2){
            ahead(-100);
            turnRight(getHeadingRadians()+90);
        }else{
            ahead(100);
            turnRight(getHeadingRadians()+90);
        }
        if(getX()==0 && getHeadingRadians() < 0 && getHeadingRadians() < Math.PI){
            ahead(-100);
            turnRight(getHeadingRadians()+90);

        }else{
            ahead(100);
            turnRight(getHeadingRadians()+90);
        }
        if(getY()==getBattleFieldHeight() && getHeadingRadians() < -Math.PI/2 && getHeadingRadians()<Math.PI/2){
            ahead(-100);
            turnRight(getHeadingRadians()+90);
        }else{
            ahead(100);
            turnRight(getHeadingRadians()+90);
        }
        if(getX()==getBattleFieldWidth() && getHeadingRadians() < 0 && getHeadingRadians()< -Math.PI){
            ahead(-100);
            turnRight(getHeadingRadians()+90);
  
        }else{
            ahead(100);
            turnRight(getHeadingRadians()+90);

        }*/


    }
}