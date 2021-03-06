package de.herrlock.manga.http.jetty;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.util.log.Log;

import de.herrlock.manga.exceptions.MDException;
import de.herrlock.manga.http.jetty.handlers.MangaBaseHandler;
import de.herrlock.manga.http.jetty.log.Log4j2Bridge;

public final class JettyServer {
    private static final Logger logger = LogManager.getLogger();

    /**
     * This must be kept a secret !!!1!!11
     */
    private static final String SUPER_SECRET_MAGIC_TOKEN_FOR_SHUTDOWN = "avadakedavra";

    private final Server server;

    public JettyServer() {
        this( 1905 );
    }

    public JettyServer( final int port ) {
        Log.setLog( new Log4j2Bridge( JettyServer.class.getName() ) );
        this.server = new Server( port );
        this.server.setHandler( createHandlers() );
    }

    public void start() throws MDException {
        try {
            this.server.start();
        } catch ( Exception ex ) {
            throw new MDException( "Starting the server failed.", ex );
        }
    }

    public void stop() throws MDException {
        try {
            this.server.stop();
        } catch ( Exception ex ) {
            throw new MDException( "Stopping the server failed.", ex );
        }
    }

    /**
     * Wait for the server to be stopped. Checks all 2 seconds if either 'q' can be read from System.in or the server's
     * shutdown-mechanism was triggered.
     */
    public void listenForStop() {
        boolean active = true;
        boolean sysinIsOpen = true;
        try {
            System.in.available();
            logger.info( "Enter 'q' to quit" );
        } catch ( IOException ex ) {
            logger.debug( "System.in threw Exception: ", ex );
            sysinIsOpen = false;
        }
        while ( active ) {
            logger.debug( "Server active" );
            boolean serverStopped = this.server.isStopping() || this.server.isStopped();
            logger.debug( "serverStopped: {}", serverStopped );
            boolean quitBySysin = sysinIsOpen && getStopFromSysin();
            logger.debug( "quitBySysin: {}", quitBySysin );
            if ( serverStopped || quitBySysin ) {
                active = false;
            } else {
                try {
                    Thread.sleep( 2000 );
                } catch ( InterruptedException ex ) {
                    logger.error( ex );
                }
            }
        }
        logger.info( "Server stopped" );
    }

    private boolean getStopFromSysin() {
        logger.traceEntry();
        boolean quitBySysin = false;
        try {
            if ( System.in.available() > 0 ) {
                int read = System.in.read();
                quitBySysin = read == 'q';
                logger.info( "Read char: {}", ( char ) read );
            }
        } catch ( IOException ex ) {
            // System.in is closed, this might happen when called from javaw
            logger.info( ex );
        }
        return quitBySysin;
    }

    private Handler createHandlers() {
        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers( new Handler[] {
            createMangaBaseHandler(), // handle the manga-related requests
            createShutdownHandler(), // handle the shutdown-requests
            createResourceHandler(), // handle the file-requests
            new DefaultHandler() // handle the left requests
        } );
        return handlerList;
    }

    private Handler createMangaBaseHandler() {
        ContextHandler contextHandler = new ContextHandler( "j" );
        contextHandler.setHandler( new MangaBaseHandler() );
        return contextHandler;
    }

    private Handler createShutdownHandler() {
        return new ShutdownHandler( SUPER_SECRET_MAGIC_TOKEN_FOR_SHUTDOWN, false, true );
    }

    private Handler createResourceHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        // load from the jetty-folder
        resourceHandler.setResourceBase( "jetty" );
        // enable caching via etags
        resourceHandler.setEtags( true );
        return resourceHandler;
    }

}
