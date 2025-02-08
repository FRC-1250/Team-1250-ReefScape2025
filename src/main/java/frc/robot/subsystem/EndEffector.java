// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystem;

import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

@Logged
public class EndEffector extends SubsystemBase {

  public enum AlgaeServoPosition {
    DEPLOYED(1),
    MIDDLE(0.5),
    HOME(0);

    public final double value;

    AlgaeServoPosition(double value) {
      this.value = value;
    }
  }

  public enum ServoOffset {
    BUMP(0.01),
    JUMP(0.1);

    public final double value;

    ServoOffset(double value) {
      this.value = value;
    }
  }

  public static class HeadPosition {
    private final static double TURE_CENTER = 0.4575;
    private final static double CORAL_BRANCH_SERVO_OFFSET = 0.15;
    private final static double NINTY_DEGREE_OFFSET = 0.35;

    public final static double LOGICAL_CENTER = 0.5;
    public final static double CENTER = TURE_CENTER;
    public final static double CENTER_LEFT = TURE_CENTER - CORAL_BRANCH_SERVO_OFFSET;
    public final static double LEFT = TURE_CENTER - NINTY_DEGREE_OFFSET;
    public final static double CENTER_RIGHT = TURE_CENTER + CORAL_BRANCH_SERVO_OFFSET;
    public final static double RIGHT = TURE_CENTER + NINTY_DEGREE_OFFSET;
  }

  private TalonFX algaeMotor = new TalonFX(21);
  private DutyCycleOut algaeDutyCycleOut = new DutyCycleOut(0);
  private DigitalInput algaeSensor = new DigitalInput(4);
  private Servo algaeIntakePosition = new Servo(1);

  private TalonFX coralMotor = new TalonFX(20);
  private DutyCycleOut coralDutyCycleOut = new DutyCycleOut(0);
  private PositionVoltage coralPositionControl = new PositionVoltage(0);
  private DigitalInput coralSensor = new DigitalInput(3);
  private Servo headRotate = new Servo(0);
  private double currentPosition = 0;

  /** Creates a new EndEffector. */
  public EndEffector() {
    Slot0Configs positionPIDConfigs = new Slot0Configs()
        .withKG(0)
        .withKS(0)
        .withKP(1)
        .withKI(0)
        .withKD(0)
        .withKV(0);

    TalonFXConfiguration coralTalonConfiguration = new TalonFXConfiguration();
    coralTalonConfiguration.Slot0 = positionPIDConfigs;
    coralTalonConfiguration.CurrentLimits.SupplyCurrentLimit = 25;
    coralTalonConfiguration.CurrentLimits.SupplyCurrentLimitEnable = true;
    coralTalonConfiguration.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    coralTalonConfiguration.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;
    coralMotor.getConfigurator().apply(coralTalonConfiguration);

    TalonFXConfiguration algaeTalonConfiguration = new TalonFXConfiguration();
    algaeTalonConfiguration.CurrentLimits.SupplyCurrentLimit = 25;
    algaeTalonConfiguration.CurrentLimits.SupplyCurrentLimitEnable = true;
    algaeTalonConfiguration.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    algaeTalonConfiguration.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;
    algaeMotor.getConfigurator().apply(algaeTalonConfiguration);

    headRotate.setBoundsMicroseconds(2500, 1500, 1500, 1500, 500);
    algaeIntakePosition.setBoundsMicroseconds(2500, 1500, 1500, 1500, 500);
  }

  @Override
  public void periodic() {
   SmartDashboard.putNumber("Head, position", headRotate.getPosition());
   SmartDashboard.putNumber("Algae intake, position", headRotate.getPosition());
  }

  public Command cmdStopCoralMotor() {
    return Commands.runOnce(() -> stopCoralMotor(), this);
  }

  public Command cmdSetCoralPosition(double position) {
    return new FunctionalCommand(
        () -> {
        },
        () -> setCoralPosition(position),
        interrupted -> stopCoralMotor(),
        () -> isCoralNearPosition(position),
        this);
  }

  public Command cmdAddCoralRotations(double rotations) {
    return new FunctionalCommand(
        () -> currentPosition = getCoralPosition(),
        () -> setCoralPosition(currentPosition + rotations),
        interrupted -> stopCoralMotor(),
        () -> isCoralNearPosition(currentPosition + rotations),
        this);
  }

  public Command cmdSetCoralDutyCycleOut(double output) {
    return Commands.runEnd(
        () -> setCoralDutyCycleOut(output),
        () -> stopCoralMotor(),
        this);
  }

  public Command cmdSetHeadRotation(double value) {
    return Commands.runOnce(() -> setHeadPosition(value), this);
  }

  public Command cmdBumpHead(boolean moveRight) {
    if(moveRight) {
      return Commands.runOnce(() -> setHeadPosition(headRotate.getPosition() + ServoOffset.BUMP.value), this);
    } else {
      return Commands.runOnce(() -> setHeadPosition(headRotate.getPosition() - ServoOffset.BUMP.value), this);
    }
  }

  public Command cmdJumpHead(boolean moveRight) {
    if(moveRight) {
      return Commands.runOnce(() -> setHeadPosition(headRotate.getPosition() + ServoOffset.JUMP.value), this);
    } else {
      return Commands.runOnce(() -> setHeadPosition(headRotate.getPosition() - ServoOffset.JUMP.value), this);
    }
  }

  public Command cmdStopAlgaeMotor() {
    return Commands.runOnce(() -> algaeMotor.stopMotor(), this);
  }

  public Command cmdSetAlgaeDutyCycleOut(double output) {
    return Commands.runEnd(
        () -> setAlgaeDutyCycleOut(output),
        () -> stopAlgaeMotor(),
        this);
  }

  public Command cmdSetAlgaeIntakePostion(AlgaeServoPosition value) {
    return Commands.runOnce(() -> setAlgaeIntakePostion(value), this);
  }

  public Command cmdBumpAlgaeIntake(boolean moveRight) {
    if(moveRight) {
      return Commands.runOnce(() -> setAlgaeIntakePostion(headRotate.getPosition() + ServoOffset.BUMP.value), this);
    } else {
      return Commands.runOnce(() -> setAlgaeIntakePostion(headRotate.getPosition() - ServoOffset.BUMP.value), this);
    }
  }

  public Command cmdJumpAlgaeIntake(boolean moveRight) {
    if(moveRight) {
      return Commands.runOnce(() -> setAlgaeIntakePostion(headRotate.getPosition() + ServoOffset.JUMP.value), this);
    } else {
      return Commands.runOnce(() -> setAlgaeIntakePostion(headRotate.getPosition() - ServoOffset.JUMP.value), this);
    }
  }

  public boolean hasCoral() {
    return !coralSensor.get();
  }

  private void stopCoralMotor() {
    coralMotor.stopMotor();
  }

  private void setCoralDutyCycleOut(double output) {
    coralMotor.setControl(coralDutyCycleOut.withOutput(output));
  }

  private void setCoralPosition(double position) {
    coralMotor.setControl(coralPositionControl.withPosition(position));
  }

  private double getCoralPosition() {
    return coralMotor.getPosition().getValueAsDouble();
  }

  private boolean isCoralNearPosition(double position) {
    return MathUtil.isNear(position, getCoralPosition(), 1);
  }

  private void setHeadPosition(double position) {
    headRotate.setPosition(position);
  }

  public boolean hasAlgae() {
    return !algaeSensor.get();
  }

  private void stopAlgaeMotor() {
    algaeMotor.stopMotor();
  }

  private void setAlgaeDutyCycleOut(double output) {
    algaeMotor.setControl(algaeDutyCycleOut.withOutput(output));
  }

  private void setAlgaeIntakePostion(double position) {
    algaeIntakePosition.setPosition(position);
  }

  private void setAlgaeIntakePostion(AlgaeServoPosition position) {
    setAlgaeIntakePostion(position.value);
  }

}
