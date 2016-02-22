package net.serenitybdd.modules.utils

/**
 * User: YamStranger
 * Date: 2/20/16
 * Time: 2:51 PM
 */
enum Projects {

    SERENITY_TEST_PROJECTS("serenity-test-projects"),
    SERENITY_JBEHAVE("serenity-jbehave"),
    SERENITY_CUCUMBER("serenity-cucumber"),
    SERENITY_CORE("serenity-core"),
    SERENITY_JIRA("serenity-jira"),
    SERENITY_MAVEN_PLUGIN("serenity-maven-plugin"),
    WEB_TODOMVC_TESTS("$SERENITY_TEST_PROJECTS/web-todomvc-tests")

    def final private String name

    public Projects(def name) {
        this.name = name
    }

    @Override
    String toString() {
        return name
    }
}
