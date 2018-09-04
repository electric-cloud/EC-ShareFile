import com.electriccloud.client.groovy.ElectricFlow
$[/myProject/preamble]

  public static void main(String[] args) throws Exception {

    ElectricFlow ef = new ElectricFlow()

    // Get Credentials
    def resp = ef.getFullCredential(credentialName: '$[config]')
    def userName = resp.credential.userName
    def password = resp.credential.password

    def company=ef.getProperty(propertyName:"/myProject/ec_plugin_cfgs/$[config]/company").property.value
    println "Company: $company"

    def folderToCreate = ef.getProperty(propertyName:"/myProject/ec_plugin_cfgs/$[config]/folder").property.value
    folderToCreate += "/$[folder]"
    println("Folder: $folderToCreate")

    def list = folderToCreate.split("/")
    def lastItem = list.last()
    def path = folderToCreate - ~/$lastItem/

    ShareFileSample sample = new ShareFileSample();
    HashMap<String, Object> optionalParameters = new HashMap<String, Object>();

    boolean loginStatus = sample.authenticate("$company", "sharefile.com", "$userName", "$password");
    if (loginStatus)
    {
        String id = sample.folderCreate(lastItem, path);
        optionalParameters.put("folderid",id);
        sample.fileUpload("$[pathToFile]", optionalParameters)
    }
  }

}
