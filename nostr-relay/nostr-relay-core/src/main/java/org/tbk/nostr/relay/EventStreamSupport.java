package org.tbk.nostr.relay;

/**
 * A marker interface to indicate support for streaming events to active subscriptions.
 * Classes implementing this interface are responsible for sending incoming events to
 * clients. In a single instance deployment, this can be done by simply processing all
 * incoming events. See {@link org.tbk.nostr.relay.support.SimpleEventStreamSupport}
 * as an example for a simple approach.
 * <p>
 * In a multi-instance environment, clients must be notified of events that can come
 * from all kind of sources (message queues, pubsubs, etc.).
 */
public interface EventStreamSupport {

}
