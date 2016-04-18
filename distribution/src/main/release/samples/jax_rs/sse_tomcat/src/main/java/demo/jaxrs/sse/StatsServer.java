/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package demo.jaxrs.sse;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.cxf.cdi.CXFCdiServlet;
import org.apache.cxf.jaxrs.sse.servlet.CXFSseServlet;
import org.jboss.weld.environment.servlet.Listener;

public final class StatsServer {
    private StatsServer() {
    }
    
    public static void main(final String[] args) throws Exception {
        // Register and map the dispatcher servlet
        final File base = new File(System.getProperty("java.io.tmpdir"));
        
        final Tomcat server = new Tomcat();
        server.setPort(8686);
        server.setBaseDir(base.getAbsolutePath());
        server.enableNaming();
        
        final StandardContext context = (StandardContext)server.addWebapp("/", base.getAbsolutePath());
        context.setConfigFile(StatsServer.class.getResource("/META-INF/context.xml"));
        context.addApplicationListener(Listener.class.getName());
        context.setParentClassLoader(Thread.currentThread().getContextClassLoader());

        final Wrapper wrapper = Tomcat.addServlet(context, "cxfSseServlet", new CXFSseServlet(new CXFCdiServlet()));
        wrapper.setAsyncSupported(true);
        context.addServletMapping("/rest/*", "cxfSseServlet");

        final Context staticContext = server.addWebapp("/static", base.getAbsolutePath());
        final File additionWebInfClasses = new File("target/classes/web-ui");
        final WebResourceRoot resources = new StandardRoot(staticContext);
        resources.addPreResources(new DirResourceSet(resources, "/", additionWebInfClasses.getAbsolutePath(), "/"));

        Tomcat.addServlet(staticContext, "cxfStaticServlet", new DefaultServlet());
        staticContext.addServletMapping("/*", "cxfStaticServlet");
        staticContext.setResources(resources);
        staticContext.setParentClassLoader(Thread.currentThread().getContextClassLoader());       
        
        server.start();
        server.getServer().await();
    }
}

