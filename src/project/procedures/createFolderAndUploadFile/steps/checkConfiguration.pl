#############################################################################
#
# Copyright Electric-Cloud 2015
#
#############################################################################

$[/plugins[EC-Admin]project/scripts/perlHeader]

use ElectricCommander::PropDB;

#############################################################################
#
# Parameters
#
#############################################################################
my $ConfigName = "$[config]";

my %configuration = getConfiguration($ConfigName);
#inject config...
if(%configuration) {
    if($configuration{'user'} eq '' || $configuration{'password'} eq ''){
        print qq{Unable to retrieve data from configuration "$ConfigName".\n};
        exit 1;
    }
} else {
    print qq{Unable to find configuration "$ConfigName".\n};
    exit 1;
}



###########################################################################
=head2 getConfiguration
 
  Title    : getConfiguration
  Usage    : getConfiguration("Configuration name");
  Function : get the information of the configuration given 
  Returns  : hash containing the configuration information
  Args     : named arguments:
           : -configName => name of the configuration to retrieve
           :
=cut
###########################################################################
sub getConfiguration{

    my $configName = shift;
    my %configToUse;

    my $pluginConfigs = new ElectricCommander::PropDB($ec,"/plugins[EC-ShareFile]project/plugin_cfgs");

    my %configRow = $pluginConfigs->getRow($configName);

    # Check if configuration exists
    unless(keys(%configRow)) {
        print "Credential $configName does not exist";
        exit 1;
    }
    
    # Get user/password out of credential
    my $xpath = $ec->getFullCredential($configRow{credential});
    $configToUse{'user'} = $xpath->findvalue("//userName");
    $configToUse{'password'} = $xpath->findvalue("//password");

    foreach my $c (keys %configRow) {   
    	#getting all values except the credential that was read previously
    	if($c ne "credential"){
    		$configToUse{$c} = $configRow{$c};
		}

	}
	return %configToUse;
}
