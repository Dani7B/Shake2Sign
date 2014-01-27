function r = calibrate()

%CALIBRATE 
    % Campioni del dispositivo da fermo
    dati = xlsread('20140119/Fermo Sun Jan 19 19_07_08 CET 2014.xlsx');
    timeLine = length(dati(:,1));
    dati(timeLine,:) = [];
    % Faccio la media dei campioni 
    r = [mean(dati(:,1)) mean(dati(:,2)) mean(dati(:,3))];         
        
end

