package com.github.exabrial.cdi.common.jsonb.producer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Type;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;
import jakarta.transaction.TransactionScoped;

import org.slf4j.Logger;

import com.github.exabrial.cdi.common.api.qualifier.NonTransacted;
import com.github.exabrial.cdi.common.api.qualifier.Transacted;
import com.github.exabrial.cdi.nanoscopes.boundaryscoped.api.scope.BoundaryScoped;

@ApplicationScoped
public class JsonbProducer {
	public static final String JOHNZON_USE_BIGDECIMAL_STRINGADAPTER = "johnzon.use-bigdecimal-stringadapter";

	@Inject
	private Logger log;

	@Produces
	@Transacted
	@Default
	@TransactionScoped
	Jsonb createTransactedJsonb() {
		final Jsonb jsonb = new SerializableJsonb(JsonbBuilder.create(newConfig()));
		log.trace("createTransactedJsonb() jsonb:{}", jsonb);
		return jsonb;
	}

	@Produces
	@NonTransacted
	@BoundaryScoped
	Jsonb createNonTransactedJsonb() {
		final Jsonb jsonb = JsonbBuilder.create(newConfig());
		log.trace("createNonTransactedJsonb() jsonb:{}", jsonb);
		return jsonb;
	}

	void disposeJsonb(@Disposes @Any final Jsonb jsonb) {
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

	private static final class SerializableJsonb implements Jsonb, Serializable {
		private static final long serialVersionUID = 1L;
		private transient Jsonb jsonb;

		private SerializableJsonb(final Jsonb jsonb) {
			this.jsonb = jsonb;
		}

		private void checkJsonb() {
			if (jsonb == null) {
				jsonb = CDI.current().select(Jsonb.class, Transacted.LITERAL).get();
			}
		}

		@Override
		public String toJson(final Object object) {
			checkJsonb();
			return jsonb.toJson(object);
		}

		@Override
		public String toJson(final Object object, final Type runtimeType) {
			checkJsonb();
			return jsonb.toJson(object, runtimeType);
		}

		@Override
		public void toJson(final Object object, final Writer writer) {
			checkJsonb();
			jsonb.toJson(object, writer);
		}

		@Override
		public void toJson(final Object object, final Type runtimeType, final Writer writer) {
			checkJsonb();
			jsonb.toJson(object, runtimeType, writer);
		}

		@Override
		public void toJson(final Object object, final OutputStream stream) {
			checkJsonb();
			jsonb.toJson(object, stream);
		}

		@Override
		public void toJson(final Object object, final Type runtimeType, final OutputStream stream) {
			checkJsonb();
			jsonb.toJson(object, runtimeType, stream);
		}

		@Override
		public <T> T fromJson(final String str, final Class<T> type) {
			checkJsonb();
			return jsonb.fromJson(str, type);
		}

		@Override
		public <T> T fromJson(final String str, final Type runtimeType) {
			checkJsonb();
			return jsonb.fromJson(str, runtimeType);
		}

		@Override
		public <T> T fromJson(final Reader reader, final Class<T> type) {
			checkJsonb();
			return jsonb.fromJson(reader, type);
		}

		@Override
		public <T> T fromJson(final Reader reader, final Type runtimeType) {
			checkJsonb();
			return jsonb.fromJson(reader, runtimeType);
		}

		@Override
		public <T> T fromJson(final InputStream stream, final Class<T> type) {
			checkJsonb();
			return jsonb.fromJson(stream, type);
		}

		@Override
		public <T> T fromJson(final InputStream stream, final Type runtimeType) {
			checkJsonb();
			return jsonb.fromJson(stream, runtimeType);
		}

		@Override
		public void close() throws Exception {
			if (jsonb != null) {
				jsonb.close();
			}
		}
	}
}
