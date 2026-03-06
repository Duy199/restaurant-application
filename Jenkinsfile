pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'duycool123/restaurant-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Build Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials') {
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push('latest')
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Build and push successful!'
        }
        failure {
            echo 'Build or push failed!'
        }
    }
}