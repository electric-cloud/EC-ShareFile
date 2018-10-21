import spock.lang.*
import com.electriccloud.spec.SpockTestSupport
import SharefileHelper

// @Ignore
@Stepwise
class uploadSuite extends SharefileHelper {

  def configNameWrongCredentials = 'specConfig-wrong-credentials'

  //variables for negative tests( where section)
  @Shared
  def sharefileJobName

  def doSetupSpec() {
      createConfiguration(configName, [doNotRecreate:false])
      createConfigurationWrongCredentials(configNameWrongCredentials, [doNotRecreate:false])
  }
  @Unroll
  def "Negative scenarios"() {
    when 'procedure runs'
    def jobName = sharefileJobNames.parametrizedBuild
    def buildParameters = 'param1=value1,param2=value2'
    def result = runProcedure([
           jobName: sharefileJobName,
           configName: sharefileConfigName,
           buildParameters: '',
       ]
   )
   then: 'Assert results'
   waitUntil {
       try {
           jobCompleted(result)
       } catch (Exception e) {
           println e.getMessage()
       }
   }
   def outcome = getJobProperty('/myJob/outcome', result.jobId)
   assert outcome == expectedOutcome

    where:
     sharefileConfigName        | expectedOutcome
     configNameWrongCredentials | 'error'

}
