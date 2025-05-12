pipeline {
    agent any

    environment {
        // Define environment variables
        DOCKER_HUB_CREDENTIALS = credentials('12212606') // Docker Hub credentials stored in Jenkins
        DOCKER_IMAGE = 'kaitongqin/teedy-app' // Your Docker Hub username/repository
        DOCKER_TAG = "${env.BUILD_NUMBER}" // Use Jenkins build number as the Docker tag
    }

    stages {
        // Stage 1: Build the application
        stage('Build') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/register-new-user']],
                    userRemoteConfigs: [[url: 'https://github.com/KaitongQin/Teedy.git']] // Your GitHub repo
                )
                sh 'mvn -B -DskipTests clean package' // Build with Maven
            }
        }

        // Stage 2: Build Docker image
        stage('Building image') {
            steps {
                script {
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        // Stage 3: Push Docker image to Docker Hub
        stage('Upload image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'DOCKER_HUB_CREDENTIALS') {
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                        // Optional: Tag as 'latest'
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        // Stage 4: Run Docker container
        stage('Run containers') {
            steps {
                script {
                    // Stop and remove existing container (if any)
                    sh 'docker stop teedy-container-8081 || true'
                    sh 'docker rm teedy-container-8081 || true'

                    // Run new container
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
                        '--name teedy-container-8081 -d -p 8081:8080'
                    )

                    // Optional: List running containers
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}