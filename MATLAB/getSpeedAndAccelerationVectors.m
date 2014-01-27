function [speedVector,accelerationVector] = getSpeedAndAccelerationVectors(aVector,speedBeforeVector,roll,pitch,yaw,delta)
% getSpeedVector takes as inputs the output of the accelerometer, the starting acceleration vector, the conversion matrix and the angles
% derived from the gyroscope. It outputs the speedVector
    dcm = angle2dcm(roll,pitch,yaw,'XYZ');
    accelerationVector = dcm * aVector';
    speedVector = speedBeforeVector' + (accelerationVector * delta);
end
