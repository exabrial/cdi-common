package com.github.exabrial.cdi.common.jaxrs.producer;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.slf4j.Logger;

import com.github.exabrial.cdi.common.api.qualifier.NonTransacted;
import com.github.exabrial.cdi.common.api.qualifier.Transacted;
import com.github.exabrial.cdi.nanoscopes.boundaryscoped.api.scope.BoundaryScoped;

@ApplicationScoped
public class JaxRsClientProducer {
	@Inject
	private Logger log;

	@Produces
	@Transacted
	@Default
	@TransactionScoped
	Client createTransactedClient() {
		final Client client = new SerializableClient(ClientBuilder.newBuilder().build());
		log.trace("createTransactedClient() client:{}", client);
		return client;
	}

	@Produces
	@NonTransacted
	@BoundaryScoped
	Client createNonTransactedClient() {
		final Client client = ClientBuilder.newBuilder().build();
		log.trace("createNonTransactedClient() client:{}", client);
		return client;
	}

	void closeClient(@Disposes @Any final Client client) {
		log.trace("closeClient() client:{}", client);
		try {
			client.close();
		} catch (final Exception exception) {
			log.trace("closeClient() exception closing Client client:{}", client, exception);
		}
	}

	private static final class SerializableClient implements Client, Serializable {
		private static final long serialVersionUID = 1L;
		private transient Client client;

		private SerializableClient(final Client client) {
			this.client = client;
		}

		private void checkClient() {
			if (client == null) {
				client = CDI.current().select(Client.class, Transacted.LITERAL).get();
			}
		}

		@Override
		public void close() {
			if (client != null) {
				client.close();
			}
		}

		@Override
		public WebTarget target(final String uri) {
			checkClient();
			return client.target(uri);
		}

		@Override
		public WebTarget target(final URI uri) {
			checkClient();
			return client.target(uri);
		}

		@Override
		public WebTarget target(final UriBuilder uriBuilder) {
			checkClient();
			return client.target(uriBuilder);
		}

		@Override
		public WebTarget target(final Link link) {
			checkClient();
			return client.target(link);
		}

		@Override
		public Invocation.Builder invocation(final Link link) {
			checkClient();
			return client.invocation(link);
		}

		@Override
		public SSLContext getSslContext() {
			checkClient();
			return client.getSslContext();
		}

		@Override
		public HostnameVerifier getHostnameVerifier() {
			checkClient();
			return client.getHostnameVerifier();
		}

		@Override
		public Configuration getConfiguration() {
			checkClient();
			return client.getConfiguration();
		}

		@Override
		public Client property(final String name, final Object value) {
			checkClient();
			return client.property(name, value);
		}

		@Override
		public Client register(final Class<?> componentClass) {
			checkClient();
			return client.register(componentClass);
		}

		@Override
		public Client register(final Class<?> componentClass, final int priority) {
			checkClient();
			return client.register(componentClass, priority);
		}

		@Override
		public Client register(final Class<?> componentClass, final Class<?>... contracts) {
			checkClient();
			return client.register(componentClass, contracts);
		}

		@Override
		public Client register(final Class<?> componentClass, final Map<Class<?>, Integer> contracts) {
			checkClient();
			return client.register(componentClass, contracts);
		}

		@Override
		public Client register(final Object component) {
			checkClient();
			return client.register(component);
		}

		@Override
		public Client register(final Object component, final int priority) {
			checkClient();
			return client.register(component, priority);
		}

		@Override
		public Client register(final Object component, final Class<?>... contracts) {
			checkClient();
			return client.register(component, contracts);
		}

		@Override
		public Client register(final Object component, final Map<Class<?>, Integer> contracts) {
			checkClient();
			return client.register(component, contracts);
		}
	}
}
