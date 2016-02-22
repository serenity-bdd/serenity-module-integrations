package net.serenitybdd.modules.utils

import net.thucydides.core.reports.OutcomeFormat
import net.thucydides.core.reports.TestOutcomeLoader
import net.thucydides.core.reports.TestOutcomes

/**
 * User: YamStranger
 * Date: 2/20/16
 * Time: 1:11 PM
 */
class Project {
    def String name
    def File root
    def File reports
    def Map<String, Project> modules

    def public Project(def String name, def File root) {
        this.name = name
        this.root = root
        this.reports = reports
        modules = new HashMap<>()
    }

    def public TestOutcomes reports(def OutcomeFormat format) {
        TestOutcomeLoader.loadTestOutcomes().inFormat(format).from(reports);
    }
}
