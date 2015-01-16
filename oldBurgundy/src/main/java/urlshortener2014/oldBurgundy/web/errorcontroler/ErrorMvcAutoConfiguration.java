package urlshortener2014.oldBurgundy.web.errorcontroler;
/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.Ordered;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.util.HtmlUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} to render errors via a MVC error
 * controller.
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 */
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class })
@ConditionalOnWebApplication
// Ensure this loads before the main WebMvcAutoConfiguration so that the error View is
// available
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@Configuration
public class ErrorMvcAutoConfiguration implements EmbeddedServletContainerCustomizer,
		Ordered {

	@Value("${error.path:/error}")
	private String errorPath = "/error";

	@Autowired
	private ServerProperties properties;

	@Override
	public int getOrder() {
		return 0;
	}

	@Bean
	@ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
	public DefaultErrorAttributes errorAttributes() {
		return new DefaultErrorAttributes();
	}

	@Bean
	@ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
	public BasicErrorController basicErrorController(ErrorAttributes errorAttributes) {
		return new BasicErrorController(errorAttributes);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		container.addErrorPages(new ErrorPage(this.properties.getServletPrefix() + this.errorPath));
	}
	
	public static ResponseEntity<?> responseError(HttpStatus httpStatus){
		StringWriter writer = new StringWriter();		
		VelocityContext context = new VelocityContext();
		context.put("error", httpStatus.getReasonPhrase());
		context.put("status", httpStatus);
		context.put("img", "http://localhost:8080/img/" + httpStatus + ".jpg");
		Velocity.mergeTemplate("err.vm", "ISO-8859-1", context, writer);
		HttpHeaders h = new HttpHeaders();
		h.setContentType(MediaType.TEXT_HTML);
		return new ResponseEntity<>(writer.toString(), h, HttpStatus.NOT_FOUND);
	}

	@Configuration
	@ConditionalOnProperty(prefix = "error.whitelabel", name = "enabled", matchIfMissing = true)
	@Conditional(ErrorTemplateMissingCondition.class)
	protected static class WhitelabelErrorViewConfiguration {

		@Bean(name = "error")
		@ConditionalOnMissingBean(name = "error")
		public static View defaultErrorView() {
			StringWriter writer = new StringWriter();		
			VelocityContext context = new VelocityContext();
			context.put("error", "${error}");
			context.put("status", "${status}");
			context.put("img", "http://localhost:8080/img/" + "${status}" + ".jpg");
			Velocity.mergeTemplate("err.vm", "ISO-8859-1", context, writer);
			SpelView defaultErrorView = new SpelView(writer.toString());
			return (defaultErrorView);
		}

		// If the user adds @EnableWebMvc then the bean name view resolver from
		// WebMvcAutoConfiguration disappears, so add it back in to avoid disappointment.
		@Bean
		@ConditionalOnMissingBean(BeanNameViewResolver.class)
		public BeanNameViewResolver beanNameViewResolver() {
			BeanNameViewResolver resolver = new BeanNameViewResolver();
			resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
			return resolver;
		}

	}

	/**
	 * {@link SpringBootCondition} that matches when no error template view is detected.
	 */
	private static class ErrorTemplateMissingCondition extends SpringBootCondition {

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context,
				AnnotatedTypeMetadata metadata) {
			List<TemplateAvailabilityProvider> availabilityProviders = SpringFactoriesLoader
					.loadFactories(TemplateAvailabilityProvider.class,
							context.getClassLoader());

			for (TemplateAvailabilityProvider availabilityProvider : availabilityProviders) {
				if (availabilityProvider.isTemplateAvailable("error",
						context.getEnvironment(), context.getClassLoader(),
						context.getResourceLoader())) {
					return ConditionOutcome.noMatch("Template from "
							+ availabilityProvider + " found for error view");
				}
			}

			return ConditionOutcome.match("No error template view detected");
		}

	}

	/**
	 * Simple {@link View} implementation that resolves variables as SpEL expressions.
	 */
	private static class SpelView implements View {

		private final String template;

		private final StandardEvaluationContext context = new StandardEvaluationContext();

		private PropertyPlaceholderHelper helper;

		private PlaceholderResolver resolver;

		public SpelView(String template) {
			this.template = template;
			this.context.addPropertyAccessor(new MapAccessor());
			this.helper = new PropertyPlaceholderHelper("${", "}");
			this.resolver = new SpelPlaceholderResolver(this.context);
		}

		@Override
		public String getContentType() {
			return "text/html";
		}

		@Override
		public void render(Map<String, ?> model, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
			if (response.getContentType() == null) {
				response.setContentType(getContentType());
			}
			Map<String, Object> map = new HashMap<String, Object>(model);
			map.put("path", request.getContextPath());
			this.context.setRootObject(map);
			String result = this.helper.replacePlaceholders(this.template, this.resolver);
			response.getWriter().append(result);
		}

	}

	/**
	 * SpEL based {@link PlaceholderResolver}.
	 */
	private static class SpelPlaceholderResolver implements PlaceholderResolver {

		private final SpelExpressionParser parser = new SpelExpressionParser();

		private final StandardEvaluationContext context;

		public SpelPlaceholderResolver(StandardEvaluationContext context) {
			this.context = context;
		}

		@Override
		public String resolvePlaceholder(String name) {
			Expression expression = this.parser.parseExpression(name);
			try {
				Object value = expression.getValue(this.context);
				return HtmlUtils.htmlEscape(value == null ? null : value.toString());
			}
			catch (Exception ex) {
				return null;
			}
		}

	}

}