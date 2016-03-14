package nl.pdok.java.util;

import org.junit.Assert;

public class TestUUIDRandomGenerator {

	@org.junit.Test
	public void testUUID() {
		UUIDRandomGenerator r = new UUIDRandomGenerator();
		String test1 = String.valueOf( r.randomString());
		System.out.println(test1);
		String test2 = String.valueOf( r.randomString());
		System.out.println(test2);
		Assert.assertNotEquals(test1, test2);
	}
}
