# promote/demote action
# upgrade action
if ($upgradeAction eq "upgrade") {
    my $query = $commander->newBatch();
    my $newcfg = $query->getProperty(
        "/plugins/$pluginName/project/plugin_cfgs");
    my $oldcfgs = $query->getProperty(
        "/plugins/$otherPluginName/project/plugin_cfgs");
	my $creds = $query->getCredentials("\$[/plugins/$otherPluginName]");

	local $self->{abortOnError} = 0;
    $query->submit();

    # if new plugin does not already have cfgs
    if ($query->findvalue($newcfg,"code") eq "NoSuchProperty") {
        # if old cfg has some cfgs to copy
        if ($query->findvalue($oldcfgs,"code") ne "NoSuchProperty") {
            $batch->clone({
                path => "/plugins/$otherPluginName/project/plugin_cfgs",
                cloneName => "/plugins/$pluginName/project/plugin_cfgs"
            });
        }
    }
	
	# Copy configuration credentials and attach them to the appropriate steps
    my $nodes = $query->find($creds);
    if ($nodes) {
        my @nodes = $nodes->findnodes("credential/credentialName");
        for (@nodes) {
            my $cred = $_->string_value;

            # Clone the credential
            $batch->clone({
                path => "/plugins/$otherPluginName/project/credentials/$cred",
                cloneName => "/plugins/$pluginName/project/credentials/$cred"
            });

            # Make sure the credential has an ACL entry for the new project principal
            my $xpath = $commander->getAclEntry("user", "project: $pluginName", {
                projectName => $otherPluginName,
                credentialName => $cred
            });
            if ($xpath->findvalue("//code") eq "NoSuchAclEntry") {
                $batch->deleteAclEntry("user", "project: $otherPluginName", {
                    projectName => $pluginName,
                    credentialName => $cred
                });
                $batch->createAclEntry("user", "project: $pluginName", {
                    projectName => $pluginName,
                    credentialName => $cred,
                    readPrivilege => "allow",
                    modifyPrivilege => "allow",
                    executePrivilege => "allow",
                    changePermissionsPrivilege => "allow"
                });
            }

            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => "createFolderAndUploadFile",
                stepName => "createFolderAndUploadFile"
            });
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => "createFolderAndUploadFile",
                stepName => "checkConfiguration"
            });
        }
    }
}


# Data that drives the create step picker registration for this plugin.
my %createFolderAndUploadFile = ( 
  label       => "EC-ShareFile - createFolderAndUploadFile", 
  procedure   => "createFolderAndUploadFile", 
  description => "", 
  category    => "Administration" 
);

@::createStepPickerSteps = (\%createFolderAndUploadFile);
