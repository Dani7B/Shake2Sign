function results = execute(file)

    dati = xlsread(file);
    timeLine = length(dati(:,1));
    DatiLen = timeLine - 1;
    results = zeros(3,DatiLen);
    speeds = zeros(3,DatiLen);
    accelerations = zeros(3,DatiLen);      
    % Delete last row
    dati(timeLine,:) = [];  
    %%%%% OffSet z-axis %%%%%% 
    offset = calibrate();
    dati(:,3) = dati(:,3) - offset(3);
    G = fspecial('gaussian',[5 5],2);
    dati(:,3)= imfilter(dati(:,3),G,'same');
    %%%%% Filtering %%%%
    filtered = filtering([dati(:,1),dati(:,2),dati(:,3)]);  
    dati(:,1) = filtered(:,1);
    dati(:,2) = filtered(:,2);
    dati(:,3) = filtered(:,3);
    filtered2 = filtering2([dati(:,4),dati(:,5),dati(:,6)]);  
    dati(:,4) = filtered2(:,1);
    dati(:,5) = filtered2(:,2);
    dati(:,6) = filtered2(:,3);
    times = zeros(DatiLen);
    for i=1:DatiLen
        if(i==1)
            times(i) = 0;
        else
            times(i) = (dati(i,7) - dati(i-1,7))/1000;
        end
    end
    w=zeros(DatiLen);
    for i=2:DatiLen
        w(i)=w(i-1)+dati(i,6)*times(i);
    end    
    roll = 0; pitch = 0; yaw = 0;
    for i=1:DatiLen
        roll = roll + floor(dati(i,4)*10000)/10000*times(i);
        pitch = pitch + floor(dati(i,5)*10000)/10000*times(i);
        yaw = yaw + floor(dati(i,6)*10000)/10000*times(i);
        if(i==1)
            [accelerations(:,i),speeds(:,i)] = getSpeedAndAccelerationVectors([dati(i,1),dati(i,2),dati(i,3)],[0,0,0],roll,pitch,yaw,times(i));
            results(:,i) = accelerations(:,i)*times(i)*times(i)/2;
        else
            [accelerations(:,i),speeds(:,i)] = getSpeedAndAccelerationVectors([dati(i,1),dati(i,2),dati(i,3)],[speeds(1,i-1),speeds(2,i-1),speeds(3,i-1)],roll,pitch,yaw,times(i));
            results(:,i) = results(:,i-1) + speeds(:,i-1)*times(i) + accelerations(:,i)*times(i)*times(i)/2;
        end
    end
end