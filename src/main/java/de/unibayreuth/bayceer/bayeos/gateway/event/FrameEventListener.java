package de.unibayreuth.bayceer.bayeos.gateway.event;

import java.io.IOException;

public interface FrameEventListener {

	void eventFired(FrameEvent e) throws IOException;
}
