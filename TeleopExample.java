// Copyright (c) 2017 FTC Team 10262 Pioπeers

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * ArcadeDrive Mode
 * <p>
 */
@TeleOp(name="TeleopExample", group="Teleop")
public class TeleopExample extends BaseExample {

    public TeleopExample() {}

    @Override
    public void loop() {
      // Note we are using the triggers and not a stick for foward/reverse
      // easy change of the first parameter if you would rather use a stick
        arcadeDrive(
                gamepad1.right_trigger + gamepad1.left_trigger * -1,
                 -gamepad1.left_stick_x,
                true);

        /* If you prefer tank drive try */
        // set_drive_power(-gamepad1.left_stick_y, -gamepad1.right_stick_y); 

        // To keep code clean we put each "subsystem" into its own
        // loop method, seems silly here, but pays off if you have a lot 
        // of code
        jewel_arm_loop();
    }

    // auton should always return the color arm, but if it gets stuck
    // or we want to use the arm to help during teleop, we provide a 
    // manual method to control it
    private void jewel_arm_loop() {
        if (gamepad2.back) {
            jewel_arm.setPosition(CalibrationExample.JEWEL_ARM_DEPLOYED);
        } else {
            jewel_arm.setPosition(CalibrationExample.JEWEL_ARM_RETRACTED);
        }
    }

}
