package de.unibayreuth.bayceer.bayeos.gateway.event;

import java.io.IOException;

public interface EventListener {

	void eventFired(Event e) throws IOException;
}
