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
package io.soabase.guice;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Servlet scopes.
 *
 * mostly copied from GuiceServlet
 */
public class ServletScopes
{
    private ServletScopes()
    {
    }

    /**
     * A sentinel attribute value representing null.
     */
    enum NullObject
    {
        INSTANCE
    }

    /**
     * HTTP servlet request scope.
     */
    public static final Scope REQUEST = new Scope()
    {
        public <T> Provider<T> scope(Key<T> key, final Provider<T> creator)
        {
            final String name = key.toString();
            return new Provider<T>()
            {
                public T get()
                {
                    InternalFilter filter = Preconditions.checkNotNull(InternalFilter.get(), "Internal filter not set!");
                    HttpServletRequest request = filter.getServletRequest();
                    //noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized(request)
                    {
                        Object obj = request.getAttribute(name);
                        if ( NullObject.INSTANCE == obj )
                        {
                            return null;
                        }
                        @SuppressWarnings("unchecked")
                        T t = (T)obj;
                        if ( t == null )
                        {
                            t = creator.get();
                            request.setAttribute(name, (t != null) ? t : NullObject.INSTANCE);
                        }
                        return t;
                    }
                }

                public String toString()
                {
                    return String.format("%s[%s]", creator, REQUEST);
                }
            };
        }

        public String toString()
        {
            return "ServletScopes.REQUEST";
        }
    };

    /**
     * HTTP session scope.
     */
    public static final Scope SESSION = new Scope()
    {
        public <T> Provider<T> scope(Key<T> key, final Provider<T> creator)
        {
            final String name = key.toString();
            return new Provider<T>()
            {
                public T get()
                {
                    InternalFilter filter = Preconditions.checkNotNull(InternalFilter.get(), "Internal filter not set!");
                    HttpSession session = filter.getServletRequest().getSession();
                    //noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized(session)
                    {
                        Object obj = session.getAttribute(name);
                        if ( NullObject.INSTANCE == obj )
                        {
                            return null;
                        }
                        @SuppressWarnings("unchecked")
                        T t = (T)obj;
                        if ( t == null )
                        {
                            t = creator.get();
                            session.setAttribute(name, (t != null) ? t : NullObject.INSTANCE);
                        }
                        return t;
                    }
                }

                public String toString()
                {
                    return String.format("%s[%s]", creator, SESSION);
                }
            };
        }

        public String toString()
        {
            return "ServletScopes.SESSION";
        }
    };
}
