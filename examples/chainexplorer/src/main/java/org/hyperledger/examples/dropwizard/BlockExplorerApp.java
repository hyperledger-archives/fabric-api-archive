/**
 * Copyright 2016 Digital Asset Holdings, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperledger.examples.dropwizard;

import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.hyperledger.api.BCSAPI;
import org.hyperledger.dropwizard.HyperLedgerBundle;
import org.hyperledger.dropwizard.HyperLedgerConfiguration;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.security.Security;
import java.util.EnumSet;

/**
 *
 */
public class BlockExplorerApp extends Application<BlockExplorerConfiguration> {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private HyperLedgerBundle<BlockExplorerConfiguration> hyperLedgerBundle;

    public static void main(String[] args) throws Exception {
        new BlockExplorerApp().run(args);
    }

    @Override
    public void initialize(Bootstrap<BlockExplorerConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor()
                )
        );
        hyperLedgerBundle = new HyperLedgerBundle<BlockExplorerConfiguration>() {
            @Override
            protected HyperLedgerConfiguration getSupernodeConfiguration(BlockExplorerConfiguration configuration) {
                return configuration.getHyperLedger();
//                return configuration.getGRPCConnectedHyperLedger();
            }
        };
        bootstrap.addBundle(hyperLedgerBundle);
        bootstrap.getObjectMapper().registerModule(new JSR310Module());
    }

    @Override
    public void run(BlockExplorerConfiguration configuration, Environment environment) throws Exception {
        BCSAPI api = hyperLedgerBundle.getBCSAPI();
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "*");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        environment.jersey().register(new ExplorerResource(api));
    }
}
