// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest.FieldCentricFacingAngle;
import com.ctre.phoenix6.swerve.SwerveRequest;
import com.pathplanner.lib.commands.PathPlannerAuto;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;

import frc.robot.generated.TunerConstants;
import frc.robot.subsystem.CommandSwerveDrivetrain;
import frc.robot.subsystem.Elevator;
import frc.robot.subsystem.EndEffector;
import frc.robot.subsystem.SystemLights;
import frc.robot.subsystem.EndEffector.AlgaeServoPosition;
import frc.robot.subsystem.EndEffector.HeadPosition;

public class RobotContainer {
    // kSpeedAt12Volts desired top speed
    private double MaxSpeed = TunerConstants.kSpeedAt12Volts.in(MetersPerSecond);

    // 3/4 of a rotation per second, max angular velocity
    private double MaxAngularRate = RotationsPerSecond.of(0.75).in(RadiansPerSecond);

    private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
            .withDeadband(MaxSpeed * 0.1).withRotationalDeadband(MaxAngularRate * 0.1)
            .withDriveRequestType(DriveRequestType.OpenLoopVoltage);

    private final SwerveRequest.FieldCentricFacingAngle driveFacingAngle = new FieldCentricFacingAngle()
            .withDeadband(MaxSpeed * 0.1)
            .withDriveRequestType(DriveRequestType.OpenLoopVoltage);

    private final Telemetry logger = new Telemetry(MaxSpeed);
    private final CommandXboxController joystick = new CommandXboxController(0);
    private final SendableChooser<Command> autoChooser = new SendableChooser<>();

    public final EndEffector endEffector = new EndEffector();
    public final Elevator elevator = new Elevator();
    public final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();
    public final SystemLights systemLights = new SystemLights();
    private final ControlFactory controlFactory = new ControlFactory(drivetrain, elevator, endEffector, systemLights);

    private final Trigger hasAlgae = new Trigger(() -> endEffector.hasAlgae());
    private final Trigger hasCoral = new Trigger(() -> endEffector.hasCoral());
    private final Trigger reefHasHighAlgae = new Trigger(() -> controlFactory.hasHighAlgae());
    private final Trigger reefHasLowAlgae = new Trigger(() -> controlFactory.hasLowAlgae());
    private final Trigger isNearCoralPosition = new Trigger(() -> elevator.isNearCoralScoringPosition());
    private final Trigger isNearAlgaePosition = new Trigger(() -> elevator.isNearReefAlgaePosition());
    private final Trigger isNearAlgaeContainmentPositon = new Trigger(() -> elevator.isNearAlgaeContainmentPosition());

    public RobotContainer() {
        configureBindings();
        configureSmartDashboardBindings();
        configureAutoCommands();
        drivetrain.registerTelemetry(logger::telemeterize);
    }

    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
    }

    private void configureBindings() {
           /* 
        drivetrain.setDefaultCommand(
                drivetrain.applyRequest(() -> drive
                        .withVelocityX(-joystick.getLeftY() * MaxSpeed)
                        .withVelocityY(-joystick.getLeftX() * MaxSpeed)
                        .withRotationalRate(-joystick.getRightX() * MaxAngularRate)));

        joystick.rightBumper().onTrue(endEffector.cmdSetHeadRotation(EndEffector.HeadPosition.CENTER_RIGHT));
        joystick.leftBumper().onTrue(endEffector.cmdSetHeadRotation(EndEffector.HeadPosition.CENTER_RIGHT));

        joystick.rightTrigger().and(isNearCoralPosition).onTrue(endEffector.cmdAddCoralRotations(12));
        joystick.rightTrigger().and(isNearAlgaePosition).whileTrue(endEffector.cmdSetAlgaeDutyCycleOut(-0.5));
        joystick.rightTrigger().and(isNearAlgaeContainmentPositon).whileTrue(endEffector.cmdSetAlgaeDutyCycleOut(0.5));

     
        joystick.leftTrigger().and(reefHasHighAlgae).onTrue(controlFactory.dealgaeReefHighPosition());
        joystick.leftTrigger().and(reefHasLowAlgae).onTrue(controlFactory.delagaeReefLowPosition());

        joystick.povDown().onTrue(elevator.cmdSetPosition(Elevator.Position.L1));
        joystick.povRight().onTrue(elevator.cmdSetPosition(Elevator.Position.L2));
        joystick.povLeft().onTrue(elevator.cmdSetPosition(Elevator.Position.L3));
        joystick.povUp().onTrue(elevator.cmdSetPosition(Elevator.Position.L4));

        joystick.b().and(hasAlgae).negate().onTrue(controlFactory.startingConfiguration());
        joystick.b().and(hasAlgae).onTrue(controlFactory.algaeContainment());

        joystick.start().onTrue(drivetrain.runOnce(() -> drivetrain.seedFieldCentric()));
        joystick.back().toggleOnTrue(drivetrain.applyRequest(
                () -> driveFacingAngle
                        .withVelocityX(-joystick.getLeftY() * MaxSpeed)
                        .withVelocityY(-joystick.getLeftX() * MaxSpeed)
                        .withTargetDirection(controlFactory.determineHeadingToReef())));

 */
        //joystick.rightTrigger().onTrue(endEffector.cmdAddCoralRotations(30));
        joystick.rightTrigger().whileTrue(endEffector.cmdSetAlgaeDutyCycleOut(0.2));
        //hasCoral.negate().onTrue(endEffector.cmdSetHeadRotation(EndEffector.HeadPosition.CENTER));
        hasCoral.onTrue(endEffector.cmdAddCoralRotations(5).andThen(endEffector.cmdSetHeadRotation(EndEffector.HeadPosition.LEFT)));
        joystick.x().whileTrue(elevator.cmdSetDutyCycleOut(0.2));
        joystick.y().whileTrue(elevator.cmdSetDutyCycleOut(-0.2));
    }

    private void configureSmartDashboardBindings() {
        // Sys ID for swerve
        SmartDashboard.putData(drivetrain.sysIdDynamic(Direction.kForward).withName("Dynamic forward"));
        SmartDashboard.putData(drivetrain.sysIdDynamic(Direction.kReverse).withName("Dynamic reverse"));
        SmartDashboard.putData(drivetrain.sysIdQuasistatic(Direction.kForward).withName("Quasistatic forward"));
        SmartDashboard.putData(drivetrain.sysIdQuasistatic(Direction.kReverse).withName("Quasistatic reverse"));

        // Servo replacement commands
        SmartDashboard.putData(endEffector.cmdBumpHead(true).withName("Head, bump right"));
        SmartDashboard.putData(endEffector.cmdBumpHead(false).withName("Head, bump left"));
        SmartDashboard.putData(endEffector.cmdJumpHead(true).withName("Head, jump right"));
        SmartDashboard.putData(endEffector.cmdJumpHead(false).withName("Head, jump left"));
        SmartDashboard.putData(endEffector.cmdSetHeadRotation(HeadPosition.LOGICAL_CENTER).withName("Head, center"));

        SmartDashboard.putData(endEffector.cmdBumpAlgaeIntake(true).withName("Algae intake, bump out"));
        SmartDashboard.putData(endEffector.cmdBumpAlgaeIntake(false).withName("Algae intake, bump in"));
        SmartDashboard.putData(endEffector.cmdJumpAlgaeIntake(true).withName("Algae intake, jump out"));
        SmartDashboard.putData(endEffector.cmdJumpAlgaeIntake(false).withName("Algae intake, jump in"));
        SmartDashboard.putData(endEffector.cmdSetAlgaeIntakePostion(AlgaeServoPosition.MIDDLE).withName("Algae intake, middle"));
    }

    private void addPathAuto(String name, String pathName) {
        try {
            autoChooser.addOption(name, new PathPlannerAuto(pathName));
        } catch (Exception e) {
            DataLogManager.log(String.format("GatorBot: Not able to build auto routines! %s", e.getMessage()));
        }
    }

    private void configureAutoCommands() {
        /*
         * Do nothing as default is a human safety condition, this should always be the
         * default
         */
        autoChooser.setDefaultOption("Do nothing", new WaitCommand(15));
        addPathAuto("EasyAuto", "EasyAuto");
        SmartDashboard.putData("Auto Chooser", autoChooser);
    }

}
