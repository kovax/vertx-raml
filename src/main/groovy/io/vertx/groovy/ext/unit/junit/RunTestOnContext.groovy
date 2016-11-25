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
package io.vertx.groovy.ext.unit.junit

import java.util.HashMap
import java.util.concurrent.CountDownLatch
import java.util.function.BiConsumer

import org.junit.rules.TestRule


/**
 * @author kovax
 *
 */
//@CompileStatic
class RunTestOnContext implements TestRule {

  private volatile Vertx vertx;
  private final Supplier<Vertx> createVertx;
  private final BiConsumer<Vertx, CountDownLatch> closeVertx;

  /**
   * Create a new rule managing a Vertx instance created with default options. The Vert.x instance
   * is created and closed for each test.
   */
  public RunTestOnContext() {
    this(new HashMap<String, Object>());
  }

  /**
   * Create a new rule managing a Vertx instance created with specified options. The Vert.x instance
   * is created and closed for each test.
   *
   * @param options the vertx options
   */
  public RunTestOnContext(Map<String, Object> options) {
//    this(() -> Vertx.vertx(options));
    this( { Vertx.vertx(options) } );
  }

  /**
   * Create a new rule with supplier/consumer for creating/closing a Vert.x instance. The lambda are invoked for each
   * test. The {@code closeVertx} lambda should invoke the consumer with null when the {@code vertx} instance is closed.
   *
   * @param createVertx the create Vert.x supplier
   * @param closeVertx the close Vert.x consumer
   */
  public RunTestOnContext(Supplier<Vertx> createVertx, BiConsumer<Vertx, Consumer<Void>> closeVertx) {
    this.createVertx = createVertx;
//    this.closeVertx = (vertx, latch) -> closeVertx.accept(vertx, v -> latch.countDown());
    this.closeVertx = { Vertx vertx, CountDownLatch latch -> closeVertx.accept(vertx, { latch.countDown() }) };
  }

  /**
   * Create a new rule with supplier for creating a Vert.x instance. The lambda are invoked for each
   * test.
   *
   * @param createVertx the create Vert.x supplier
   */
  public RunTestOnContext(Supplier<Vertx> createVertx) {
    this(createVertx, (vertx, latch) -> vertx.close(ar -> latch.accept(null)));
  }

  /**
   * Retrieves the current Vert.x instance, this value varies according to the test life cycle.
   *
   * @return the vertx object
   */
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new VertxUnitStatement(() -> vertx != null ? vertx.getOrCreateContext() : null, base) {
      @Override
      public void evaluate() throws Throwable {
        vertx = createVertx.get();
        try {
          super.evaluate();
        } finally {
          CountDownLatch latch = new CountDownLatch(1);
          closeVertx.accept(vertx, latch);
          try {
            if (!latch.await(30 * 1000, TimeUnit.MILLISECONDS)) {
              Logger logger = LoggerFactory.getLogger(description.getTestClass());
              logger.warn("Could not close Vert.x in tme");
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    };
  }
}
