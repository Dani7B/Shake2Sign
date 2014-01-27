%# build a list of file names with absolute path
clear;
[selectedFile, filePath] = uigetfile({'*.xls'; '*.xlsx'},'Select the file to match');
selFileCompl = strcat(filePath, selectedFile);
result1 = execute(selFileCompl);


fPath = uigetdir('.', 'Select directory containing XLS files to query against');
if fPath==0, error('no folder selected'), end
fNames = dir( fullfile(fPath,'*.xls*') );
fullNames = strcat(fPath, filesep, {fNames.name});
elements = length(fNames);
rank = zeros(1,elements);
simpleN = cell(elements);

%# process each file
for i=1:elements
    name = fullNames{1,i};
    result2 = execute(name);
    [a,b] = compareAndCrop(result1,result2);
    c = corr2(a,b);
    
    simpleName = regexp(name,'\','split');
    simpleN(1,i) = simpleName(1,length(simpleName));
    disp([simpleN{1,i}, ' - Score: ', num2str(c)]);
    rank(1,i) = c;
end

highest = max(rank(1,:));
index = find(rank(1,:) == highest);
disp(' ');

disp(['Match ',simpleN{1,index},' - Score: ',num2str(highest)]);