pipeline {
    agent any
    stages {
        stage('Build'){
            steps {
                sh(label: 'Maven build', script: "./mvnw clean test")
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }
    }
}