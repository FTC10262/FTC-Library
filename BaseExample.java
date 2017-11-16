package org.firstinspires.ftc.teamcode;

import android.content.Context;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Base code common to all other opmodes
 */
@Disabled()
public class BaseExample extends OpMode {
    private static Context appContext;
    protected MenuController menu_controller = null;

    // Make private to ensure that our set_power routine
    // is used and power ramping is implemented
    private static DcMotor left_drive = null;
    private static DcMotor right_drive = null;

    protected static Servo jewel_arm = null;
    
    // Put the rest of your hardware objects here
    // make them protected to share directly or
    // make them private and provide access methods

    /**
     * Constructor
     */
    public BaseExample() {
        // most if not all of your setup code
        // belongs in init, not here (see below)
    }

     /*
      * Code to run when the op mode is initialized goes here
	  */
    @Override
    public void init() {
        appContext = hardwareMap.appContext;
        CalibrationExample calibration = new CalibrationExample();
        calibration.readFromFile();

        left_drive = hardwareMap.dcMotor.get("left drive");
        right_drive = hardwareMap.dcMotor.get("right drive");

        jewel_arm = hardwareMap.servo.get("jewel arm");

        // Initialze your additional hardware objects here

        menu_controller = new MenuController(calibration);
    }

    @Override
    public void init_loop() {
        menu_controller.loop(telemetry, gamepad1);

        // Generally this code would have gone into init,
        // But we want the change to take place if adjusted
        // using the menu controller, so it has to be in 
        // loop
        DcMotor.ZeroPowerBehavior drive_mode = DcMotor.ZeroPowerBehavior.FLOAT;
        if (CalibrationExample.LOCK_DRIVE_WHEELS) {
            drive_mode = DcMotor.ZeroPowerBehavior.BRAKE;
        }
        left_drive.setZeroPowerBehavior(drive_mode);
        right_drive.setZeroPowerBehavior(drive_mode);

        jewel_arm.setPosition(CalibrationExample.JEWEL_ARM_RETRACTED);
    }

    @Override
    public void start() {
        telemetry.clearAll(); // cleanup any noise from the menu
    }

    @Override
    public void loop() {
        // do nothing
    }

    static Context getContext() {
        return appContext;
    }

   /*
	 * Code to run when the op mode is first disabled goes here
	 *
	 */
    @Override
    public void stop() {
        // not a bad habit to be in -- stopping the drive motors
        set_drive_power(0,0);
    }

    /**
     * Limit values to the -1.0 to +1.0 range.
     * Handy enough to keep around
     */
    protected static double limit(double num) {
        return limit(-1, 1, num);
    }

    protected static double limit(double min, double max, double num) {
        if (num > max) {
            return max;
        }
        if (num < min) {
            return min;
        }

        return num;
    }

    protected static double ramp(double oldVal, double newVal, double maxRamp) {
        double delta = newVal - oldVal;
        if (delta > maxRamp) {
            delta = maxRamp;
        } else if (delta < -maxRamp) {
            delta = -maxRamp;
        }
        return oldVal + delta;
    }

    /* 
     * This is a good example of why making your hardware
     * objects private and only exposing them to your base
     * object is handy. 
     *
     * We found that ramping our drive power made the robot
     * move smoother and was easier to control -- it is also
     * easier on the battery.
     *
     * Since everytime we set drive motor power, we use this
     * function, we only had to add the ramp logic here and
     * the effect took place everywhere else.
     * 
     */
    private double prevLeftPower = 0;
    private double prevRightPower = 0;
    private long lastSetDrivePower = 0;
    protected void set_drive_power(double left, double right) {
        if (CalibrationExample.RAMP_DRIVE_POWER) {
            final double maxChangePerMilliSecond = CalibrationExample.RAMP_DRIVE_MAX_CHANGE_PER_SECOND / 1000;
            final long ticks = System.currentTimeMillis() - lastSetDrivePower;
            final double maxRamp = Math.max(1, maxChangePerMilliSecond * ticks);

            left = ramp(prevLeftPower, left, maxRamp);
            right = ramp(prevRightPower, right, maxRamp);
            prevLeftPower = left;
            prevRightPower = right;
        }
        this.left_drive.setPower(-left);
        this.right_drive.setPower(right);
    }

    /**
     * Arcade drive implements single stick driving. This function lets you
     * directly provide joystick values from any source.
     *$
     * @param moveValue The value to use for forwards/backwards
     * @param rotateValue The value to use for the rotate right_drive/left_drive
     * @param squaredInputs If set, decreases the sensitivity at low speeds
     *
     * It was largely borrrowed from FRC WPILib
     */
    public void arcadeDrive(double moveValue, double rotateValue, boolean squaredInputs) {
        double leftMotorSpeed;
        double rightMotorSpeed;

        moveValue = limit(moveValue);
        rotateValue = limit(rotateValue);

        if (squaredInputs) {
            // square the inputs (while preserving the sign) to increase fine control
            // while permitting full power
            if (moveValue >= 0.0) {
                moveValue = (moveValue * moveValue);
            } else {
                moveValue = -(moveValue * moveValue);
            }
            if (rotateValue >= 0.0) {
                rotateValue = (rotateValue * rotateValue);
            } else {
                rotateValue = -(rotateValue * rotateValue);
            }
        }

        if (moveValue > 0.0) {
            if (rotateValue > 0.0) {
                leftMotorSpeed = moveValue - rotateValue;
                rightMotorSpeed = Math.max(moveValue, rotateValue);
            } else {
                leftMotorSpeed = Math.max(moveValue, -rotateValue);
                rightMotorSpeed = moveValue + rotateValue;
            }
        } else {
            if (rotateValue > 0.0) {
                leftMotorSpeed = -Math.max(-moveValue, rotateValue);
                rightMotorSpeed = moveValue + rotateValue;
            } else {
                leftMotorSpeed = moveValue - rotateValue;
                rightMotorSpeed = -Math.max(-moveValue, -rotateValue);
            }
        }

        set_drive_power(leftMotorSpeed, rightMotorSpeed);
    }

}
