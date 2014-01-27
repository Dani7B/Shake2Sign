function accelerationVector = filtering( accelerationVector )

%FILTERING Summary of this function goes here

    
    DatiLen = length(accelerationVector(:,1));
    media = mean(accelerationVector);
    varAcc = var(accelerationVector);
    % intervallo valore atteso + o - la varianza
    % limite superiore della finestra
    A = media + varAcc;
    % limite inferiore della finestra
    B = media - varAcc;
    % filtering
    for i=1:DatiLen 
                % X
               if(accelerationVector(i,1) < A(1,1)) && (accelerationVector(i,1) > B(1,1))
                   accelerationVector(i,1) = 0;
               end
               % Y 
               if(accelerationVector(i,2) < A(1,2)) && (accelerationVector(i,2) > B(1,2))
                   accelerationVector(i,2) = 0;                 
               end
               % Z
               if(accelerationVector(i,3) < A(1,3)) &&(accelerationVector(i,3) > B(1,3))
                   accelerationVector(i,3) = 0;                    
               end
    end
end

