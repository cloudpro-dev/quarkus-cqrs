pipeline {
    agent any
    stages {
        stage('Build'){
            steps {
                sh(label: 'Maven build', script: "./mvnw clean")
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }
    }
}