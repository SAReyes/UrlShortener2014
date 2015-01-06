package urlshortener2014.bangladeshgreen.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import urlshortener2014.common.domain.Click;
import urlshortener2014.common.domain.ShortURL;
import urlshortener2014.common.repository.ClickRepository;
import urlshortener2014.common.repository.ShortURLRepository;
import urlshortener2014.common.web.UrlShortenerController;

@RestController
public class UrlShortenerControllerWithLogs extends UrlShortenerController {

	@Autowired
	private ClickRepository clickRepository;
	@Autowired
	private ShortURLRepository SURLR;
	private static final Logger logger = LoggerFactory
			.getLogger(UrlShortenerControllerWithLogs.class);

	public ResponseEntity<?> redirectTo(@PathVariable String id,
			HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		String agent = request.getHeader("User-Agent");
		String ip = request.getRemoteAddr();
		String navegador = "", SO = "";
		if (agent.indexOf("Chrome") != -1)
			navegador = "Chrome";
		else if (agent.indexOf("Firefox") != -1)
			navegador = "Firefox";
		else if (agent.indexOf("Safari") != -1)
			navegador = "Safari";
		else
			navegador = "Explorer";

		if (agent.indexOf("Windows") != -1)
			SO = "Windows";
		else if (agent.indexOf("Linux") != -1)
			SO = "Linux";
		else if (agent.indexOf("Macintosh") != -1)
			SO = "Macintosh";

		logger.info("Requested redirection with hash " + id);
		// Guardar en un objeto la llamada al padre, guardarme en una lista la
		// consulta
		// a los Cliks, y quedarme con el ultimo con la IP del request,
		// modificar el
		// click con el navegador y SO, actualizar BD y return
		ResponseEntity<?> response = super.redirectTo(id, request);
		List<Click> listaClicks = clickRepository.findByHash(id);
		logger.info("Tamaño: " + listaClicks.size() + "ip: " + ip);
		for (int i = listaClicks.size() - 1; i >= 0; i--) {
			Click click = listaClicks.get(i);
			logger.info("Click con ip: " + click.getIp());
			if (click.getIp().equals(ip)) {
				String hash = click.getHash();
				Long identificador = click.getId();
				Date fecha = click.getCreated();
				Click clickFinal = new Click(identificador, hash, fecha, null,
						navegador, SO, ip, null);
				clickRepository.update(clickFinal);
				logger.info("Actualizado Click con " + navegador + "SO: " + SO
						+ " e ip:" + ip);
				break;
			}
		}

		return response;
	}

	public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
			@RequestParam(value = "sponsor", required = false) String sponsor,
			@RequestParam(value = "brand", required = false) String brand,
			HttpServletRequest request) {

		logger.info("Requested new short for uri " + "url");

		ResponseEntity<ShortURL> su = super.shortener(url, sponsor, brand,
				request);

		// comprobar si es segura
		Client c = ClientBuilder.newClient();
		url = parse(url);
		Response response = c
				.target("https://sb-ssl.google.com/safebrowsing/api/lookup?client=Roberto&key=AIzaSyBbjDCPwK13dOYioVf6Cp9_lrFZ_MOEFbU&appver=1.5.2&pver=3.1&url="
						+ url).request(MediaType.TEXT_HTML).get();

		
		if (response.getStatus() == 200){
				
			ShortURL suUnSafe = SURLR.mark(su.getBody(), false);// marcar como no segura
			new DirectFieldAccessor(su.getBody()).setPropertyValue("safe", false);
		}
		return su;
	}

	@RequestMapping(value = "/Upload", method = RequestMethod.POST)
	public String upload(HttpServletRequest request) throws Exception {
		Part part = request.getPart("fileToUpload");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				part.getInputStream()));

		File fichero = new File("src\\main\\resources\\public\\csv\\fich_original.csv");
		fichero.createNewFile();
		PrintWriter f = new PrintWriter(fichero);
		String linea = "";
		while ((linea = reader.readLine()) != null) {
			System.out.println(linea);
			String[] listaURL = linea.split(",");
			for (String url : listaURL) 
				f.write(url + ",");
			f.write("\n");
		}
		f.close();
		Response respuesta=analizarCSV(fichero, request);
		if(respuesta.getStatus()==400){ return "Error con el File!";}
		return "File Uploaded!";
	}

	@SuppressWarnings("resource")
	public Response analizarCSV(File csv, HttpServletRequest request)
			throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(csv));
		File csvAcortado = new File("src\\main\\resources\\public\\csv\\fich_temporal.csv");
		csvAcortado.createNewFile();
		PrintWriter fileResul = new PrintWriter(csvAcortado);
		String linea = "";
		while ((linea = br.readLine()) != null) {
			String[] listaURL = linea.split(",");
			for (String url : listaURL) {
				System.out.println("Url " + url);
				if (!url.equals(null) || !url.equals("")) {
					ResponseEntity<ShortURL> urlAcortada = shortener(url, null,
							null, request);
					if(urlAcortada.getBody()==null) return Response.status(Status.BAD_REQUEST).build();
					else fileResul.write(urlAcortada.getBody().getUri().toString()+ ",");
				}
			}
			fileResul.write("\n");
		}
		fileResul.close();
		br.close();
		return Response.status(Status.OK).build();
	}

	private static String parse(String a) {
		String res = "";
		for (int i = 0; i < a.length(); i++) {
			switch (a.charAt(i)) {
			case ':':
				res = res + "%3A";
				break;
			case '/':
				res = res + "%2F";
				break;
			case ' ':
				res = res + "%20";
				break;
			case '?':
				res = res + "%3F";
				break;
			case '<':
				res = res + "%3C";
				break;
			case '>':
				res = res + "%3E";
				break;
			case '%':
				res = res + "%25";
				break;
			case '#':
				res = res + "%23";
				break;
			case ';':
				res = res + "%3B";
				break;
			case '|':
				res = res + "%7C";
				break;
			case '&':
				res = res + "%26";
				break;
			default:
				res = res + a.charAt(i);
				break;
			}
		}
		return res;
	}
}
