timeout(10) {
    node("maven") {

        wrap([$class: 'BuildUser']) {
            currentBuild.description = """
                build user : $BUILD_USER
                branch : $BRANCH
            """

            params = readYaml text: env.YAML_CONFIG ?: null
            if (params != null) {
                for (param in params.entrySet()) {
                    env.setProperty(param.getKey(), param.getValue())
                }
            }
        }

        stage("Checkout") {
            checkout scm
        }

        stage("Create configurations") {
            sh "echo REMOTE_URL=${env.getProperty('REMOTE_URL')} > ./.env"
            sh "echo BASE_URL=${env.getProperty('BASE_URL')} >> ./.env"
            sh "echo BROWSER=${env.getProperty('BROWSER')} >> ./.env"
            sh "echo VERSION_BROWSER=${env.getProperty('VERSION_BROWSER')} >> ./.env"
            sh "echo REMOTE=${env.getProperty('REMOTE')} >> ./.env"
        }

        stage("Build Docker image") {
            sh "docker build -t ui_tests:1.0.0 ."
        }

        stage("Run tests") {
            def exitCode = sh(
                    returnStatus: true,
                    script: """
                        docker run --rm \
                        --env-file ./.env \
                        --network=host
                        -v m2:/root/.m2 \
                        -v ./allure-results:/home/user/ui_tests/target/allure-results \
                        -t ui_tests:1.0.0
                    """
            )
            if(exitCode == 1) {
                currentBuild.result = 'UNSTABLE'
            }
        }

        stage("Publish allure results") {
            allure([
                    includeProperties: false,
                    jdk: '',
                    properties: [],
                    reportBuildPolicy: 'ALWAYS',
                    results: [[path: './allure-results']]
            ])
        }
    }
}