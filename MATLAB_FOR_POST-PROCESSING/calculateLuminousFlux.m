%luminousFlux = calculateLuminousFlux(M)
%   Calculates the luminous flux supposing that M represents a luminous 
%   intensity distribution.
%
%   M must be organized in the following manner:
%       First column: Polar angles
%       Second column: Azimuth angles
%       Third column: Luminous intensities
function luminousFlux = calculateLuminousFlux(M)
theta = degtorad(M(:,1));
phi = degtorad(M(:,2));
lumints = M(:,3);

%Unique theta and phi
Utheta = unique(theta,'stable');
Uphi = unique(phi,'stable');
thetaRes = Utheta(2) - Utheta(1);
phiRes = Uphi(3) - Uphi(2);

nMeasuresSemiSphere = ...
    (360 / radtodeg(phiRes)) * (90 / radtodeg(thetaRes)) + 1;

%%% --- LUMINOUS INTENSITY --- %%%

%%%areaElement=[cos(thetaLow)-cos(ThetaHigh)]*resPhi
%Shifted Unique theta
% SUtheta = circshift(Utheta,length(Utheta)-1);
area = phiRes*(cos(Utheta-thetaRes/2) - cos(Utheta+thetaRes/2));
area(1)=2*pi*(cos(0)-cos(thetaRes/2));
if length(lumints) <= nMeasuresSemiSphere
    lastTheta=Utheta(end);
    area(end)=phiRes*(cos(lastTheta-thetaRes/2)-cos(degtorad(90)));
else
    area(end)=area(1);
end
% clear lastTheta;

ringFlux = zeros(length(area),1);
for i=1:length(Utheta)
    foundValues = find(theta==Utheta(i));
    ringFlux(i) = sum(lumints(foundValues'));    
end
ringFlux = ringFlux.*area;

luminousFlux = sum(ringFlux);
end