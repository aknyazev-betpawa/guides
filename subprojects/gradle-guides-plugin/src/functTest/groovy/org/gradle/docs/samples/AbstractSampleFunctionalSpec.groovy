/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.docs.samples

import org.gradle.docs.AbstractFunctionalTest
import org.gradle.docs.TestFile
import org.gradle.testkit.runner.BuildResult

import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import static org.hamcrest.CoreMatchers.containsString

abstract class AbstractSampleFunctionalSpec extends AbstractFunctionalTest {
    protected static final SKIPPED_TASK_OUTCOMES = [FROM_CACHE, UP_TO_DATE, SKIPPED, NO_SOURCE]

    protected static void writeReadmeTo(TestFile directory) {
        directory.file("README.adoc") << """
= Demo Sample

Some doc
"""
    }

    protected static void writeGroovyDslSample(TestFile sampleDirectory) {
        writeGroovyDslSampleToDirectory(sampleDirectory.file("groovy"))
    }

    protected static void writeGroovyDslSampleToDirectory(TestFile directory) {
        directory.file("build.gradle") << """
// tag::println[]
println "Hello, world!"
// end:println[]
"""
        directory.file("settings.gradle") << """
// tag::root-project-name[]
rootProject.name = 'demo'
// end:root-project-name[]
"""
    }

    protected static void writeKotlinDslSample(TestFile sampleDirectory) {
        writeKotlinDslSampleToDirectory(sampleDirectory.file("kotlin"))
    }

    protected static void writeKotlinDslSampleToDirectory(TestFile directory) {
        directory.file("build.gradle.kts") << """
// tag::println[]
println("Hello, world!")
// end:println[]
        """
        directory.file("settings.gradle.kts") << """
// tag::root-project-name[]
rootProject.name = "demo"
// end:root-project-name[]
        """
    }

    protected TestFile getGroovyDslZipFile() {
        return file("build/sample-zips/sample_demo-groovy-dsl.zip")
    }

    protected TestFile getKotlinDslZipFile() {
        return file("build/sample-zips/sample_demo-kotlin-dsl.zip")
    }

    protected static String getSampleUnderTestDsl() {
        return "samples.publishedSamples.demo"
    }

    protected static void assertBothDslSampleTasksSkipped(BuildResult result) {
        assertCommonSampleTasksSkipped(result)
        assertDslSampleTasksSkipped(result, "Groovy")
        assertDslSampleTasksSkipped(result, "Kotlin")
    }

    protected static void assertBothDslSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertCommonSampleTasksExecutedAndNotSkipped(result)
        assertDslSampleTasksExecutedAndNotSkipped(result, "Groovy")
        assertDslSampleTasksExecutedAndNotSkipped(result, "Kotlin")
    }

    protected static void assertOnlyGroovyDslTasksExecutedAndNotSkipped(BuildResult result) {
        assertCommonSampleTasksExecutedAndNotSkipped(result)
        assertDslSampleTasksExecutedAndNotSkipped(result, "Groovy")
        assertDslSampleTasksNotExecuted(result, "Kotlin")
    }

    protected static void assertOnlyKotlinDslTasksExecutedAndNotSkipped(BuildResult result) {
        assertCommonSampleTasksExecutedAndNotSkipped(result)
        assertDslSampleTasksExecutedAndNotSkipped(result, "Kotlin")
        assertDslSampleTasksNotExecuted(result, "Groovy")
    }

    private static void assertDslSampleTasksNotExecuted(BuildResult result, String dsl) {
        assert result.task(":zipSampleDemo${dsl}") == null
    }

    protected static void assertDslSampleTasksExecutedAndNotSkipped(BuildResult result, String dsl) {
        assert result.task(":zipSampleDemo${dsl}").outcome == SUCCESS
    }

    protected static void assertDslSampleTasksSkipped(BuildResult result, String dsl) {
        assert result.task(":zipSampleDemo${dsl}").outcome in SKIPPED_TASK_OUTCOMES
    }

    private static void assertCommonSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assert result.task(":generateDemoPage").outcome == SUCCESS
        assert result.task(":assembleDemoSample").outcome == SUCCESS
    }

    private static void assertCommonSampleTasksSkipped(BuildResult result) {
        assert result.task(":generateDemoPage").outcome in SKIPPED_TASK_OUTCOMES
        assert result.task(":assembleDemoSample").outcome in SKIPPED_TASK_OUTCOMES
    }

    protected static void assertGradleWrapperVersion(TestFile file, String expectedGradleVersion) {
        file.asZip().assertDescendantHasContent('gradle/wrapper/gradle-wrapper.properties', containsString("-${expectedGradleVersion}-"))
    }

    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }
            repositories {
                jcenter()
                maven {
                    url = "https://repo.gradle.org/gradle/libs-releases"
                }
            }

            samples {
                publishedSamples {
                    demo
                }
            }
        """
    }

    protected void writeSampleUnderTest(String directory="src/samples/demo") {
        writeReadmeTo(file(directory))
        writeGroovyDslSample(file(directory))
        writeKotlinDslSample(file(directory))
    }
}
