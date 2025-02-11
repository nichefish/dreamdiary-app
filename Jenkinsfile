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
        BRANCH = 'main'
    }

    stages {
        /**
         * Node.js 패키지 설치
         */
        stage('Install NPM Dependencies') {
            steps {
                sh 'npm install'
            }
        }

        /**
         * jar에 포함될 Static Asseets 복사
         * (ex. Metronic(유료 템플릿) 등 jar에 포함되지만 git으로 관리되지 않는 항목들.
         */
        stage('Prepare Assets') {
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

        /**
         * bootJar 빌드 실행
         * tsc, sass를 먼저 수행 후 빌드 결과물을 jar에 포함한다.
         */
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'  // 실행 권한 추가
                sh './gradlew --daemon bootJar --parallel --build-cache --scan --info --stacktrace'
            }
        }

        // TODO: 테스트 실행
        // stage('Test') {
        //     steps {
        //         sh './gradlew test'
        //     }
        // }

        /**
         * 배포 패키지 준비
         * jar파일 및 기타 외부 파일들을 배포 폴더에 복사 후 압축한다.
         */
        stage('Packaging') {
            steps {
                script {
                    def lib = "build/libs"
                    def dist = "build/dist"

                    // dist 폴더 초기화
                    sh "find ${dist} -mindepth 1 -delete"

                    // dist 폴더에 배포 파일 복사
                    sh "mv $lib/dreamdiary-*.jar $dist/dreamdiary.jar"  // 패턴에 매칭되는 파일이 한 개만 존재한다면 문제없이 동작합니다.
                    sh "cp -R config $dist"

                    sh "mkdir -p ${dist}/file"  // `file` 폴더가 없으면 생성
                    sh "cp -R file/upfile ${dist}/file/"
                    sh "cp -R file/content ${dist}/file/"
                    sh "mkdir -p ${dist}/file/report"
                    sh "mkdir -p ${dist}/file/report/xlsx_template"
                    sh "cp -R file/report/xlsx_template ${dist}/file/report/xlsx_template/"

                    sh "cp -R lib $dist"
                    sh "cp -R static $dist"
                    sh "cp -R templates $dist"
                    sh "cp deploy.sh $dist"
                    sh "cp run.sh $dist"
                    sh """
                        mkdir -p ${dist}/config/env  # config/env 폴더가 없으면 생성
                        if [ -d "\$JENKINS_HOME/static" ]; then
                            cp \$JENKINS_HOME/static/config/.env* ${dist}/config/env
                        else
                            echo 'Jenkins 환경 변수 파일 없음, 복사 건너뜀'
                        fi
                    """

                    // 배포용 아카이브 생성 (tar.gz)
                    sh "tar czvf build/dist.tar.gz -C $dist ."
                }
            }
        }

        /**
         * 배포 스크립트 실행
         * 생성된 배포용 압축파일을 AWS EC2에 복사한다.
         */
        stage('Deploy') {
            steps {
                script {
                    def awsUser = "dreamdiary"
                    def awsHost = "43.200.175.156"
                    def remoteDir = "/home/dreamdiary/app/dreamdiary"

                    def distPackage = "build/dist.tar.gz"

                    // dist 압축파일 전송
                    sshagent(['dreamdiary-aws-key']) {
                        sh "scp -o StrictHostKeyChecking=no ${distPackage} ${awsUser}@${awsHost}:${remoteDir}/"
                    }

                    // 압축 해제
                    sshagent(['dreamdiary-aws-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ${awsUser}@${awsHost} << 'EOF'
                                cd ${remoteDir}

                                # 기존 static, templates 폴더 삭제
                                rm -rf static templates
                                rm -rf file/content file/report/xlsx_template

                                # 배포 패키지 압축 해제
                                tar xzvf dist.tar.gz

                                # 배포 스크립트에 실행 권한 부여
                                chmod +x deploy.sh

                                # 배포 스크립트 실행
                                ./deploy.sh

                                exit
                            EOF
                        """
                    }
                }
            }
        }
    }
}