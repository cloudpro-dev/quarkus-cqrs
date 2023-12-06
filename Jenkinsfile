pipeline {
    agent any
    stages {
        stage('Load Test'){
            steps {
                sh(label: 'Maven build', script: "./mvnw -f./load-testing/pom.xml gatling:test -Dgatling.simulationClass=cqrs.SmokeTestSimulation")
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }
    }
}