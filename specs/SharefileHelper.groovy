import spock.lang.*
import com.electriccloud.spec.*
import spock.util.concurrent.PollingConditions;

class SharefileHelper extends PluginSpockTestSupport {
    def deleteConfiguration(String configName) {
        def result = dsl """
            runProcedure(
                projectName: '/plugins/EC-Sharefile/project',
                procedureName: 'DeleteConfiguration',
                actualParameter: [
                    config: '$configName'
                ]
            )
        """

        assert result.jobId
        waitUntil {
            // jobSucceeded(result.jobId)
            try {
                jobCompleted(result.jobId)
            } catch (Exception e) {
                println e.getMessage();
            }
        }
    }
    def dslWithTimeout(dslString, timeout = 3600) {
        def result = dsl(dslString)
        PollingConditions poll = new PollingConditions(timeout: timeout, initialDelay: 0,  factor: 1.25)
        poll.eventually {
            jobStatus(result.jobId).status == 'completed'
        }
        return result
    }
    def doesConfExist(def configName) {
        return doesConfExist('/plugins/EC-Sharefile/project/ec_plugin_cfgs',configName)
    }
    def createConfiguration(String configName, options = [:]) {
        return createConfiguration(configName, [:], options)
    }
    //def createConfiguration(String configName, params = [:], options = [:]) {
    def createConfiguration(String configName, def params, def options) {
        if (options.doNotRecreate) {
            if (doesConfExist(configName)) {
                println "Configuration $configName exists"
                return
            }
        }

        if (doesConfExist(configName)) {
            deleteConfiguration(configName)
        }
        def username = System.getenv('Sharefile_USERNAME') ?: 'admin'
        def password = System.getenv('Sharefile_PASSWORD') ?: 'changeme'
        def company = System.getenv('Sharefile_COMPANY') ?: 'electric-cloud'
        def folder = System.getenv('Sharefile_FOLDER') ?: 'employees/lrochette'

        def isProxyAvailable = System.getenv('IS_PROXY_AVAILABLE') ?: '0'
        def efProxyCompany = System.getenv('EF_PROXY_COMPANY') ?: ''
        def efProxyFolder = System.getenv('EF_PROXY_FOLDER') ?: ''
        def efProxyUsername = System.getenv('EF_PROXY_USERNAME') ?: ''
        def efProxyPassword = System.getenv('EF_PROXY_PASSWORD') ?: ''

        if (params.userName) {
            username = params.userName
        }
        if (params.password) {
            password = params.password
        }
        if (params.company) {
            company = params.company
        }
        if (params.folder) {
            folder = params.folder
        }

        println folder

        def result;
        // create configuration with proxy only when proxy env is available.
        if (isProxyAvailable != '0') {
            result = dsl """
            runProcedure(
                projectName: '/plugins/EC-Sharefile/project',
                procedureName: 'CreateConfiguration',
                credential: [
                    [
                        credentialName: 'proxy_credential',
                        userName: '$efProxyUsername',
                        password: '$efProxyPassword'
                    ],
                    [
                        credentialName: 'credential',
                        userName: '$username',
                        password: '$password'
                    ],
                ],
                actualParameter: [
                    config: '$configName',
                    folder: '$folder',
                    company: '$company'
                    credential: 'credential',
                    http_proxy: '$efProxyUrl',
                    proxy_credential: 'proxy_credential'
                ]
            )
            """
        }
        // There is no proxy, regular creation.
        else {
            result = dsl """
            runProcedure(
                projectName: '/plugins/EC-Sharefile/project',
                procedureName: 'CreateConfiguration',
                credential: [
                    credentialName: 'credential',
                    userName: '$username',
                    password: '$password'
                ],
                actualParameter: [
                    config: '$configName',
                    folder: '$fodler',
                    credential: 'credential',
                    company: '$company'
                ]
            )
            """
        }
        assert result.jobId
        waitUntil {
            try {
                //jobCompleted(result.jobId)
                jobCompleted(result)
            } catch (Exception e) {
                print e.getMessage();
            }
        }
    }


    def createConfigurationWrongCredentials(def configName, options = [:]) {
        def params = [
            userName: 'noexistentuser',
            password: 'completelywrongpassword'
        ]
        return createConfiguration(configName, params, options)
    }
    def createConfigurationWrongUrl(def configName, options = [:]) {
        def wrongUrl = System.getenv('EF_SERVER_HOSTNAME')
        if (wrongUrl) {
            wrongUrl = 'http://' + wrongUrl + ':8000'
        }
        else {
            wrongUrl = 'http://localhost:8000'
        }
        def params = [
            url: wrongUrl
        ]
        return createConfiguration(configName, params, options)
    }
}
