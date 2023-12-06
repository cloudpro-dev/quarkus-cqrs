pipeline {
    agent any
    stages {
        stage('Load Test'){
            steps {
                sh(label: 'Maven build', script: "./load-testing/mvnw gatling:test -Dgatling.simulationClass=cqrs.SmokeTestSimulation")
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }
    }
}