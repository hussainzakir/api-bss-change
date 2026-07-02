package com.trinet.ambis.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class BssSecurityExceptionTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void bssSecurityExceptionTest1() {
		thrown.expect(BssSecurityException.class);

		throw new BssSecurityException();
	}

	@Test
	public void bssSecurityExceptionTest2() {
		thrown.expect(BssSecurityException.class);
		thrown.expectMessage("some message");

		throw new BssSecurityException("some message");
	}

	@Test
	public void bssSecurityExceptionTest3() {
		thrown.expect(BssSecurityException.class);
		thrown.expectMessage("some message");

		throw new BssSecurityException("some message", new NullPointerException());
	}

	@Test
	public void bssSecurityExceptionTest4() {
		thrown.expect(BssSecurityException.class);

		throw new BssSecurityException(new NullPointerException());
	}
}
