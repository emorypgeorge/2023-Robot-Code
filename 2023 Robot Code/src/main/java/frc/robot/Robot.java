//General Imports
package frc.robot;
import java.lang.Math;

import javax.lang.model.util.ElementScanner14;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

//REV Imports
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;

//navX Imports: https://dev.studica.com/releases/2023/NavX.json
import edu.wpi.first.wpilibj.SPI;
import com.kauailabs.navx.frc.AHRS;

//Pneumatic Imports
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;

//Do we need these???
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.PneumaticHub;

//Timer impoerts
import edu.wpi.first.wpilibj.Timer;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */

public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private RobotDrivetrain drivetrain = new RobotDrivetrain();
  private XboxController controller0 = new XboxController(0);
  //The navX
  private AHRS navX = new AHRS(SPI.Port.kMXP);
  private boolean autoBalanceXMode;
  private static final double balanceThreshold = 5;
  private boolean autoAngle;

  //Arm and Claw
  private CANSparkMax armMotor = new CANSparkMax(9, MotorType.kBrushless);
  private CANSparkMax clawWheels = new CANSparkMax(6, MotorType.kBrushless);
  private RelativeEncoder encoderArm = armMotor.getEncoder();
  private RelativeEncoder encoderWheels = clawWheels.getEncoder();
  //Timer
  private double startTime;
  // Pneumatics Initialization
  private final Solenoid s1 = new Solenoid(10, PneumaticsModuleType.REVPH, 0);
  private double time = Timer.getFPGATimestamp();
  

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {}

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    drivetrain.setEncoderReset();
    navX.zeroYaw();
    startTime = Timer.getFPGATimestamp();
    navX.resetDisplacement();
  }

  /** This function is called periodically during autonomous. */

  @Override
  public void autonomousPeriodic() {
    //SmartDashboard.putNumber("Pitch Angle", navX.getPitch());
    //Made it so only distance needs to be changed to go foward or backwards.
    //robot info
    // straight = middle, left = left, right = right
    boolean straight = true;
    boolean right = false;
    boolean left = false;
    //if H-Drive fails
    boolean hDriveNotWork = false;
    //Precondition if we are going to AutoBalance or let our teammates do it
    boolean ifAutoBalance = false;
    //Determines if we start with cone or with the cube
    //True = cone, false = cube
    boolean coneOrCube = true;
    drivetrain.encoderInfo();
    double x = navX.getDisplacementX();
    double y = navX.getDisplacementY();
    robotInfo();
    // Conversion from in to m is 0.0254
    double initialX = 10.5 * 0.0254;
    double initialY = 21.75 * 0.0254;
    // size of cube
    double cube = 9.75 * 0.0254;
    // size of cone
    double cone = 8.5 * 0.0254;
    // Auto if we use cone + Straigh ahead
    if(coneOrCube){
      while (time - startTime < 15){
        if (straight){
          while(x > (initialX + cone)/2){
            drivetrain.curvatureDrive(-0.01, 0);
          }
          while(x < ((1.54 - (cone + initialX))+ 1.9 + (2.16/3))){
            drivetrain.curvatureDrive(0.08, 0);
          }
          while(x > ((2.16/3) - 1.9/3)){
            drivetrain.curvatureDrive(-0.03, 0);
          }
          if(ifAutoBalance){
            AutoBalance();
          }
          else 
            drivetrain.curvatureDrive(0,0);
        }
        else if(right){
          if (x > (initialX + cone)/2){
            drivetrain.curvatureDrive(-0.1, 0);
          }
          else if(x < ((1.54 - (cone + initialX))+ 1.9 + (2.16/3))){
              drivetrain.curvatureDrive(0.8, 0);
          }
          else if(x > ((2.16/3) - 1.9/3)){
            if (y > -1 * ((1.5 + (2.44/2)) - initialY)){
              drivetrain.hDriveMovement(1);
            }
            else
              drivetrain.curvatureDrive(-0.3, 0);
          }
          else if(ifAutoBalance){
            AutoBalance();
          }
          else 
            drivetrain.curvatureDrive(0,0);
        }
        else if (left){
          if (x > ((initialX + cone)/2)){
            drivetrain.curvatureDrive(-0.1, 0);
          }
          else if(x < ((1.54 - (cone + initialX))+ 1.9 + (2.16/3))){
            drivetrain.curvatureDrive(0.8, 0);
          }
          else if(x > ((2.16/3) - 1.9/3)){
            if (y < initialY + (1.4 + (2.44/2))){
              drivetrain.hDriveMovement(1);
            }
            else
              drivetrain.curvatureDrive(-0.3, 0);
          }
          else if(ifAutoBalance){
            AutoBalance();
          }
          else 
            drivetrain.curvatureDrive(0,0);
        }
      }
    }
    // Auto if we use cube + Straigh ahead
    if (!coneOrCube){
      while (time - startTime < 15){
        if (straight){
          if (x > ((initialX + cube)/2)){
            drivetrain.curvatureDrive(-0.1, 0);
          }
          else if(x < ((1.54 - (cube + initialX))+ 1.9 + (2.16/3))){
            drivetrain.curvatureDrive(0.8, 0);
          }
          else if(x > ((2.16/3) - 1.9/3)){
            drivetrain.curvatureDrive(-0.5, 0);
          }
          else if(ifAutoBalance){
            AutoBalance();
          }
          else 
            drivetrain.curvatureDrive(0,0);
        }
        else if(right){
          if (x > (initialX + cube)/2){
            drivetrain.curvatureDrive(-0.1, 0);
          }
          else if(x < ((1.54 - (cube + initialX))+ 1.9 + (2.16/3))){
            drivetrain.curvatureDrive(0.8, 0);
          }
          else if(x > ((2.16/3) - 1.9/3)){
            if (y > -1 * ((1.5 + (2.44/2)) - initialY)){
              drivetrain.hDriveMovement(1);
            }
            else
              drivetrain.curvatureDrive(-0.3, 0);
          }
          else if(ifAutoBalance){
            AutoBalance();
          }
          else 
            drivetrain.curvatureDrive(0,0);
        }
        else if(left){
          if (x > ((initialX + cone)/2)){
            drivetrain.curvatureDrive(-0.1, 0);
          }
          else if(x < ((1.54 - (cone + initialX))+ 1.9 + (2.16/3))){
            drivetrain.curvatureDrive(0.8, 0);
          }
          else if(x > ((2.16/3) - 1.9/3)){
            if (y < initialY + (1.4 + (2.44/2))){
              drivetrain.hDriveMovement(1);
            }
            else
              drivetrain.curvatureDrive(-0.3, 0);
          }
          else if(ifAutoBalance){
            AutoBalance();
          }
          else 
            drivetrain.curvatureDrive(0,0);
        }
      }
    }
  }


  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    drivetrain.setEncoderReset();
    navX.zeroYaw();
    navX.resetDisplacement();
  }

  public double getArmEncoderPosition(){
    return encoderArm.getPosition();
  }

  public double getWhellEncoderPosition(){
    return encoderWheels.getPosition();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    //D-pad functionality works regardless of drive type chosen
    
    //Tank Drive
    //Need y-axis for each stick
    //Hand.kLeft gives the left analog stick and Hand.kRight gives the right analog stick
    //Speeds are currently set at 50%
    //drivetrain.tankDrive(-0.5*controller.getLeftY(), -0.5*controller.getRightY()); 
    drivetrain.encoderInfo();
    //Information for Pitch, Yaw, Speed, and Position
    robotInfo();
    //Curvature Drive  
    double forwardSpeed = controller0.getRightTriggerAxis(); //forward speed from right trigger
    double reverseSpeed = controller0.getLeftTriggerAxis(); //reverse speed from left trigger
    boolean leftSpeed = controller0.getLeftBumper(); //right direction from right bumper
    boolean rightSpeed = controller0.getRightBumper(); //left direction from left bumper
    double turn = controller0.getLeftX(); //gets the direction from the left analog stick
    
    if (forwardSpeed > 0){
      drivetrain.curvatureDrive(forwardSpeed, -1*turn);
    }
    else if (reverseSpeed > 0){
      drivetrain.curvatureDrive(-.5*reverseSpeed, -.5*turn);
    }
    else{
      drivetrain.curvatureDrive(0,0);
    }    
    
    //h-drive
    if (leftSpeed){
      drivetrain.hDriveMovement(-0.5);
    }
    else if (rightSpeed){
      drivetrain.hDriveMovement(0.5);
    }
    else{
      drivetrain.hDriveMovement(0);
    }

    //D-Pad controls for fine movements
    int dPad = controller0.getPOV(); //scans to see which directional arrow is being pushed
    drivetrain.dPadGetter(dPad);
    
    /* this function moves the arm up and down, 
    it has a set limit that needs to be found so we can input it 
    theses codes are thoughts of possible code for the arm, don't know how to access motor currently
    so it's just a thought
    THIS DOES NOT WORK YET!!! BE CAREFUL!!!
    */
    
    //arm code
    boolean armUp = controller0.getYButton();
    boolean armDown = controller0.getXButton();
    double upLimit = 50.0;
    double downLimit = 100.0;
    SmartDashboard.putNumber("Encoder Position Arm", encoderArm.getPosition());
    if (getArmEncoderPosition() < upLimit){
      if(armUp){
        armMotor.set(-0.25);
      }
      else {
        armMotor.set(0);
      }
    }
    if (getArmEncoderPosition() > downLimit) {
      if(armDown){
        armMotor.set(0.25);
      }
      else {
        armMotor.set(0);
      }
    }

    //claw code
    boolean clawClose = controller0.getAButton();
    if (clawClose) {
      s1.set(true);
    }
    else{
      s1.set(false);
    }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}
//pilk is good
  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}

  //AutoBalance for autonomous period 
  //Should make the robot speed slow as it gets closer to equilibrium
  //It seems to be unable to switch from Teleop to Auto and Back
  public void AutoBalance(){
    double pitchDegrees = navX.getPitch();
    if (!autoBalanceXMode && (Math.abs(pitchDegrees) > Math.abs(balanceThreshold))){
      autoBalanceXMode = true;
    }
    if (autoBalanceXMode && (Math.abs(pitchDegrees) <= Math.abs(balanceThreshold))){
      autoBalanceXMode = false;
    } 
    if (autoBalanceXMode){
      double pitchAngleRadians = pitchDegrees * (Math.PI / 180.0);
      double balanceSpeed = Math.sin(pitchAngleRadians) * -1; 
      drivetrain.curvatureDrive(balanceSpeed, 0);
    }
  }
  
  public void angleRotation(double angle){
    double yawDegrees = navX.getAngle();
    double angleSpeed = 0;
    if (!autoAngle && (yawDegrees < angle)){
      autoAngle = true;
    }
    if (autoAngle && (yawDegrees >= angle)){
      autoAngle = false;
    }
    if(autoAngle){
      double yawRadians = yawDegrees * (Math.PI / 180.0);
      if(angle > 90){
        angleSpeed = 0.5 * (Math.cos(.5*yawRadians) + 0.29);
      }
      else if (angle <= 90 ){
        angleSpeed = 0.5 * (Math.cos(yawRadians) + 0.2);
      }
      drivetrain.tankDrive(-angleSpeed, angleSpeed);
    }
  }
  public void robotInfo(){
    SmartDashboard.putNumber("Time", time);
    SmartDashboard.putNumber("Pitch Angle", navX.getPitch());
    SmartDashboard.putNumber("Yaw Angle", navX.getAngle());
    SmartDashboard.putNumber("Velocity", navX.getVelocityX());
    SmartDashboard.putNumber("X Displacement", navX.getDisplacementX());
    SmartDashboard.putNumber("Y Displacement", navX.getDisplacementY());
  }
}
