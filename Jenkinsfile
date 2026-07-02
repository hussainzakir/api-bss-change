// load the shared library and create an instance of BuildFunctions
@Library('jenkins-shared-libraries') import com.trinet.api.BuildFunctions
def builder = new BuildFunctions()

// the following values are provided as Global Jenkins properties
echo "env.HOURS_BEFORE_CLEANUP = ${env.HOURS_BEFORE_CLEANUP}"
echo "env.SONARQUBE_ENV = ${env.SONARQUBE_ENV}"
echo "env.MAVEN_REPOSITORY = ${env.MAVEN_REPOSITORY}"
echo "env.DOCKER_REGISTRY = ${env.DOCKER_REGISTRY}"
echo "env.K8S_API_URL = ${env.K8S_API_URL}"
echo "env.K8S_CLUSTER_NAME = ${env.K8S_CLUSTER_NAME}" 
echo "env.K8S_INGRESS_URL_BASE = ${env.K8S_INGRESS_URL_BASE}"

// possibly override the target K8S cluster for this build
node {
  withFolderProperties {
    if (env.K8S_CLUSTER_NAME_FOLDER) {
      env.K8S_CLUSTER_NAME = env.K8S_CLUSTER_NAME_FOLDER
    }
  }
}
echo "possibly updated env.K8S_CLUSTER_NAME = ${env.K8S_CLUSTER_NAME}" 

// Unique label for the build node in kubernetes
def nodeLabel = "api-build-${UUID.randomUUID().toString()}"

// Additional containers
// buildContainers = [
//   containerTemplate(name: 'busybox', image: 'busybox', alwaysPullImage: false, ttyEnabled: true, command: 'cat')
// ]
// Additional volumes
// buildVolumes = [
//   nfsVolume(mountPath: '/src/hrp', serverAddress: '10.0.40.11', serverPath: '/mnt/hrp-code-content', readOnly: false)
// ]
// builder.buildInKubernetes(label: nodeLabel, containers: buildContainers, volumes: buildVolumes) {

builder.buildInKubernetes(label: nodeLabel, cloud: env.K8S_CLUSTER_NAME) {

  def scmVars

  env.DOCKER_IMAGE_TAG = env.BUILD_TAG.replace("%2F", "-")

  node(nodeLabel) {
 properties([disableConcurrentBuilds(),disableResume()]) 

    withFolderProperties {
      // the following values need to be defined as Folder Properties
      echo "API_NAME = $API_NAME"
      echo "K8S_SERVICE_ACCOUNT_CREDENTIAL = $K8S_SERVICE_ACCOUNT_CREDENTIAL"
      echo "BASE_ENVIRONMENT = $BASE_ENVIRONMENT"
      echo "K8S_NAMESPACE = $K8S_NAMESPACE"
      echo "AUTH_COOKIE_NAME = $AUTH_COOKIE_NAME"
      echo "ENDPOINT_UNAUTHENTICATED = $ENDPOINT_UNAUTHENTICATED"
      echo "PATH_APPLICATION_PROPERTIES = $PATH_APPLICATION_PROPERTIES"

      // generate URLs
      generatedURLs = builder.generateURLs(k8sNamespace: K8S_NAMESPACE, k8sIngressURLBase: K8S_INGRESS_URL_BASE, endpointUnauthenticated: ENDPOINT_UNAUTHENTICATED)

      stage('Clean workspace') {
        deleteDir()
      }
      stage('Clone source code') {
          scmVars = checkout scm
          
          //BSS API automation checkout
          checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'BSSAPITest']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'git-automation-ro', url: 'https://stash.trinet-devops.com/scm/aut/bssapiautomation.git']]])
   
          
          // scmVars contains the following values
          // GIT_BRANCH=origin/daniel-update
          // GIT_COMMIT=9a1fc82707ebaf806f2e310fce15a7a54238eb71
          // GIT_PREVIOUS_COMMIT=9a1fc82707ebaf806f2e310fce15a7a54238eb71
          // GIT_PREVIOUS_SUCCESSFUL_COMMIT=9a1fc82707ebaf806f2e310fce15a7a54238eb71
          // GIT_URL=https://stash.trinet-devops.com/scm/poc/api-sample.git
      }

      // build, test and analyze
      builder.mavenBuild(mavenRepository: env.MAVEN_REPOSITORY)
      builder.mavenTest(mavenRepository: env.MAVEN_REPOSITORY)
      builder.updateVersionJSON(mavenRepository: env.MAVEN_REPOSITORY, gitCommit: scmVars.GIT_COMMIT)
      builder.mavenPackage(mavenRepository: env.MAVEN_REPOSITORY)
      builder.calculateCodeCoverageJacoco()
      builder.pmdAnalysis()
      builder.sonarQubeAnalysis(environment: SONARQUBE_ENV, gitBranch: scmVars.GIT_BRANCH, mavenRepository: env.MAVEN_REPOSITORY)
      // create and push docker image
      builder.buildTagPushDocker(dockerRegistry: env.DOCKER_REGISTRY, dockerImageName: API_NAME, dockerImageTag: env.DOCKER_IMAGE_TAG)
      def filesForConfigMap = []
      def yamlFiles = []
      // prepare deployment and configuration files
      stage('Render Config and Deployment Files') {
        yamlFiles = builder.renderKubernetesTemplates(urls: generatedURLs)
        def slmConfigFilesForConfigMap = builder.renderSLMConfigFiles(urls: generatedURLs)
        // also update application configuration
        sh "sed -i 's~BASE_ENVIRONMENT~$BASE_ENVIRONMENT~g' ${pwd()}$PATH_APPLICATION_PROPERTIES"
        filesForConfigMap = slmConfigFilesForConfigMap + ["${pwd()}$PATH_APPLICATION_PROPERTIES"]
      }
       // OKTA disable
      // deploy to kubernetes
      builder.applyToKubernetes(
        filesForConfigMap: filesForConfigMap, 
        yamlFiles: yamlFiles, 
        k8sNamespace: K8S_NAMESPACE, 
        urls: generatedURLs,
        k8sServiceAccountCredId: K8S_SERVICE_ACCOUNT_CREDENTIAL)
      // verify deployment using the logs
      // expectedLogs = ['Deployment of web application archive [/usr/local/tomcat/webapps/api-job-executor.war] has finished', 'INFO: Server startup in']
      // builder.deploymentLogContains(
      //   deploySuccessLogOccurrence: expectedLogs,
      //   urls: generatedURLs)
      
      // OKTA disable below
      // verify deployment using URL
      //builder.deploymentUrlIsLive(urls: generatedURLs)
      // post deploy activities
      // builder.restAssuredLiveTests(mavenRepository: env.MAVEN_REPOSITORY, urls: generatedURLs, authCookieName: AUTH_COOKIE_NAME)
      // builder.zapBaselineSecurityTest(urls: generatedURLs)
      builder.uploadWarToArtifactory(mavenRepository: env.MAVEN_REPOSITORY)
      
      //BSS Rest api automation run
     	builder.restAssuredAPIAutomationTests(dir:"BSSAPITest",mavenRepository: env.MAVEN_REPOSITORY,baseEnv: env.BASE_ENVIRONMENT,includedGROUP: env.INCLUDED_GROUP,dbCredentialId: env.DB_CREDENTIAL,apiBaseUrl:generatedURLs,xmlFIle: "src/test/resources/com/trinet/modules/$MODULE_NAME/$MODULE_NAME"+".xml",emailTo: env.MAILTO )
     	builder.publishTestNGReport(dir:"BSSAPITest",report:[$class: 'Publisher', reportFilenamePattern: '**//target/surefire-reports/testng-results.xml'] )
     	builder.thresholdCalulation(dir:"BSSAPITest",expectedPassPercentage: env.PASS_PERCENTAGE_THRESHOLD, xml: "**/target/surefire-reports/testng-results.xml")
 
      builder.finish(gitUrl: scmVars.GIT_URL,generatedURLs:generatedURLs)
     }
    }
}
