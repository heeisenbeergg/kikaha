package io.skullabs.undertow.routing.samples;

import static org.mockito.Mockito.mock;
import io.skullabs.undertow.urouting.api.ExceptionHandler;
import io.skullabs.undertow.urouting.api.Response;
import trip.spi.Service;

@Service
public class NullPointerExceptionHandler implements ExceptionHandler<NullPointerException> {

	@Override
	public Response handle( NullPointerException exception ) {
		return mock( Response.class );
	}
}
