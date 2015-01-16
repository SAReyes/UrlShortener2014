package urlshortener2014.oldBurgundy.config;

import org.apache.velocity.app.Velocity;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@PropertySource("classpath:/properties/server.properties")
@EnableEntityLinks
@ComponentScan(basePackages = {
        "urlshortener2014.oldBurgundy.web"
})
public class WebAppContext extends WebMvcConfigurerAdapter {
	
	public WebAppContext(){		
		Velocity.init("./src/main/resources/properties/velocity.properties");		
	}
	
}
