%[VFWHM,HFWHM] = calculateHVFWHM(M)
%   Calculates the luminous horizontal and vertical Full Width at Half 
%   Maximum (FWHM) supposing that M represents a luminous intensity 
%   distribution.
%
%   M must be organized in the following manner:
%       First column: Polar angles
%       Second column: Azimuth angles
%       Third column: Luminous intensities
function [HFWHM,VFWHM] = calculateHVFWHM(M)

theta = M(:,1);
phi = M(:,2);
lumints = M(:,3);

norm_lumints = lumints / max(lumints);

indx180=find(theta==180);

%%% --- HORIZONTAL FWHM --- %%%
horizontalCut = find(phi==0);
horizontalCut = [horizontalCut;indx180];
norm_slice = norm_lumints(horizontalCut');
theta_slice = theta(horizontalCut');
diff = abs(0.5-norm_slice);
[hhwhmVal,~] = min(diff);
hhwhmIndx = find(diff==hhwhmVal, 1, 'last');

horizontalCut2 = find(phi==180);
horizontalCut2 = [horizontalCut2;indx180];
norm_slice2 = norm_lumints(horizontalCut2');
theta_slice2 = theta(horizontalCut2');
diff2 = abs(0.5-norm_slice2);
[hhwhmVal2,~] = min(diff2);
hhwhmIndx2 = find(diff2==hhwhmVal2, 1, 'last');

HFWHM=theta_slice(hhwhmIndx)+theta_slice2(hhwhmIndx2);
%%% --- HORIZONTAL FWHM --- %%%

%%% --- VERTICAL FWHM --- %%%
verticalCut = find(phi==90);
verticalCut = [verticalCut;indx180];
norm_slice = norm_lumints(verticalCut');
theta_slice = theta(verticalCut');
diff = abs(0.5-norm_slice);
[vhwhmVal,~] = min(diff);
vhwhmIndx = find(diff==vhwhmVal, 1, 'last');

verticalCut2 = find(phi==270);
verticalCut2 = [verticalCut2;indx180];
norm_slice2 = norm_lumints(verticalCut2');
theta_slice2 = theta(verticalCut2');
diff2 = abs(0.5-norm_slice2);
[vhwhmVal2,~] = min(diff2);
vhwhmIndx2 = find(diff2==vhwhmVal2, 1, 'last');

VFWHM=theta_slice(vhwhmIndx)+theta_slice2(vhwhmIndx2);
%%% --- VERTICAL FWHM --- %%%
end