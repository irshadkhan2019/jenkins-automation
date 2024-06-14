import groovy.json.JsonOutput

// Get commiter username
def readCommitAuthor() {
    sh '''#!/bin/bash
        git rev-parse HEAD | tr '\n' ' ' > gitCommit
        git show --format="%aN <%aE>" ${gitCommit} | head -1 | tr '\n' ' ' > gitCommitAuthor
    '''
    return readFile('gitCommitAuthor')
}

def durationTime(m1, m2) {
    int timecase = m2 - m1

    int seconds = (int) (timecase / 1000)
    int minutes = (int) (timecase / (60*1000))
    int hours = (int) (timecase / (1000*60*60))

    return hours.mod(24) + "h " + minutes.mod(60) + "m " + seconds.mod(60) + "s"
}

// get pods name of the service eg .based on app=jobber-review ,get all pods where it is running
def findPodsFromName(String namespace, String name) {
    podsAndImagesRaw = sh(
        script: """
            kubectl get pods -n ${namespace} --selector=app=${name} -o jsonpath='{range .items[*]}{.metadata.name}###'
        """,
        returnStdout: true
    ).trim()
    wantedPods = podsAndImagesRaw.split('###')

    return wantedPods //returna array of pod names
}

// notify slack about CICD success or Failure
def notifySlack(text, channel, attachments) {
    //  slack webhook url and token
    def slackURL = 'https://hooks.slack.com/services/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'
    def jenkinsIcon = 'https://a.slack-edge.com/205a/img/services/jenkins-ci_72.png'

    def payload = JsonOutput.toJson([
        text: text,
        channel: channel,
        username: "jenkins",
        icon_url: jenkinsIcon,
        attachments: attachments
    ])

    sh "curl -s -X POST ${slackURL} -H 'Cache-Control: no-cache' -H 'Content-Type: application/json;charset=UTF-8' -d '${payload}'"
}

return this