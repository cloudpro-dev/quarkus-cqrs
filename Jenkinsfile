def testGroups = [:]
def numberOfTestNodes = 2

pipeline {

    agent {
        label 'jenkins-agent-1'
    }

    parameters {
        separator(name: "Endpoints", sectionHeader: "Service Endpoints", sectionHeaderStyle: """font-size:2em""")

        string(defaultValue: 'http://localhost:9020',
                            description: 'Event Store URL',
                            name: 'eventStoreUrl')

        string(defaultValue: 'http://localhost:9010',
                            description: 'View Store URL',
                            name: 'viewStoreUrl')

        string(defaultValue: 'http://localhost:9040',
                            description: 'Aggregate View URL',
                            name: 'aggregateViewUrl')

        separator(name: "Test Type", sectionHeader: "Select load test type", sectionHeaderStyle: """font-size:2em""")

        choice(name: "TestType", choices: [
            "cqrs.SmokeTestSimulation",
            "cqrs.TargetLoadSimulation",
            "cqrs.SpikeTestSimulation",
            "cqrs.FatigueTestSimulation"
        ])

        separator(name: "Target Test", sectionHeader: "Target Test Parameters", sectionHeaderStyle: """font-size:2em""")

        string(defaultValue: '10',
                            description: 'Target Rate',
                            name: 'TargetTPSRate')

        string(defaultValue: '30',
                description: 'Test Duration',
                name: 'TargetTPSDuration')

        string(defaultValue: '5',
                description: 'Warmup Duration',
                name: 'TargetTPSWarmupDuration')

        separator(name: "Spike Test", sectionHeader: "Spike Test Parameters", sectionHeaderStyle: """font-size:2em""")

        string(defaultValue: '10.0',
                description: 'Base Rate',
                name: 'SpikeBaseRate')

        string(defaultValue: '10',
                description: 'Warmup Duration',
                name: 'SpikeWarmupDuration')

        string(defaultValue: '50',
                description: 'Max Rate',
                name: 'SpikeMaxRate')

        string(defaultValue: '10',
                description: 'Spike Interval',
                name: 'SpikeInterval')

        separator(name: "Fatigue Test", sectionHeader: "Fatigue Test Parameters", sectionHeaderStyle: """font-size:2em""")

        string(defaultValue: '10.0',
                description: 'Initial Rate',
                name: 'FatigueTestInitialRate')

        string(defaultValue: '5.0',
                description: 'Step Increase Rate',
                name: 'FatigueTestStepRateIncrease')

        string(defaultValue: '10',
                description: 'Step Duration',
                name: 'FatigueStepDuration')

        string(defaultValue: '4',
                description: 'Step Count',
                name: 'FatigueStepCount')
    }

    stages {
        stage("Setup") {
            steps {
                script {
                    env.EVENT_STORE_URL = "${eventStoreUrl}"
                    env.VIEW_STORE_URL = "${viewStoreUrl}"
                    env.AGGREGATE_VIEW_URL = "${aggregateViewUrl}"

                    // Echo to console
                    echo("Event Store URL: ${env.EVENT_STORE_URL}")
                    echo("View Store URL: ${env.VIEW_STORE_URL}")
                    echo("Aggregate View URL: ${env.AGGREGATE_VIEW_URL}")

                    env.SIMULATION_CLASS = "${TestType}"

                    env.TEST_NAME = "${SIMULATION_CLASS.substring(SIMULATION_CLASS.lastIndexOf('.') + 1)}"

                    // Echo to console
                    echo("Simulation Class: ${env.SIMULATION_CLASS}")
                    echo("Test Name: ${env.TEST_NAME}")

                    env.TARGET_TPS = "${TargetTPSRate}"
                    env.TARGET_TPS_DURATION_IN_SECS = "${TargetTPSDuration}"
                    env.TARGET_TPS_RAMP_PERIOD_IN_SECS = "${TargetTPSWarmupDuration}"

                    // Echo to console
                    echo("Target TPS Rate: ${env.TARGET_TPS}")
                    echo("Target TPS Duration: ${env.TARGET_TPS_DURATION_IN_SECS}")
                    echo("Target TPS Warmup Duration: ${env.TARGET_TPS_RAMP_PERIOD_IN_SECS}")

                    env.SPIKE_BASE_TPS = "${SpikeBaseRate}"
                    env.SPIKE_RAMP_DURATION = "${SpikeWarmupDuration}"
                    env.SPIKE_MAX_TPS = "${SpikeMaxRate}"
                    env.SPIKE_INTERVAL = "${SpikeInterval}"

                    // Echo to console
                    echo("Spike Base Rate: ${env.SPIKE_BASE_TPS}")
                    echo("Spike Warmup Duration: ${env.SPIKE_RAMP_DURATION}")
                    echo("Spike Max Rate: ${env.SPIKE_MAX_TPS}")
                    echo("Spike Interval: ${env.SPIKE_INTERVAL}")

                    env.FATIGUE_INITIAL_TARGET_TPS = "${FatigueTestInitialRate}"
                    env.FATIGUE_STEP_TPS_INCREASE = "${FatigueTestStepRateIncrease}"
                    env.FATIGUE_STEP_DURATION = "${FatigueStepDuration}"
                    env.FATIGUE_TOTAL_STEP_COUNT = "${FatigueStepCount}"

                    // Echo to console
                    echo("Fatigue Initial Rate: ${env.FATIGUE_INITIAL_TARGET_TPS}")
                    echo("Fatigue Step Rate Increase: ${env.FATIGUE_STEP_TPS_INCREASE}")
                    echo("Fatigue Step Duration: ${env.FATIGUE_STEP_DURATION}")
                    echo("Fatigue Step Count: ${env.FATIGUE_TOTAL_STEP_COUNT}")
                }
            }
        }
        stage('Load Test') {
            steps {
                script {
                    currentBuild.displayName = "${TEST_NAME}-${env.BUILD_NUMBER}"

                    def count = 0
                    for (int i = 0; i < numberOfTestNodes; i++) {
                        def num = i
                        def agentno = i+2 // start from jenkins-agent-2
                        testGroups["node $num"] = {
                            node("jenkins-agent-$agentno") {
                                // delete existing directory on node
                                deleteDir()
                                // checkout code from SCM
                                checkout scm
                                // make gradlew executable after SCM checkout
                                sh "chmod +x ./mvnw"
                                // build project up-front
                                sh "./mvnw -f ./load-testing/pom.xml clean gatling:enterprisePackage"
                                // let others know we are ready
                                count++
                                // wait until everyone is ready
                                waitUntil { count == numberOfTestNodes }
                                // execute the Gatling load test
                                sh(label: 'Run Gatling Scripts', script:  "./mvnw -f ./load-testing/pom.xml gatling:test -Dgatling.noReports=true -Dgatling.simulationClass=${env.SIMULATION_CLASS}")

                                def gatlingRunId = sh(returnStdout: true, script: 'cat ./load-testing/target/gatling/lastRun.txt | tr "\\n" " " | xargs')
                                echo "Gatling Run ID: ${gatlingRunId}"

                                def testDir = "./load-testing/target/gatling"
                                echo "Test dir: ${testDir}"

                                def reportDir = "${testDir}/${env.TEST_NAME}"
                                echo "Report dir: ${reportDir}"

                                echo "Will execute command: cp ${testDir}/${gatlingRunId}/simulation.log ${reportDir}/simulation-${num}.log"

                                sh "rm -rf ${testDir}"
                                sh "mkdir -p ${testDir}"
                                sh "cp ${testDir}/${gatlingRunId}/simulation.log ${reportDir}/simulation-${num}.log"

                                // sh "find . -name \\*.log -exec cp '{}' ./load-testing/target/gatling/${env.TEST_NAME}/simulation-${num}.log \\;"

                                // store the results for the master node to read later
                                stash name: "node $num", includes: '**/simulation*.log'
                            }
                        }
                    }
                    parallel testGroups
                }
            }
            post {
                always {
                    script {
                        def now = new Date()
                        def dt = now.format("yyyyMMddHHmm", TimeZone.getTimeZone("UTC"))

                        // make gradlew executable after SCM checkout
                        sh "chmod +x ./mvnw"

                        // clean previous runs
                        sh "./mvnw clean"

                        // unstash results from runners
                        script {
                            for (int i = 0; i < numberOfTestNodes; i++) {
                                def num = i
                                // unpacks to same directory on host
                                unstash "node $i"
                            }
                        }

                        // show current directory
                        sh "pwd"

                        // build reports
                        sh "./mvnw -f ./load-testing/pom.xml gatling:test -Dgatling.reportsOnly=${env.TEST_NAME}"

                        // move results to a directory containing a dash (required by Gatling archiver)
                        sh "mv ./load-testing/target/gatling/${env.TEST_NAME} ./load-testing/target/gatling/${env.TEST_NAME}-${dt}"

                        // archive the Gatling reports in Jenkins
                        gatlingArchive()
                    }
                }
            }
        }
    }



}