// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystem;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.CANdle;
import com.ctre.phoenix.led.CANdle.LEDStripType;
import com.ctre.phoenix.led.CANdle.VBatOutputMode;
import com.ctre.phoenix.led.CANdleConfiguration;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SystemLights extends SubsystemBase {

  private final CANdle candle = new CANdle(30, "rio");

  public enum PresetColor {
    BLACK(0, 0, 0),
    WHITE(255, 255, 255),
    RED(255, 0, 0),
    GREEN(0, 255, 0),
    BLUE(0, 0, 255),
    PURPLE(157, 0, 255),
    PINK(255, 141, 161),
    KELLY_GREEN(76, 187, 23);

    private final int red;
    private final int green;
    private final int blue;
    private final int rgbMin = 0;
    private final int rgbMax = 255;

    PresetColor(int red, int green, int blue) {
      this.red = MathUtil.clamp(red, rgbMin, rgbMax);
      this.green = MathUtil.clamp(green, rgbMin, rgbMax);
      this.blue = MathUtil.clamp(blue, rgbMin, rgbMax);
    }
  }

  public SystemLights() {
    CANdleConfiguration configAll = new CANdleConfiguration();
    configAll.brightnessScalar = 1;
    configAll.disableWhenLOS = true;
    configAll.enableOptimizations = true;
    configAll.statusLedOffWhenActive = true;
    configAll.stripType = LEDStripType.RGB;
    configAll.v5Enabled = true;
    configAll.vBatOutputMode = VBatOutputMode.Off;
    candle.configAllSettings(configAll);
    setDefaultCommand(setLEDs(PresetColor.KELLY_GREEN));
  }

  public Command setAnimation(Animation animation) {
    return Commands.runOnce(() -> candle.animate(animation), this);
  }

  public Command setLEDs(PresetColor color) {
    return Commands.runOnce(() -> {
      candle.clearAnimation(0);
      candle.setLEDs(color.red, color.green, color.blue);
    }, this);
  }

  public Command setLEDs(int r, int g, int b) {
    return Commands.runOnce(() -> {
      candle.clearAnimation(0);
      candle.setLEDs(r, g, b);
    }, this);
  }

  public Command clear() {
    return Commands
        .runOnce(() -> candle.setLEDs(PresetColor.BLACK.red, PresetColor.BLACK.green, PresetColor.BLACK.blue), this);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
