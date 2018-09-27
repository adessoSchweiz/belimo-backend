@Library('semantic_releasing') _

podTemplate(label: 'mypod', containers: [
        containerTemplate(name: 'docker', image: 'docker', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.11.2', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'maven', image: 'maven:3.5.2-jdk-8', command: 'cat', ttyEnabled: true)
],
        volumes: [
                hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
        ]) {
    node('mypod') {

        stage('checkout & unit tests & build') {
            git url: 'https://github.com/adessoschweiz/belimo-backend'
            container('maven') {
                sh 'mvn clean package'
            }
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
        }

        stage('build image & git tag & docker push') {
            env.VERSION = semanticReleasing()
            currentBuild.displayName = env.VERSION

            container('maven') {
                sh "mvn versions:set -DnewVersion=${env.VERSION}"
            }
            sh "git config user.email \"jenkins@khinkali.ch\""
            sh "git config user.name \"Jenkins\""
            sh "git tag -a ${env.VERSION} -m \"${env.VERSION}\""
            withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/adessoschweiz/belimo-backend.git --tags"
            }

            container('docker') {
                sh "docker build -t robertbrem/belimo-backend:${env.VERSION} ."
                withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh "docker login --username ${DOCKER_USERNAME} --password ${DOCKER_PASSWORD}"
                }
                sh "docker push robertbrem/belimo-backend:${env.VERSION}"
            }
        }

        stage('deploy') {
            sh "sed -i -e 's~image: robertbrem/belimo-backend:todo~image: robertbrem/belimo-backend:${env.VERSION}~' kubeconfig.yml"
            sh "sed -i -e 's~value: \"todo\"~value: \"${env.VERSION}\"~' kubeconfig.yml"
            container('kubectl') {
                sh "kubectl apply -f kubeconfig.yml"
            }
            waitUntilReady('app=belimo-frontend', 'belimo-frontend')
        }

        stage('system tests') {
            container('maven') {
                sh "mvn clean integration-test failsafe:integration-test failsafe:verify"
            }
            junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml'
        }
    }
}