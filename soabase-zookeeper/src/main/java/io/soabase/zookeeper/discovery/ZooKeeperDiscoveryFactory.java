/**
 * Copyright 2014 Jordan Zimmerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.soabase.zookeeper.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.soabase.core.SoaBundle;
import io.soabase.core.SoaFeatures;
import io.soabase.core.SoaInfo;
import io.soabase.core.features.config.ComposedConfigurationAccessor;
import io.soabase.core.features.config.SoaConfiguration;
import io.soabase.core.features.discovery.Discovery;
import io.soabase.core.features.discovery.DiscoveryFactory;
import org.apache.curator.framework.CuratorFramework;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@JsonTypeName("zookeeper")
public class ZooKeeperDiscoveryFactory implements DiscoveryFactory
{
    private String bindAddress;

    @NotEmpty
    @Pattern(regexp = "/..*")
    private String zookeeperPath = "/discovery";

    @JsonProperty("bindAddress")
    public String getBindAddress()
    {
        return bindAddress;
    }

    @JsonProperty("bindAddress")
    public void setBindAddress(String bindAddress)
    {
        this.bindAddress = bindAddress;
    }

    @JsonProperty("zookeeperPath")
    public String getZookeeperPath()
    {
        return zookeeperPath;
    }

    @JsonProperty("zookeeperPath")
    public void setZookeeperPath(String zookeeperPath)
    {
        this.zookeeperPath = zookeeperPath;
    }

    @Override
    public Discovery build(Configuration configuration, Environment environment, SoaInfo soaInfo)
    {
        SoaFeatures features = SoaBundle.getFeatures(environment);
        CuratorFramework curatorFramework = features.getNamedRequired(CuratorFramework.class, SoaFeatures.DEFAULT_NAME);
        SoaConfiguration soaConfiguration = ComposedConfigurationAccessor.access(configuration, environment, SoaConfiguration.class);
        return new ZooKeeperDiscovery(curatorFramework, this, soaInfo, environment, soaConfiguration.getDeploymentGroups());
    }
}
