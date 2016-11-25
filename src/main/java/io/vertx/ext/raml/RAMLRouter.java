package io.vertx.ext.raml;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;

/**
 *
 */
@Slf4j
public class RAMLRouter extends AbstractVerticle {

    private Raml getRAML() throws IOException {
        String ramlString = config().getString("ramlString");

        if(ramlString == null || "".equals(ramlString)) {
            log.debug("Reading ramlFile config!");

            String ramlFile = config().getString("ramlFile", "conf/default.raml");
            ramlString = FileUtils.readFileToString(new File(ramlFile));
        }

        return new RamlDocumentBuilder().build(ramlString, "");
    }

    /**
     * 
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Raml raml = getRAML();

        log.debug("Title: "+raml.getTitle());
//        List<Protocol> prorocols = raml.getProtocols();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
//        router.get("/products/:productID").handler(this::handleGetProduct);
//        router.put("/products/:productID").handler(this::handleAddProduct);
//        router.get("/products").handler(this::handleListProducts);

        vertx.createHttpServer().requestHandler(router::accept).listen( config().getInteger("port", 8888), res -> {
            if (res.succeeded()) {
                log.debug("HTTP Server was started");
                startFuture.complete();
            }
            else {
                startFuture.fail("Could not start HTTP Server");
            }
        });
    }

}
