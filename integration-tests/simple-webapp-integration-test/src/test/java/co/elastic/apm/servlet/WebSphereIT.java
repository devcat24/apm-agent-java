/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 - 2019 Elastic and contributors
 * %%
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
 * #L%
 */
package co.elastic.apm.servlet;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.Arrays;
import java.util.Collections;

@RunWith(Parameterized.class)
public class WebSphereIT extends AbstractServletContainerIntegrationTest {

    public WebSphereIT(final String version) {
        super(new GenericContainer<>(
              /* uncomment to debug (waits for debugger to attach)
              new ImageFromDockerfile()
                    .withDockerfileFromBuilder(builder -> builder
                        .from("websphere-liberty:" + version)
                        .cmd("/opt/ibm/wlp/bin/server", "debug", "defaultServer")
              */
            "websphere-liberty:" + version
        )
            .withNetwork(Network.SHARED)
            .withEnv("JVM_ARGS", "-javaagent:/elastic-apm-agent.jar")
            // uncomment for debugging
            //.withEnv("JVM_ARGS", "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005")
            .withEnv("ELASTIC_APM_SERVER_URL", "http://apm-server:1080")
            .withEnv("ELASTIC_APM_IGNORE_URLS", "/status*,/favicon.ico")
            .withEnv("ELASTIC_APM_REPORT_SYNC", "true")
            .withEnv("ELASTIC_APM_LOGGING_LOG_LEVEL", "DEBUG")
            .withLogConsumer(new StandardOutLogConsumer().withPrefix("websphere"))
            .withFileSystemBind(pathToWar, "/config/dropins/simple-webapp.war")
            .withFileSystemBind(pathToJavaagent, "/elastic-apm-agent.jar")
            .withExposedPorts(9080, 7777),
            9080,
            "/simple-webapp",
            "websphere-application",
            "/config/dropins");
    }

    @Parameterized.Parameters(name = "WebSphere {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{{"8.5.5"}, {"webProfile7"}});
    }

    @Override
    protected boolean isExpectedStacktrace(String path) {
        return true;
    }

    @Override
    protected Iterable<TestApp> getTestApps() {
        return Collections.singletonList(TestApp.JSF);
    }
}
