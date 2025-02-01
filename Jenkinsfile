pipeline {
    agent any

    options {
        disableConcurrentBuilds()  // 동시 실행 방지
    }

    stages {
        // maim 브랜치 체크아웃
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/nichefish/dreamdiary.git'
            }
        }

        // Node.js 패키지 설치
        stage('Install Dependencies') {
            steps {
                sh 'npm install'  // Node.js 패키지 설치
            }
        }

        stage('Build Frontend') {
            steps {
                sh 'npm run build'  // TypeScript & SCSS 빌드 실행
            }
        }

        // bootJar 빌드 실행
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'  // 실행 권한 추가
                sh './gradlew bootJar --scan --info'
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