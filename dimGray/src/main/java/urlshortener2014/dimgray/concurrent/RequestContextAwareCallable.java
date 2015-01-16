package urlshortener2014.dimgray.concurrent;

import java.util.concurrent.Callable;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


/**
 * Superclase que soluciona el problema del contexto de los ThreadPool en Spring.
 * @author csamuel	
 * @link http://stackoverflow.com/questions/1528444/accessing-scoped-proxy-beans-within-threads-of
 *
 * @param <V> par�metro gen�rico que devuelve el m�todo call() de la clase.
 */
public abstract class RequestContextAwareCallable<V> implements Callable<V> {

    private final RequestAttributes requestAttributes;
    private Thread thread;

    /**
     * M�todo constructor de la clase.
     */
    public RequestContextAwareCallable() {
        this.requestAttributes = RequestContextHolder.getRequestAttributes();
        this.thread = Thread.currentThread();
    }

    /**
     * M�todo que se encarga de solucionar el problema del contexto de Spring.
     */
    public V call() {
        try {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            return onCall();
        } finally {
            if (Thread.currentThread() != thread) {
                RequestContextHolder.resetRequestAttributes();
            }
            thread = null;
        }
    }

    /**
     * M�todo para implementar para la clase que extienda esta clase.
     * @return un par�metro gen�rico V.
     */
    public abstract V onCall();
}
