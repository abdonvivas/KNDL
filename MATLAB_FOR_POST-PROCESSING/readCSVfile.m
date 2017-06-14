%M = readCSVfile()
%   Opens a folder selection dialog box to select a csv file and imports it as
%   a matrix.
function M = readCSVfile()
[FileName,PathName] = uigetfile({'*.csv;*.CSV','CSV Files (*.csv,*.CSV)'});

if isequal(FileName,0)
    error('Operation canceled by the user.');
else
    M = csvread(fullfile(PathName, FileName));
end
end