/**
 * Jenkinsfile
 * Jenkins Pipeline Script
 *
 * @author nichefish
 */
pipeline {
    agent any

    options {
        disableConcurrentBuilds()  // 동시 실행 방지
    }
    // 변수 선언
    environment {
        GRADLE_USER_HOME = "$JENKINS_HOME/.gradle"       // 워크스페이스 내에 캐시 폴더 생성
        REPO_URL = 'https://github.com/nichefish/dreamdiary.git'
        BRANCH = 'dev'
    }

    stages {
        // maim 브랜치 체크아웃
        stage('Checkout') {
            steps {
                git branch: "${BRANCH}", url: "${REPO_URL}"
            }
        }

        // Node.js 패키지 설치
        stage('Install NPM Dependencies') {
            steps {
                sh 'npm install'
            }
        }

        // jar에 포함될 Metronic(유료 템플릿) 파일 복사
        stage('Prepare Metronic') {
            steps {
                sh '''
                    if [ ! -d "$WORKSPACE/src/main/resources/static/metronic/assets" ]; then
                        echo "Assets 폴더가 없으므로 Metronic 템플릿을 복사합니다."
                        cp -R $JENKINS_HOME/static/metronic/. $WORKSPACE/src/main/resources/static/metronic
                        if [ $? -ne 0 ]; then exit 1; fi
                    else
                        echo "Bypassing copying Metronic assets because they already exist."
                   fi
                '''
            }
        }

        // 기존 jar 파일 삭제 (build/libs 폴더 내 모든 .jar 파일)
        stage('Clean Old Jar') {
            steps {
                sh '''
                   echo "Deleting old jar files..."
                   rm -f $WORKSPACE/build/libs/*.jar
                   if [ $? -ne 0 ]; then exit 1; fi
                '''
            }
        }

        // bootJar 빌드 실행
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'  // 실행 권한 추가
                sh './gradlew --daemon bootJar --scan --info --stacktrace'
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