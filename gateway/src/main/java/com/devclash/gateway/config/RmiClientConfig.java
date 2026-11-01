package com.devclash.gateway.config;

import com.devclash.common.rmi.QuizRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Spring Boot Configuration for Java RMI Client Lookup.
 * Connects to the standalone Java RMI Quiz Server using JRMP over TCP/IP.
 */
@Configuration
public class RmiClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RmiClientConfig.class);

    @Value("${devclash.rmi.host:${RMI_SERVER_HOST:${RMI_HOST:localhost}}}")
    private String rmiHost;

    @Value("${devclash.rmi.port:${RMI_SERVER_PORT:${RMI_PORT:1099}}}")
    private int rmiPort;

    @Value("${devclash.rmi.service-name:${RMI_SERVICE_NAME:DevClashQuizService}}")
    private String serviceName;

    /**
     * Creates a resilient dynamic proxy for QuizRemote.
     * Looks up the remote stub in the RMI Registry on demand, allowing the Gateway
     * to reconnect smoothly even if the RMI server restarts.
     */
    @Bean
    public QuizRemote quizRemote() {
        log.info("[Gateway RMI] Initializing RMI Client Proxy for rmi://{}:{}/{}", rmiHost, rmiPort, serviceName);

        return (QuizRemote) Proxy.newProxyInstance(
                QuizRemote.class.getClassLoader(),
                new Class<?>[]{QuizRemote.class},
                (proxy, method, args) -> {
                    try {
                        Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
                        QuizRemote remoteStub = (QuizRemote) registry.lookup(serviceName);
                        return method.invoke(remoteStub, args);
                    } catch (InvocationTargetException ite) {
                        Throwable target = ite.getTargetException();
                        log.error("[Gateway RMI] Remote method '{}' failed on RMI Server: {}",
                                method.getName(), target.getMessage());
                        if (target instanceof RemoteException) {
                            throw target;
                        }
                        throw new RemoteException("RMI Execution Error in " + method.getName(), target);
                    } catch (Exception e) {
                        log.error("[Gateway RMI] Communication failure with RMI Registry at {}:{}/{} - {}",
                                rmiHost, rmiPort, serviceName, e.getMessage());
                        throw new RemoteException("Could not connect to DevClash RMI Server: " + e.getMessage(), e);
                    }
                }
        );
    }
}
