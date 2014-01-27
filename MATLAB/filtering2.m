function accelerationVector = filtering2( accelerationVector )

%FILTERING Summary of this function goes here

% Low pass filtering of the signal is a very good way to remove noise
% (both mechanical and electrical) from the accelerometer.
% Reducing the noise is critical for a positioning application in order
% to reduce major errors when integrating the signal.
% A simple way for low pass filtering a sampled signal is to perform a rolling
% average. Filtering is simply then reduced to obtain the average of a set of
% samples. It is important to obtain the average of a balanced amount of samples. 
% Taking too many samples to do this process can result in a loss of data, yet
% taking too few can result in an inaccurate value.

    DatiLen = length(accelerationVector(:,1));
    
    samples = zeros(3,50);   %% 50 samples

       for i=1:DatiLen
            % prendo 10 campioni random
           samples(:,i) = accelerationVector(randi([1, DatiLen]),:);
       
       end
       
    %Modulo segnale picco picco massimo
%     absX = abs(max(samples(:,1))) + abs(min(samples(:,1)));
%     absY = abs(max(samples(:,2))) + abs(min(samples(:,2)));
%     absZ = abs(max(samples(:,3))) + abs(min(samples(:,3)));

    %alzo del modulo massimo
%     AccX = samples(:,1) + absX; 
%     AccY = samples(:,2) + absY;
%     AccZ = samples(:,2) + absZ;
    
    media = mean(samples);


    % intervallo valore atteso + o - la varianza
    
%     % limite superiore della finestra
%     Ax = mediaX - absX;
%     Ay = mediaY - absY;
%     Az = mediaZ - absZ;
% 
%     % limite inferiore della finestra
%     
%     Bx = varAcc;
    
    % filtering
    
    for i=1:DatiLen 
                % X
               if(accelerationVector(i,1) < abs(media(1,1)))
                   accelerationVector(i,1) = 0;
               end
               % Y 
               if(accelerationVector(i,1) <= abs(media(1,2)))
                   accelerationVector(i,2) = 0;                 
               end
               % Z
               if(accelerationVector(i,3) <= abs(media(1,3)))
                   accelerationVector(i,3) = 0;                 
               end
    end
    
    
    
end

