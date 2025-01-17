/*
 *  Copyright 2019 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.spock2.samples

import io.qameta.allure.*
import spock.lang.Specification

/**
 * @author charlie (Dmitry Baev).
 */
class TestWithAnnotations extends Specification {

    @Epic("epic1")
    @Epics([
            @Epic("epic2"),
            @Epic("epic3")
    ])
    @Features([
            @Feature("feature1"),
            @Feature("feature2")
    ])
    @Feature("feature3")
    @Story("story1")
    @Stories([
            @Story("story2"),
            @Story("story3")]
    )
    @Owner("some-owner")
    @Flaky
    @Muted
    def "someTest"() {
        expect:
        true
    }
}
