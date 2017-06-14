%M = adaptIncompleteMeasure(I)
%   Adapts an incomplete measure matrix so it can still be plotted.
function M = adaptIncompleteMeasure(I)

polarAngle = I(:,1);
azimuthAngle = I(:,2);

Uazimuth = unique(azimuthAngle,'stable');

azimuthRes = Uazimuth(2)-Uazimuth(1);

lastPolar = polarAngle(end);
lastAzimuth = 360-azimuthRes;

if(lastPolar ~= 180)
    if lastAzimuth~=azimuthAngle(end)
        addAzimuths = azimuthAngle(end)+azimuthRes:azimuthRes:lastAzimuth;
        addAzimuths = addAzimuths';
        addPolars = ones(length(addAzimuths),1)*polarAngle(end);
        addLumints = zeros(size(addAzimuths));

        polars = [I(:,1);addPolars];
        azimuths = [I(:,2);addAzimuths];
        lumints = [I(:,3);addLumints];

        R=[polars azimuths lumints];
    else
        R=I;
    end
else
    R=I;
end

M=R;