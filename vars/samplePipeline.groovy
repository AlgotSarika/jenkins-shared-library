def call(Map configMap){
    pipeline {
        agent { label 'AGENT-1' }
        environment { 
            greeting = configMap.get('greeting')
        }
        options {
            disableConcurrentBuilds()
            timeout(time: 30, unit: 'MINUTES')
        }
        parameters{
            booleanParam(name: 'deploy', defaultValue: false, description: 'Toggle this value')
        }
        stages {
            stage('print greeting') {
                steps {
                  script{
                    
                    echo "Version is: $greeting"
                }
                }
            }
            
            /* stage('Run Sonarqube') {
                environment {
                    scannerHome = tool 'sonar-scanner-7.1';
                }
                steps {
                withSonarQubeEnv('sonar-scanner-7.1') {
                    sh "${scannerHome}/bin/sonar-scanner"
                    // This is generic command works for any language
                }
                }
            }
            stage("Quality Gate") {
                steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
                }
            } */
            
            
        }
        post { 
            always { 
                echo 'I will always say Hello again!'
                deleteDir()
            }
            failure { 
                echo 'I will run when pipeline is failed'
            }
            success { 
                echo 'I will run when pipeline is success'
            }
        }
    }
}