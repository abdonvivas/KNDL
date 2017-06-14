%threeDpolarplot(M)
%   Makes a 3D polar plot of the matrix M, which must be organized in the
%   following manner:
%       First column: Polar angles
%       Second column: Azimuth angles
%       Third column: Radial coordinates
function threeDpolarplot(M)
%%%--- FORMAT ADAPTING ---%%%
%Calculate elevation angle
M(:,1)=90-M(:,1);

%Swap coordinates (phi=PolarAngle, theta=AzimuthAngle)
M(:,[1,2])=M(:,[2,1]);

M(:,3)=M(:,3)/max(M(:,3));

%Resolution
Utheta=unique(M(:,1),'stable');
Uphi=unique(M(:,2),'stable');
thetaRes = Utheta(2) - Utheta(1);
phiRes = Uphi(2) - Uphi(3);

%Number of measures of a full sphere
Nmax = 360 / thetaRes * ((180 / phiRes) - 1) + 2;
%Number of measures taken
N = size(M,1);
%%%--- FORMAT ADAPTING ---%%%

%%%--- MESHGRID COMPUTATION ---%%%
Utheta=degtorad(Utheta);
Uphi=degtorad(Uphi);
[theta0,~]=meshgrid(Utheta,Uphi);
%%%--- MESHGRID COMPUTATION ---%%%

%%%--- RHO ADAPTING TO MESHGRID ---%%%
rho0 = zeros(1,numel(theta0));

%Generate the values for (theta,lastPhi) as the same values for (0,lastPhi) 
% rho0(1:numel(theta0)-N+1)=M(1,3);
rho0(1:length(Utheta))=M(1,3);

% rho0(numel(theta0)-N+2:numel(theta0))=M(2:N,3);
if N<Nmax
    rho0(length(Utheta)+1:numel(theta0))=M(2:N,3);
else
    rho0(length(Utheta)+1:numel(theta0)-length(Utheta))=M(2:N-1,3);
    rho0(numel(theta0)-length(Utheta)+1:numel(theta0))=M(end,3);
end

rho0=reshape(rho0,[size(theta0,2),size(theta0,1)]);
rho0=rho0';
%%%--- RHO ADAPTING TO MESHGRID ---%%%

%%%--- ADD 2*PI TO AZIMUTH TO CLOSE THE PLOT ---%%%
%Add 2*PI to azimuth (REPLICATE 0,0). C=Complete
theta2pi=[Utheta;2*pi];
[theta,phi]=meshgrid(theta2pi,Uphi);
rho=[rho0,rho0(:,1)];
%%%--- ADD 2*PI TO AZIMUTH TO CLOSE THE PLOT ---%%%

%%%--- SPHERICAL TO CARTESIAN ---%%%
[X,Y,Z] = sph2cart(theta,phi,rho);
%%%--- SPHERICAL TO CARTESIAN ---%%%

%%%--- SURFACE PLOT ---%%%
figure;
% surf(X,Y,Z);
mesh(X,Y,Z);
maxlim=max([max(abs(xlim)),max(abs(ylim)),max(abs(zlim))]);
xlim([-maxlim,maxlim]);
ylim([-maxlim,maxlim]);
zlim([-maxlim,maxlim]);
xticks(-maxlim:maxlim/10:maxlim);
yticks(-maxlim:maxlim/10:maxlim);
zticks(-maxlim:maxlim/10:maxlim);
set(gca,'xticklabel',{[]},'yticklabel',{[]},'zticklabel',{[]});
pbaspect([1 1 1]);
title('Luminous intensity distribution');
%%%--- SURFACE PLOT ---%%%
end