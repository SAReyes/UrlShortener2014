package urlshortener2014.taupegray;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import urlshortener2014.common.domain.ShortURL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port=0")
@DirtiesContext
public class ApplicationTests {
	private final String hash = "d2536c56";
	
	@Value("${local.server.port}")
	private int port = 0;

	@Test
	public void testHome() throws Exception {
		ResponseEntity<String> entity = new TestRestTemplate().getForEntity(
				"http://localhost:" + this.port, String.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertTrue("Wrong body (title doesn't match):\n" + entity.getBody(), entity
				.getBody().contains("<title>URL"));
	}

	@Test
	public void testCss() throws Exception {
		ResponseEntity<String> entity = new TestRestTemplate().getForEntity(
				"http://localhost:" + this.port
						+ "/webjars/bootstrap/3.0.3/css/bootstrap.min.css", String.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertTrue("Wrong body:\n" + entity.getBody(), entity.getBody().contains("body"));
		assertEquals("Wrong content type:\n" + entity.getHeaders().getContentType(),
				MediaType.valueOf("text/css;charset=UTF-8"), entity.getHeaders().getContentType());
	}
	
	@Test
	public void testPost() throws Exception {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
		parameters.add("url", "http://ianfette.org");
		parameters.add("sponsor", "http://www.unizar.es");
		TestRestTemplate rt = new TestRestTemplate();

		ResponseEntity<ShortURL> entity = rt.postForEntity(
				"http://localhost:" + this.port+"/link", parameters, ShortURL.class);
		assertEquals(HttpStatus.CREATED, entity.getStatusCode());
		assertEquals(entity.getBody().getHash(),hash);
	}
	
	@Test
	public void testQR() throws Exception {
		ResponseEntity<byte[]> entity = new TestRestTemplate().getForEntity(
				"http://localhost:" + this.port
						+ "/qr"+hash, byte[].class);
		assertEquals(HttpStatus.FOUND, entity.getStatusCode());
		assertEquals("Wrong content type:\n" + entity.getHeaders().getContentType(),
				MediaType.valueOf("image/png;charset=UTF-8"), entity.getHeaders().getContentType());
	}
	
	@Test
	public void testSponsorAndWarning() throws Exception {
		ResponseEntity<String> entity = new TestRestTemplate().getForEntity(
				"http://localhost:" + this.port + "/l" + hash, String.class);
		assertEquals(HttpStatus.FOUND, entity.getStatusCode());
		assertTrue("Wrong body (not sponsor loading):\n" + entity.getBody(), entity
				.getBody().contains("Loading"));
		assertTrue("Wrong body (not safe browsing):\n" + entity.getBody(), entity
				.getBody().contains("Warning"));
	}
}