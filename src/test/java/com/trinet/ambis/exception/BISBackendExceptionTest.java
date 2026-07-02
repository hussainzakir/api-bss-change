package com.trinet.ambis.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class BISBackendExceptionTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void bisBackendExceptionTest1() {
		thrown.expect(BISBackendException.class);

		throw new BISBackendException();
	}

	@Test
	public void bisBackendExceptionTest2() {
		thrown.expect(BISBackendException.class);
		thrown.expectMessage("some message");

		throw new BISBackendException("some message");
	}

	@Test
	public void bisBackendExceptionTest3() {
		thrown.expect(BISBackendException.class);
		thrown.expectMessage("some message");

		throw new BISBackendException("some message", new NullPointerException());
	}

	@Test
	public void bisBackendExceptionTest4() {
		thrown.expect(BISBackendException.class);

		throw new BISBackendException(new NullPointerException());
	}
}
