$[/myProject/preamble]


  public static void main(String[] args) throws Exception
    {
    
        def commanderServer = 'https://' + System.getenv('COMMANDER_SERVER')
        def commanderPort = System.getenv("COMMANDER_HTTPS_PORT")
 
        def sessionId = System.getenv('COMMANDER_SESSIONID')
 
        def client = new RESTClient(commanderServer + ":" + commanderPort)
 
        client.ignoreSSLIssues()
 
        def jobStepId = "$[/myJobStep/jobStepId]"
 
        def resp = client.get( path : '/rest/v1.0/jobsSteps/' + jobStepId + '/credentials/$[config]', headers:['Cookie':"sessionId="+sessionId, 'Accept': 'application/json'])
 
        assert resp.status == 200 
 
        def userName = resp.getData().credential.userName
        def password = resp.getData().credential.password


        def folderToCreate = "$[folderToCreate]"

        def list = folderToCreate.split("/")
 
        def lastItem = list.last()

        def path = folderToCreate - ~/$lastItem/

        ShareFileSample sample = new ShareFileSample();
        HashMap<String, Object> optionalParameters = new HashMap<String, Object>();

        boolean loginStatus = sample.authenticate("$[company]", "sharefile.com", "$userName", "$password");
        if (loginStatus)
        {
            String id = sample.folderCreate(lastItem, path);
            optionalParameters.put("folderid",id);
            sample.fileUpload("$[pathToFile]",optionalParameters)


        }
    }

}
