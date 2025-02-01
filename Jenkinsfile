pipeline {
    agent any

    stages {
        // maim 브랜치 체크아웃
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/nichefish/dreamdiary.git'
            }
        }

        // bootJar 빌드 실행
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'  // 실행 권한 추가
                sh './gradlew clean bootJar -x test'
            }
        }
        // 테스트 실행
        // stage('Test') {
        //     steps {
        //         sh './gradlew test'
        //     }
        // }
        // 배포 스크립트 실행
        // stage('Deploy') {
        //     steps {
        //         sh './deploy.sh'
        //     }
        // }
    }
}