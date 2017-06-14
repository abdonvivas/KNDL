clear all; close all;

M = readCSVfile();
M = adaptIncompleteMeasure(M);

threeDpolarplot(M);

luminousFlux = calculateLuminousFlux(M);
disp(['Luminous Flux = ' num2str(luminousFlux)]);

[HFWHM,VFWHM] = calculateHVFWHM(M);
disp(['Horizontal FWHM = ' num2str(HFWHM)]);
disp(['Vertical FWHM = ' num2str(VFWHM)]);

fixedAnglePolarPlot(M,HFWHM/2,0);

