package frc.robot;

//external imports

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
import com.ctre.phoenix.motorcontrol.SensorTerm;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

import edu.wpi.first.cameraserver.CameraServer;
//internal imports
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;

import static edu.wpi.first.wpilibj.DoubleSolenoid.Value.*;
import edu.wpi.first.wpilibj.Relay.Direction;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DigitalInput;


// Welcome to the 2020 Robot Code!
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
 /* //Ultrasonic
  private static final int kUltrasonicPort = 1;
  private final AnalogInput sonic = new AnalogInput(kUltrasonicPort);
*/
  //Auto
  private static final String kSimpleBLF = "Simple BLF";
  private static final String kSimpleBLB = "Simple BLB";
  private static final String kSimpleFLF = "Simple FLF";
  private static final String kSimpleFLB = "Simple FLB";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  //Drivetrain Init
  WPI_VictorSPX _rightFront = new WPI_VictorSPX(02);
  WPI_VictorSPX _rightRear = new WPI_VictorSPX(04);
  WPI_VictorSPX _leftFront = new WPI_VictorSPX(01);
  WPI_VictorSPX _leftRear = new WPI_VictorSPX(03);

  private SpeedControllerGroup _leftController = new SpeedControllerGroup(_leftFront, _leftRear);
  private SpeedControllerGroup _rightController = new SpeedControllerGroup(_rightFront, _rightRear);

  DifferentialDrive _diffDrive = new DifferentialDrive(_leftController, _rightController);

  /*Faults _faults_L = new Faults();
  Faults _faults_R = new Faults();*/

  //Joystick init
  Joystick leftJoy = new Joystick(0);
  Joystick rightJoy = new Joystick(1);
  Joystick xbox = new Joystick(2);

  //Motor init
  TalonFX shootMotor = new TalonFX(8);
  VictorSPX siloMotor = new VictorSPX(06);
  VictorSPX intakeMotor = new VictorSPX(05);
  VictorSPX climbMotor = new VictorSPX(07);

  //Light init
  private AddressableLED leds = new AddressableLED(6);;
  private AddressableLEDBuffer m_ledBuffer = new AddressableLEDBuffer(75);

  // Store what the last hue of the first pixel is
  private int m_rainbowFirstPixelHue;

  //Pneumatic init
  DoubleSolenoid intakeSolenoid = new DoubleSolenoid(0, 1);
  DoubleSolenoid climberSolenoid = new DoubleSolenoid(2, 3);

  //Limit Switche init
  private DigitalInput siloEnterLimit;
  private DigitalInput siloEndLimit;

  @Override
  public void robotInit() {
    //Auto
    m_chooser.setDefaultOption("Simple BLF", kSimpleBLF);
    m_chooser.addOption("Simple BLB", kSimpleBLB);
    m_chooser.addOption("Simple FLF", kSimpleFLF);
    m_chooser.addOption("Simple FLB", kSimpleFLB);
    SmartDashboard.putData("Auto choices", m_chooser);

    //Lights
    leds.setLength(m_ledBuffer.getLength());

    //// Set the data
    leds.setData(m_ledBuffer);
    leds.start();

    //Limit Switches
    siloEnterLimit = new DigitalInput(1);
    siloEndLimit = new DigitalInput(4);
    
    CameraServer.getInstance().startAutomaticCapture();
  }

  //Toggles
  boolean shooterToggleOn = false;
  boolean shooterTogglePressed = false;
  boolean siloFull = false;
  private double autoStartTime;

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */

  /*@Override
  public void disabledPeriodic() {
    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      //Sets the specified LED to the RGB values for green
      m_ledBuffer.setRGB(i, 0, 255, 0);}
    leds.setData(m_ledBuffer);
    super.disabledPeriodic();
  }*/
  //Rainbow Code
  private void rainbow() {
    // For every pixel
    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      // Calculate the hue - hue is easier for rainbows because the color
      // shape is a circle so only one value needs to precess
      final var hue = (m_rainbowFirstPixelHue + (i * 180 / m_ledBuffer.getLength())) % 180;
      // Set the value
      m_ledBuffer.setHSV(i, hue, 255, 128);
    }
    // Increase by to make the rainbow "move"
    m_rainbowFirstPixelHue += 3;
    // Check bounds
    m_rainbowFirstPixelHue %= 180;
  }

  @Override
  public void robotPeriodic() {
    SmartDashboard.putBoolean("Silo Full", siloFull);
    ////rainbow();
    //for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      ////Sets the specified LED to the RGB values for green
      //m_ledBuffer.setRGB(i, 0, 255, 0);}
    //leds.setData(m_ledBuffer);
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    autoStartTime = Timer.getFPGATimestamp();
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    rainbow();
    leds.setData(m_ledBuffer);
    double currTime = Timer.getFPGATimestamp();
    double timeElapsed = currTime - autoStartTime;
    switch (m_autoSelected) {
      case kSimpleBLB:
      if( timeElapsed > 0 && timeElapsed < 5) {
        shootMotor.set(ControlMode.PercentOutput, 0.8);
      }
      if( timeElapsed > 2 && timeElapsed < 5) {
        siloMotor.set(ControlMode.PercentOutput, -0.8);
      }
      if( timeElapsed > 5 && timeElapsed < 8){
        shootMotor.set(ControlMode.PercentOutput, 0);
        siloMotor.set(ControlMode.PercentOutput, 0);
        _diffDrive.arcadeDrive(-0.5, 0);
      }
      if( timeElapsed > 8){
        _diffDrive.arcadeDrive(0, 0);
      }
        break;
      case kSimpleBLF:
      default:
      if( timeElapsed > 0 && timeElapsed < 5) {
        shootMotor.set(ControlMode.PercentOutput, 0.8);
      }
      if( timeElapsed > 2 && timeElapsed < 5) {
        siloMotor.set(ControlMode.PercentOutput, -0.8);
      }
      if( timeElapsed > 5 && timeElapsed < 8){
        shootMotor.set(ControlMode.PercentOutput, 0);
        siloMotor.set(ControlMode.PercentOutput, 0);
        _diffDrive.arcadeDrive(0.5, 0);
      }
      if( timeElapsed > 8){
        _diffDrive.arcadeDrive(0, 0);
      }
        break;
      case kSimpleFLB:
      if( timeElapsed > 0 && timeElapsed < 5) {
        shootMotor.set(ControlMode.PercentOutput, 0.8);
      }
      if( timeElapsed > 2 && timeElapsed < 5) {
        siloMotor.set(ControlMode.PercentOutput, -0.8);
      }
      if( timeElapsed > 5 && timeElapsed < 8){
        shootMotor.set(ControlMode.PercentOutput, 0);
        siloMotor.set(ControlMode.PercentOutput, 0);
        _diffDrive.arcadeDrive(-0.5, 0);
      }
      if( timeElapsed > 8){
        _diffDrive.arcadeDrive(0, 0);
      }
      break;
      case kSimpleFLF:
      if( timeElapsed > 0 && timeElapsed < 5) {
        shootMotor.set(ControlMode.PercentOutput, 0.8);
      }
      if( timeElapsed > 2 && timeElapsed < 5) {
        siloMotor.set(ControlMode.PercentOutput, -0.8);
      }
      if( timeElapsed > 5 && timeElapsed < 8){
        shootMotor.set(ControlMode.PercentOutput, 0);
        siloMotor.set(ControlMode.PercentOutput, 0);
        _diffDrive.arcadeDrive(0.5, 0);
      }
      if( timeElapsed > 8){
        _diffDrive.arcadeDrive(0, 0);
      }
      break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    //updateShooterToggle();
    //rainbow();
    /*for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      // Sets the specified LED to the RGB values for green
      m_ledBuffer.setRGB(i, 0, 255, 0);}
    leds.setData(m_ledBuffer);*/
    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      //Sets the specified LED to the RGB values for green
      m_ledBuffer.setRGB(i, 0, 255, 0);}
    leds.setData(m_ledBuffer);
    //Variables
    final double climbSpeed = -1;
    final double turnSpeed = 0.5;
    boolean readyToShoot = false;
    final double fireRPM = 6000;
    boolean shooter = false;
    boolean portal = false;
    boolean silo = false;
    boolean siloUnloading = false;
    boolean shootMotorActive = false;
    boolean shootUnload = false;
    boolean intakeActive = false;
    boolean intakeUnload = false;
    boolean siloUnload = false;
    boolean climberActive0 = false;
    boolean climberActive1 = false;
    boolean disableSilo = false;
    float KpAim = -0.1f;
    float KpDistance = -0.1f;
    float min_aim_command = 0.05f;

    // Limelight
    final NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
    final NetworkTableEntry txd = table.getEntry("tx");
    final NetworkTableEntry tyd = table.getEntry("ty");
    final NetworkTableEntry tad = table.getEntry("ta");

    // read values periodically
    double tx = table.getEntry("tx").getDouble(0);
    double ty = table.getEntry("ty").getDouble(0);
    double area = table.getEntry("ta").getDouble(0);
    double v = table.getEntry("tv").getDouble(0);

    // post to smart dashboard periodically
    SmartDashboard.putNumber("LimelightX", tx);
    SmartDashboard.putNumber("LimelightY", ty);
    SmartDashboard.putNumber("LimelightArea", area);
    SmartDashboard.putNumber("LimelightValid", v);
    SmartDashboard.putBoolean("Ready to Fire", readyToShoot);

    //Get joystick values
    double left = -1 * leftJoy.getRawAxis(1);
    double right = -1 * rightJoy.getRawAxis(1);
    final boolean rightMadTrigger = rightJoy.getRawButton(1);
    final double xboxRightTrigger = xbox.getRawAxis(3);
    final boolean xboxRightBumper = xbox.getRawButton(6);
    final double xboxLeftTrigger = xbox.getRawAxis(2);
    boolean xboxRightStickButton = xbox.getRawButtonPressed(9);
    final boolean leftMadTrigger = leftJoy.getRawButton(1);
    final boolean leftMadDown = leftJoy.getRawButton(2);
    final boolean xboxY = xbox.getRawButton(1);
    final boolean xboxA = xbox.getRawButton(4);
    boolean xboxX = xbox.getRawButton(3);
    final boolean leftMadUp = leftJoy.getRawButton(3);
    final boolean xboxLeftBumper = xbox.getRawButton(5);
    boolean xboxLeftStickButton = xbox.getRawButtonPressed(10);
    double xboxLeftStick = xbox.getRawAxis(1);

    //Drive code
    //Align
    if (rightMadTrigger == true) {
      double heading_error = -tx;
      double distance_error = -ty;
      double steering_adjust = 0.0f;
      if (v == 1.0){
        if (tx > 1.0){
          steering_adjust = KpAim * heading_error - min_aim_command;
        }
        else if (tx < 1.0){
          steering_adjust = KpAim * heading_error + min_aim_command;
        }
        double distance_adjust = KpDistance * distance_error;

        _diffDrive.arcadeDrive(0, steering_adjust);
    }
    else if (v == 0.0){
      _diffDrive.arcadeDrive(0, 0.7);
    }
    }
    else{
      // Once again, sad and lonely drive code.
      _diffDrive.arcadeDrive(leftJoy.getRawAxis(1) * -1, rightJoy.getRawAxis(0));
    }

    /*//Manual Override !!!DO NOT EDIT!!!
    if (xboxLeftStickButton == true){
      //C shooter 
      if (xboxRightTrigger >= 0.75){
        shootMotor.set(ControlMode.PercentOutput, 1);
      }
      else {
        shootMotor.set(ControlMode.PercentOutput, 1);
      }

      //Intake
      if (xboxLeftTrigger >= 0.75){
        intakeSolenoid.set(DoubleSolenoid.Value.kForward);
        intakeMotor.set(ControlMode.PercentOutput, 1);
      }
      else{
        intakeSolenoid.set(DoubleSolenoid.Value.kReverse);
        intakeMotor.set(ControlMode.PercentOutput, 0);
      }

      //Silo
      if (xboxLeftBumper == true){
        siloMotor.set(ControlMode.PercentOutput, -0.75);
      }
      else {
        siloMotor.set(ControlMode.PercentOutput, 0);
      }
      // Climber
      if (leftMadTrigger == true){
        climberSolenoid.set(DoubleSolenoid.Value.kForward);
        if (leftMadDown == true){
          climbMotor.set(ControlMode.PercentOutput, 0.6);
        }
        else {
        }
        if (leftMadUp == true){
          climbMotor.set(ControlMode.PercentOutput, -1);
        }
      }
      else {
        climberSolenoid.set(DoubleSolenoid.Value.kReverse);
      }
    }

    //Normal Mode
    else {*/
      //C Shooter
      //Shoot 
      if (xboxRightTrigger >= 0.75){
        shootMotorActive = true;
        shootMotor.set(ControlMode.PercentOutput, 0.8);
      }
      else {
        shootMotor.set(ControlMode.PercentOutput, 0);
        shootMotorActive = false;
      }        

      //Intake
      if (xboxLeftTrigger >= 0.75){
        intakeMotor.set(ControlMode.PercentOutput, 1);
        intakeSolenoid.set(DoubleSolenoid.Value.kReverse);
      }
      else{
        intakeSolenoid.set(DoubleSolenoid.Value.kForward);
        intakeMotor.set(ControlMode.PercentOutput, 0);
      }

      /*if (xboxRightStickButton == true){
        portal = true;
        for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the RGB values for purple
        m_ledBuffer.setRGB(i, 255, 0, 255);
        }
      leds.setData(m_ledBuffer);
      }
      else{
        portal = false;
      }*/

      //if (shooter == false){
        //if (portal == false){
          //();
          //// Set the LEDs
          //leds.setData(m_ledBuffer);
        //}rainbow
      //}

      siloFull = siloEndLimit.get();
      silo = siloEnterLimit.get();

      if(xboxRightBumper){
        disableSilo = true;
        siloMotor.set(ControlMode.PercentOutput, -0.75);
      }

      if(xboxLeftBumper){
        disableSilo = true;
        siloMotor.set(ControlMode.PercentOutput, 0.75);
      }
      
      if (!disableSilo){
        if(silo && !siloFull){
          siloMotor.set(ControlMode.PercentOutput, -0.75);
          Timer.delay(0.35);
        }
        else {
          siloMotor.set(ControlMode.PercentOutput, 0);
        }
      }
      
      // Climber
      if (xboxX == true){
        climberSolenoid.set(DoubleSolenoid.Value.kForward);
        }
        else {
          climberSolenoid.set(DoubleSolenoid.Value.kReverse);
        }
        if (xboxLeftStick > 0.25){
          climbMotor.set(ControlMode.PercentOutput, -1);
        }
        else if (xboxLeftStick < -0.25){
          climbMotor.set(ControlMode.PercentOutput, 1);
        }
        else {
          climbMotor.set(ControlMode.PercentOutput, 0);
        }
      }
  }

