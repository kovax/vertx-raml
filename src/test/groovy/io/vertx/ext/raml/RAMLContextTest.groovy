/**
 * This file is part of the CRISTAL-iSE kernel.
 * Copyright (c) 2001-2015 The CRISTAL Consortium. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * http://www.fsf.org/licensing/licenses/lgpl.html
 */
package io.vertx.ext.raml;

import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.http.HttpClient
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.http.HttpClientResponse
import io.vertx.groovy.ext.unit.Async
import io.vertx.groovy.ext.unit.TestContext
import io.vertx.groovy.ext.unit.junit.VertxUnitRunner

import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.fail

/**
 * @author kovax
 *
 */
@RunWith(VertxUnitRunner.class)
//@CompileStatic
public class RAMLContextTest {

    @Rule
    public final RunTestOnContext rule = new RunTestOnContext(Vertx.&vertx);
    
    
    String ramlHeader = '''
#%RAML 0.8
title: VERTX REST Test API
version: v0.1
baseUri: https://localhost/
mediaType: application/json'''
    
    @BeforeClass
    public static void beforeClass() {
        //System.properties.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp(TestContext context) throws Exception {
        //context.assertTrue(Context.isOnEventLoopThread());
        JsonObject config = new JsonObject().put("ramlString", ramlHeader);
        Async async = context.async();
        rule.vertx().deployVerticle(RAMLRouter.class.getName(), new DeploymentOptions().setConfig(config)) { AsyncResult<String> res ->
            if (res.succeeded()) {
                async.complete()
            }
            else {
                fail("Could not deploy RAMLRouter")
            }
        };
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown(TestContext context) throws Exception {
        rule.vertx().close()
    }

    @Test
    public void test(TestContext context) {
        HttpClient client = rule.vertx().createHttpClient();
        Async async = context.async();
        
        client.getNow(8888, "localhost", "/") { HttpClientResponse resp ->
            context.assertEquals(400, resp.statusCode(), "Wrong status code")
            println "Status code: " + resp.statusCode()
            
            //resp.bodyHandler() { Buffer body -> context.assertEquals("foo", body.toString("UTF-8")) };
            
            client.close();
            async.complete();
        };
    }

}
