%closest = closest2XinY(X,Y)
%   Returns the value closest to X among those contained in Y.
function closest = closest2XinY(X,Y)
    Z=abs(X-Y);
    [~,indx]=min(Z);
    closest=Y(indx);
end