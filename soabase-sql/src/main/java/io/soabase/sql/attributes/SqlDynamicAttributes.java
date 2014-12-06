package io.soabase.sql.attributes;

import io.dropwizard.lifecycle.Managed;
import io.soabase.core.features.attributes.SoaDynamicAttributeListener;
import io.soabase.core.features.attributes.SoaDynamicAttributes;
import io.soabase.core.features.attributes.StandardAttributesContainer;
import io.soabase.core.listening.Listenable;
import org.apache.ibatis.session.SqlSession;
import java.util.Collection;
import java.util.List;

public class SqlDynamicAttributes implements SoaDynamicAttributes, Managed
{
    private final StandardAttributesContainer container;
    private final SqlSession session;

    public SqlDynamicAttributes(SqlSession session, List<String> scopes)
    {
        this.session = session;
        container = new StandardAttributesContainer(scopes);
    }

    @Override
    public String getAttribute(String key)
    {
        return container.getAttribute(key, null);
    }

    @Override
    public String getAttribute(String key, String defaultValue)
    {
        return container.getAttribute(key, defaultValue);
    }

    @Override
    public boolean getAttributeBoolean(String key)
    {
        return container.getAttributeBoolean(key, false);
    }

    @Override
    public boolean getAttributeBoolean(String key, boolean defaultValue)
    {
        return container.getAttributeBoolean(key, defaultValue);
    }

    @Override
    public int getAttributeInt(String key)
    {
        return container.getAttributeInt(key, 0);
    }

    @Override
    public int getAttributeInt(String key, int defaultValue)
    {
        return container.getAttributeInt(key, defaultValue);
    }

    @Override
    public long getAttributeLong(String key)
    {
        return container.getAttributeLong(key, 0);
    }

    @Override
    public long getAttributeLong(String key, long defaultValue)
    {
        return container.getAttributeLong(key, defaultValue);
    }

    @Override
    public void temporaryOverride(String key, boolean value)
    {
        container.temporaryOverride(key, value);
    }

    @Override
    public void temporaryOverride(String key, int value)
    {
        container.temporaryOverride(key, value);
    }

    @Override
    public void temporaryOverride(String key, long value)
    {
        container.temporaryOverride(key, value);
    }

    @Override
    public void temporaryOverride(String key, String value)
    {
        container.temporaryOverride(key, value);
    }

    @Override
    public boolean removeOverride(String key)
    {
        return container.removeOverride(key);
    }

    @Override
    public Collection<String> getKeys()
    {
        return container.getKeys();
    }

    @Override
    public Listenable<SoaDynamicAttributeListener> getListenable()
    {
        return container.getListenable();
    }

    @Override
    public void start() throws Exception
    {
        update();
    }

    @Override
    public void stop() throws Exception
    {
        // NOP
    }

    synchronized void update()
    {
        StandardAttributesContainer.Updater updater = container.newUpdater();
        AttributeEntityMapper mapper = session.getMapper(AttributeEntityMapper.class);
        for ( AttributeEntity entity : mapper.selectAll() )
        {
            updater.put(entity.getfKEY(), entity.getfSCOPE(), entity.getfVALUE());
        }
        updater.commit();
    }
}