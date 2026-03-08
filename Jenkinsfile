pipeline {
    agent any

    environment {
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Build Image') {
            steps {
                echo "Building Docker image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
                echo "Build completed successfully"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                echo "Pushing ${DOCKER_IMAGE}:${DOCKER_TAG} to Docker Hub"
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials') {
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push('latest')
                    }
                }
                echo "Push completed successfully"
            }
        }

        stage('Deploy to VPS') {
            steps {
                echo "Deploying to VPS at ${VPS_HOST}"
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sshagent(['vps-ssh-key']) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no root@${VPS_HOST} \
                                "echo '${DOCKER_PASS}' | docker login -u '${DOCKER_USER}' --password-stdin && \
                                cd /root/project/restaurant-app && \
                                docker compose up -d --pull always && \
                                docker logout"
                        '''
                    }
                }
                echo "Deploy completed successfully"
            }
        }
    }

    post {
        success {
            echo "Pipeline #${BUILD_NUMBER} completed successfully!"
        }
        failure {
            echo "Pipeline #${BUILD_NUMBER} failed!"
        }
    }
}
