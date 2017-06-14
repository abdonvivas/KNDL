%fixedAnglePolarPlot(M,fixedPolarAngle,fixedAzimuthAngle)
%   Generates polar plots of matrix M for a fixed polar angle (from 0º to 
%   180º) and a fixed azimuth angle (from 0º to 180º) for the first half of
%   the plot and the ame angle + 180º for the second half of the plot.
%   
%   M must be organized in the following manner:
%       First column: Polar angles
%       Second column: Azimuth angles
%       Third column: Radial coordinates
function fixedAnglePolarPlot(M,fixedPolarAngle,fixedAzimuthAngle)

theta = M(:,1);
phi = M(:,2);
lumints = M(:,3);

%Ensure rounding to their closest values
Z=abs(fixedPolarAngle-theta);
[~,indx]=min(Z);
fixedPolarAngle=theta(indx);
Z=abs(fixedAzimuthAngle-phi);
[~,indx]=min(Z);
fixedAzimuthAngle=phi(indx);

%%%--- FIXED THETA POLAR PLOT ---%%%
figure;
indx=find(theta==fixedPolarAngle);
%TO CLOSE THE POLAR PLOT
graphPhi=[phi(indx);360];
graphLumints=[lumints(indx);lumints(indx(1))];
%TO CLOSE THE POLAR PLOT
subplot(1,2,1);
polarplot(degtorad(graphPhi),graphLumints);
title(['Polar angle = ' num2str(fixedPolarAngle)]);
thetaticks(0:10:350);
% rticks(0:5:max(graphLumints)+5);
%%%--- FIXED THETA POLAR PLOT ---%%%

%%%--- FIXED PHI POLAR PLOT ---%%%
indx=find(phi(2:end)==fixedAzimuthAngle);
indx2=find(phi(2:end)==fixedAzimuthAngle+180);

indx180=find(theta==180);
if isempty(indx180)
    val180=0;
else
   val180=lumints(indx180);
end

thetaTMP=theta(2:end);
lumintsTMP=lumints(2:end);

graphTheta=[180;flipud(thetaTMP(indx));0;thetaTMP(indx2)*-1;-180];
graphLumints=[val180;flipud(lumintsTMP(indx));...
    lumints(1);lumintsTMP(indx2);val180];

subplot(1,2,2);
polarplot(degtorad(graphTheta),graphLumints);
title(['Azimuth angles = ' num2str(fixedAzimuthAngle)...
    ' and ' num2str(fixedAzimuthAngle+180)]);
thetaticks(0:10:350);
thetaticklabels({'0','10','20','30','40','50','60','70','80','90','100',...
    '110','120','130','140','150','160','170','180','170','160','150',...
    '140','130','120','110','100','90','80','70','60','50','40','30',...
    '20','10'})

ax=gca;
ax.RAxisLocation=280;
ax.ThetaZeroLocation='top';
%%%--- FIXED PHI POLAR PLOT ---%%%

end