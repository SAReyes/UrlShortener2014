package urlshortener2014.dimgray.concurrent;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import urlshortener2014.common.domain.ShortURL;
import urlshortener2014.dimgray.domain.UrlPair;
import urlshortener2014.dimgray.web.UrlShortenerControllerWithLogs;;


/**
 * Clase que extiende la clase RequestContextAwareCallable<V> y permite ejecutar un m�todo
 * concurrentemente sin que aparezcan excepciones relacionadas con el RequestContextHolder.
 * @author Ivan
 *
 */
public class UrlShortenerTask extends RequestContextAwareCallable<UrlPair>{

	private String url,sponsor,brand;
	private HttpServletRequest request;
	private UrlShortenerControllerWithLogs uscw;

	/**
	 * M�todo constructor que contiene los elementos necesarios para ejecutar 
	 * uan petici�n al acortador de URLs de forma as�ncrona.
	 * @param url Url a acortar.
	 * @param request Petici�n.
	 * @param sponsor Sponsor
	 * @param brand Marca
	 * @param uscw Contexto
	 */
	public UrlShortenerTask(String url,HttpServletRequest request, String sponsor, 
			String brand,UrlShortenerControllerWithLogs uscw) {
		this.url = url;
		this.request = request;
		this.sponsor = sponsor;
		this.brand = brand;
		this.uscw = uscw;
	}

	/**
	 * Devuelve el resultado de la petici�n al acortador, un par url, url acortada 
	 * si la url est� bien formada, o un par url, null si no lo estaba.
	 */
	public UrlPair onCall() {
		ResponseEntity<ShortURL> re = uscw.shortener(url, sponsor, brand,request);
		if(re.getStatusCode() == HttpStatus.BAD_REQUEST){
			return new UrlPair(url,null);
		}
		return new UrlPair(url,re.getBody().getUri());
	}



}
