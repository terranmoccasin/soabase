package io.soabase.zookeeper.discovery;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Iterables;
import io.dropwizard.lifecycle.Managed;
import io.soabase.core.features.discovery.SoaDiscovery;
import io.soabase.core.features.discovery.SoaDiscoveryInstance;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import java.util.concurrent.TimeUnit;

// TODO
public class ZooKeeperDiscovery extends CacheLoader<String, ServiceProvider<Void>> implements SoaDiscovery, Managed, RemovalListener<String, ServiceProvider<Void>>
{
    private final ServiceDiscovery<Void> discovery;
    private final LoadingCache<String, ServiceProvider<Void>> providers;

    public ZooKeeperDiscovery(CuratorFramework curator, ZooKeeperDiscoveryFactory factory)
    {
        providers = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)  // TODO config
            .removalListener(this)
            .build(this);

        try
        {
            discovery = ServiceDiscoveryBuilder
                .builder(Void.class)
                .basePath("/")  // TODO
                .client(curator)
                .thisInstance(ServiceInstance.<Void>builder().build())  // TODO
                .build();
        }
        catch ( Exception e )
        {
            // TODO logging
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServiceProvider<Void> load(String serviceName) throws Exception
    {
        // TODO - other values
        ServiceProvider<Void> provider = discovery.serviceProviderBuilder().serviceName(serviceName).build();
        provider.start();
        return provider;
    }

    @Override
    public void onRemoval(RemovalNotification<String, ServiceProvider<Void>> notification)
    {
        CloseableUtils.closeQuietly(notification.getValue());
    }

    @Override
    public SoaDiscoveryInstance getInstance(String serviceName)
    {
        try
        {
            ServiceProvider<Void> provider = providers.get(serviceName);
            ServiceInstance<Void> instance = provider.getInstance();
            if ( instance.getPort() != null )
            {
                return new SoaDiscoveryInstance(instance.getAddress(), instance.getPort(), false);
            }
            if ( instance.getSslPort() != null )
            {
                return new SoaDiscoveryInstance(instance.getAddress(), instance.getSslPort(), true);
            }
            return new SoaDiscoveryInstance(instance.getAddress(), 0, true);
        }
        catch ( Exception e )
        {
            // TODO logging
            throw new RuntimeException(e);
        }
    }

    @Override
    public void noteError(String serviceName, final SoaDiscoveryInstance errorInstance)
    {
        ServiceProvider<Void> provider = providers.getUnchecked(serviceName);
        if ( provider != null )
        {
            try
            {
                ServiceInstance<Void> foundInstance = Iterables.find
                    (
                        provider.getAllInstances(),
                        new Predicate<ServiceInstance<Void>>()
                        {
                            @Override
                            public boolean apply(ServiceInstance<Void> instance)
                            {
                                if ( instance.getAddress().equals(errorInstance.getHost()) )
                                {
                                    //noinspection SimplifiableIfStatement
                                    if ( errorInstance.getPort() != 0 )
                                    {
                                        return errorInstance.isForceSsl() ? (errorInstance.getPort() == instance.getSslPort()) : (errorInstance.getPort() == instance.getPort());
                                    }
                                    return true;
                                }
                                return false;
                            }
                        },
                        null
                    );
                if ( foundInstance != null )
                {
                    provider.noteError(foundInstance);
                }
            }
            catch ( Exception e )
            {
                // TODO logging
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void start() throws Exception
    {
        discovery.start();
    }

    @Override
    public void stop() throws Exception
    {
        providers.invalidateAll();
        CloseableUtils.closeQuietly(discovery);
    }
}