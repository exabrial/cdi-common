package com.github.exabrial.cdi.common.jsonb;

import org.slf4j.Logger;

import com.github.exabrial.cdi.nanoscopes.boundaryscoped.BoundaryScoped;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;

@ApplicationScoped
public class JsonbProducer {
	static final String JOHNZON_USE_BIGDECIMAL_STRINGADAPTER = "johnzon.use-bigdecimal-stringadapter";

	@Inject
	private Logger log;

	@Produces
	@BoundaryScoped
	Jsonb createJsonb() {
		final Jsonb jsonb = JsonbBuilder.create(newConfig());
		log.trace("createJsonb() jsonb:{}", jsonb);
		return jsonb;
	}

	void disposeJsonb(@Disposes final Jsonb jsonb) {
		log.trace("disposeJsonb() jsonb:{}", jsonb);
		try {
			jsonb.close();
		} catch (final Exception exception) {
			log.trace("disposeJsonb() exception closing Jsonb jsonb:{}", jsonb, exception);
		}
	}

	static final JsonbConfig newConfig() {
		return new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.IDENTITY)
				.withPropertyVisibilityStrategy(new InheritedPropertyVisibilityStrategy())
				.setProperty(JOHNZON_USE_BIGDECIMAL_STRINGADAPTER, false);
	}
}
