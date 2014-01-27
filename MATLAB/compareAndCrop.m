function [c, d] = compareAndCrop(a, b)

    la = length(a);
    lb = length(b);
    if(la == lb)
        c = a;
        d = b;
    elseif(la<lb)
        c = a;
        d = b(1:3,1:la);
    else
        d = b;
        c = a(1:3,1:lb);
    end
end

