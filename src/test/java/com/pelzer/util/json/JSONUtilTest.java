package com.pelzer.util.json;

import junit.framework.TestCase;

public class JSONUtilTest extends TestCase {
	public static class JSONFoo extends JSONObject {
		int i;
		int j;
		String jason;

		public JSONFoo() {
		}

		@Override
		protected String getIdentifier() {
			return "foo";
		}

		public JSONFoo(final int i, final int j, final String jason) {
			this.i = i;
			this.j = j;
			this.jason = jason;
		}
	}

	public static class JSONTimestamp extends JSONFoo {
		long timestamp;

		public JSONTimestamp() {
		}

		public JSONTimestamp(final int i, final int j, final String jason,
		    final long timestamp) {
			super(i, j, jason);
			this.timestamp = timestamp;
		}
	}

	public void testTimestamp() {
		final String json = "{\"timestamp\":1293643635318,\"i\":1,\"j\":2,\"jason\":\"yaaaaargh\",\"_i\":\"foo\"}";
		assertEquals(json,
		    JSONUtil.toJSON(new JSONTimestamp(1, 2, "yaaaaargh", 1293643635318L)));
		final JSONTimestamp stamp = JSONUtil.fromJSON(json, JSONTimestamp.class);
		assertEquals(1293643635318L, stamp.timestamp);
	}

	public void testToJSON() {
		assertEquals("{\"i\":7,\"j\":9,\"jason\":\"Jason Pelzer\",\"_i\":\"foo\"}",
		    JSONUtil.toJSON(new JSONFoo(7, 9, "Jason Pelzer")));
	}

	public void testRoundTrip() {
		final JSONFoo foo = new JSONFoo(3, 4, "Pelzer, Jason");

		final JSONFoo bar = JSONUtil.fromJSON(JSONUtil.toJSON(foo), JSONFoo.class);
		assertEquals(3, bar.i);
		assertEquals(4, bar.j);
		assertEquals("Pelzer, Jason", bar.jason);
	}

	public void testJSONObject() {
		JSONUtil.register(new JSONFoo());

		final String json = JSONUtil.toJSON(new JSONFoo(6, 7, "argh"));

		final JSONObject obj = JSONUtil.fromJSON(json);
		assertTrue(obj instanceof JSONFoo);
		final JSONFoo foo = (JSONFoo) obj;
		assertEquals(6, foo.i);

	}
}
