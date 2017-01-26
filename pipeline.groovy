String ocpApiServer = env.OCP_API_SERVER ? "${env.OCP_API_SERVER}" : "https://openshift.default.svc.cluster.local"

node('master') {

  env.NAMESPACE = readFile('/var/run/secrets/kubernetes.io/serviceaccount/namespace').trim()
  env.TOKEN = readFile('/var/run/secrets/kubernetes.io/serviceaccount/token').trim()
  env.OC_CMD = "oc --token=${env.TOKEN} --server=${ocpApiServer} --certificate-authority=/run/secrets/kubernetes.io/serviceaccount/ca.crt --namespace=${env.NAMESPACE}"

  env.APP_NAME = "${env.JOB_NAME}".replaceAll(/-?pipeline-?/, '').replaceAll(/-?${env.NAMESPACE}-?/, '')
  def projectBase = "${env.NAMESPACE}".replaceAll(/-dev/, '')
  env.STAGE1 = "${projectBase}-dev"
  env.STAGE2 = "${projectBase}-qa"
  env.STAGE3 = "${projectBase}-prod"
}

node('maven') {
  def mvnHome = "/usr/share/maven/"
  def mvnCmd = "${mvnHome}bin/mvn"
  stage('Scm Checkout') {
  	checkout scm
  }
  stage('Build') {

    sh "${mvnCmd} clean --batch-mode install -DskipTests=true"

  }

  stage('Build Image') {

  	sh "find target -name 'pipeline*jar' > pipelineFile"
  	def file=readFile('pipelineFile')
  	sh "${OC_CMD} start-build ${env.APP_NAME} --from-file=${file} --wait=true --follow=true"
  }

  input "Promote to QA?"

  stage('Promote to QA') {
    sh """
    ${env.OC_CMD} tag ${env.STAGE1}/${env.APP_NAME}:latest ${env.STAGE2}/${env.APP_NAME}:latest
    """

    input "Promote Application to Prod?"	
  }

  stage('Promote To Prod') {
    sh """
    ${env.OC_CMD} tag ${env.STAGE1}/${env.APP_NAME}:latest ${env.STAGE3}/${env.APP_NAME}:latest
    """

  }
}