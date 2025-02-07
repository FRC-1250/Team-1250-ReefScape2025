// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystem.CommandSwerveDrivetrain;
import frc.robot.subsystem.Elevator;
import frc.robot.subsystem.EndEffector;
import frc.robot.subsystem.SystemLights;
import frc.robot.subsystem.Elevator.Position;
import frc.robot.subsystem.EndEffector.AlgaeServoPosition;
import frc.robot.subsystem.EndEffector.HeadPosition;

/** Add your docs here. */
public class ControlFactory {

    private final CommandSwerveDrivetrain swerveDrivetrain;
    private final Elevator elevator;
    private final EndEffector endEffector;
    private final SystemLights systemLights;
    private final Translation2d blueReef = new Translation2d(4.490, 4);
    private final Translation2d redReef = new Translation2d(13.05, 4);
    private final double[] highAlgaeAprilTags = { 6, 8, 10, 17, 19, 21 };
    private final double[] lowAlgaeAprilTags = { 7, 9, 11, 18, 20, 22 };
    private boolean previousHasCoralState = false;

    public ControlFactory(CommandSwerveDrivetrain swerveDrivetrain, Elevator elevator, EndEffector endEffector,
            SystemLights systemLights) {
        this.swerveDrivetrain = swerveDrivetrain;
        this.elevator = elevator;
        this.endEffector = endEffector;
        this.systemLights = systemLights;
    }

    public Command home() {
        List<Command> commands = new ArrayList<>();
        commands.add(endEffector.cmdStopAlgaeMotor());
        commands.add(endEffector.cmdStopCoralMotor());
        commands.add(endEffector.cmdSetAlgaeIntakePostion(EndEffector.AlgaeServoPosition.HOME));

        if (endEffector.hasAlgae()) {
            commands.add(elevator.cmdSetPosition(Elevator.Position.CONTAIN_ALGAE));
        } else {
            commands.add(elevator.cmdSetPosition(Elevator.Position.HOME));
        }
        return Commands.sequence(commands.toArray(Command[]::new));
    }

    public Command headIdle() {
        Command command;
        boolean hasCoral = endEffector.hasCoral();
        boolean isAtHome = elevator.isAtHome();

        if (hasCoral && isAtHome) {
            command = endEffector.cmdSetHeadRotation(EndEffector.HeadPosition.LEFT);
        } else if (!hasCoral && isAtHome) {
            command = endEffector.cmdSetHeadRotation(EndEffector.HeadPosition.CENTER);
        } else if (!previousHasCoralState && hasCoral) {
            command = endEffector.cmdAddCoralRotations(6);
        } else {
            command = Commands.none();
        }
        previousHasCoralState = hasCoral;
        return command;
    }

    public Command dealgaeReefPosition() {
        double id = LimelightHelpers.getFiducialID("limelight");
        if (idInArray(lowAlgaeAprilTags, id)) {
            return Commands.sequence(
                    elevator.cmdSetPosition(Elevator.Position.L3_5),
                    endEffector.cmdSetAlgaeIntakePostion(EndEffector.AlgaeServoPosition.DEPLOYED),
                    elevator.cmdSetPosition(Elevator.Position.L2_5));
        } else {
            return Commands.sequence(
                    elevator.cmdSetPosition(Elevator.Position.L4),
                    endEffector.cmdSetAlgaeIntakePostion(EndEffector.AlgaeServoPosition.DEPLOYED),
                    elevator.cmdSetPosition(Elevator.Position.L3_5));
        }
    }

    public Command handleGameObject() {
        if (elevator.isNearCoralPosition()) {
            return endEffector.cmdAddCoralRotations(12);
        } else if (elevator.isNearAlgaePosition()) {
            return endEffector.cmdSetAlgaeDutyCycleOut(0.5);
        } else if (elevator.isAtHome()) {
            return endEffector.cmdSetAlgaeDutyCycleOut(-0.5);
        } else {
            return Commands.none();
        }
    }

    public Rotation2d determineHeadingToReef() {
        if (swerveDrivetrain.getOperatorForwardDirection().getDegrees() == 0) {
            return Rotation2d.fromDegrees(
                    Math.atan2(
                            swerveDrivetrain.getState().Pose.getY() - blueReef.getY(),
                            swerveDrivetrain.getState().Pose.getX() - blueReef.getX()));
        } else {
            return Rotation2d.fromDegrees(
                    Math.atan2(
                            swerveDrivetrain.getState().Pose.getY() - redReef.getY(),
                            swerveDrivetrain.getState().Pose.getX() - redReef.getX()));
        }
    }

    private boolean idInArray(double[] arr, double id) {
        for (double i : arr) {
            if (Double.compare(i, id) == 0)
                return true;
        }
        return false;
    }

}
