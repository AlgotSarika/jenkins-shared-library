def call(Map configMap){
    pipeline {
        agent { label 'AGENT-1' }
        environment { 
            PROJECT = expense
            COMPONENT = backend
            appVersion = ''
            ACC_ID = '879381244178'
        }

     options {
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    parameters{
        booleanParam(name: 'deploy', defaultValue: false, description: 'Toggle this value')
    }
    stages {
        stage('Read Version') {
            steps {
                script {
                  def packageJson = readJSON file: 'package.json'
                  appVersion = packageJson.version
                  echo "Version is: $appVersion"
                    
                }
               
            }
        }

        stage('Install Dependencies') {
            steps {
               script{ 
                 sh """
                    npm install
                 """
               }
            }
        }

        // stage('Run Sonarqube') {
        //     environment {
        //         scannerHome = tool 'sonar-scanner-7.1';
        //     }
        //     steps {
        //       withSonarQubeEnv('sonar-scanner-7.1') {
        //         sh "${scannerHome}/bin/sonar-scanner"
        //         // This is generic command works for any language
        //       }
        //     }
        // }

        // stage("Quality Gate") {
        //     steps {
        //       timeout(time: 1, unit: 'HOURS') {
        //         waitForQualityGate abortPipeline: true
        //       }
        //     }
        // } 

        stage('Docker Build') {
            steps {
               script{ 
                 withAWS(region: 'us-east-1', credentials: "aws-creds-${environment}") {
                 sh """
                    aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com

                    docker build -t  ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com/${project}/${component}:${appVersion} .
                    docker push ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com/${project}/${component}:${appVersion}
                 """
               }
               }
            }
        }
        stage('Trigger Deploy'){
            when { 
                 expression { params.deploy }
             }
             steps{
                build job: 'backend-cd', parameters: [string(name: 'version', value: "${appVersion}")], wait: false
            }
        }

    }    
    
    post { 
    
        always { 
            echo 'I will always say hello again!'
            deleteDir()
        }
        failure { 
            echo 'This session runs when pipeline failure'
        }
        success { 
            echo 'This session runs when pipeline success'
        }
    }
}
}