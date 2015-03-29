# Data that drives the create step picker registration for this plugin.
my %CreateFolderAndUploadFile = ( 
  label       => "EC-ShareFile - CreateFolderAndUploadFile", 
  procedure   => "CreateFolderAndUploadFile", 
  description => "", 
  category    => "Other" 
);

@::createStepPickerSteps = (\%CreateFolderAndUploadFile);
