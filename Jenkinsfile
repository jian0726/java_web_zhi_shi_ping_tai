// ============================================================
// 好YOU经验创作者知识平台 —— 持续集成流水线（Jenkins 声明式）
// 位置：仓库根目录 /Jenkinsfile
//
// 重要：Maven 工程位于子目录 zhi_shi_ku/，不在仓库根目录，
//       因此每条 mvn 命令都要先 dir('zhi_shi_ku') 进去再执行。
//
// 配套环境：自建 Jenkins（Docker 容器，本机 8081 端口）
//           镜像内已预装 JDK 21 / Maven 3.9.16 / Node 20 / Docker CLI
// ============================================================
pipeline {
    agent any

    options {
        timeout(time: 20, unit: 'MINUTES')             // 超时保护，防止构建挂死
        buildDiscarder(logRotator(numToKeepStr: '10')) // 只保留最近 10 次构建记录
        disableConcurrentBuilds()                      // 同任务不并发，避免抢占端口
    }

    environment {
        BACKEND_DIR = 'zhi_shi_ku'   // Maven 模块目录
        FRONT_DIR   = 'web'          // 前端目录（尚未创建，建好后自动启用）
        MAVEN_OPTS  = '-Xmx768m'
    }

    stages {

        // ---------- 0. 环境自检：一眼看出是哪条工具链出了问题 ----------
        stage('环境自检') {
            steps {
                echo "构建编号 #${env.BUILD_NUMBER}  提交 ${env.GIT_COMMIT}"
                sh '''
                    echo "---------- JDK ----------";  java -version 2>&1
                    echo "---------- Maven --------";  mvn -v 2>&1 | head -3
                    echo "---------- Node ---------";  node -v; npm -v
                    echo "---------- Docker ------";  docker version --format "CLI {{.Client.Version}} / Server {{.Server.Version}}" 2>&1 || echo "docker 不可用"
                    echo "-------------------------"
                '''
            }
        }

        // ---------- 1. 后端编译 ----------
        stage('后端 · 编译') {
            steps {
                dir("${BACKEND_DIR}") {
                    sh 'mvn -B -ntp clean compile'
                }
            }
        }

        // ---------- 2. 后端单元测试 ----------
        stage('后端 · 单元测试') {
            steps {
                dir("${BACKEND_DIR}") {
                    // 骨架阶段还没有测试类，allowEmptyResults 保证"零测试"不算失败
                    sh 'mvn -B -ntp test'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: "${BACKEND_DIR}/target/surefire-reports/*.xml"
                }
            }
        }

        // ---------- 3. 后端打包 ----------
        stage('后端 · 打包') {
            steps {
                dir("${BACKEND_DIR}") {
                    sh 'mvn -B -ntp package -DskipTests'
                }
            }
        }

        // ---------- 4. 前端构建（web/ 目录出现后自动开始执行） ----------
        stage('前端 · 构建') {
            when {
                expression { fileExists("${FRONT_DIR}/package.json") }
            }
            steps {
                dir("${FRONT_DIR}") {
                    sh 'npm ci --registry=https://registry.npmmirror.com || npm install --registry=https://registry.npmmirror.com'
                    sh 'npm run build'
                }
            }
        }

        // ---------- 5. 归档产物 ----------
        stage('归档产物') {
            steps {
                archiveArtifacts artifacts: "${BACKEND_DIR}/target/*.jar",
                                 fingerprint: true,
                                 allowEmptyArchive: true
                archiveArtifacts artifacts: "${FRONT_DIR}/dist/**",
                                 allowEmptyArchive: true
            }
        }

        // ---------- 6.（预留）构建应用镜像 ----------
        // 等 Spring Boot 代码写起来、zhi_shi_ku/Dockerfile 建好之后，取消注释即可启用
        // stage('构建应用镜像') {
        //     steps {
        //         sh "docker build -t haoyou/backend:${env.BUILD_NUMBER} ${BACKEND_DIR}"
        //     }
        // }

        // ---------- 7.（预留）部署上线 ----------
        // stage('部署上线') {
        //     when { branch 'main' }
        //     steps {
        //         sh 'docker compose -f deploy/docker-compose.yml up -d'
        //     }
        // }
    }

    post {
        success {
            echo "✅ 流水线通过 —— 第 ${env.BUILD_NUMBER} 次构建"
        }
        failure {
            echo "❌ 流水线失败 —— 第 ${env.BUILD_NUMBER} 次构建，请查看上面失败阶段的日志定位问题"
        }
        always {
            echo "结束状态：${currentBuild.currentResult}，耗时 ${currentBuild.durationString}"
        }
    }
}
