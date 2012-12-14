package com.pelzer.util.json;


/**
 * An object that the {@link JSONUtil} can serialize/deserialize automagically because
 * of the {@link #getIdentifier()} method.
 */
public abstract class JSONObject {
	protected String _i = getIdentifier();

	/**
	 * @return a unique identifier that will be used to serialize/deserialize this
	 *         class to JSON later. Should be short, just one or two characters,
	 *         and by convention requests should be lower case and responses be
	 *         the same character upper case, whenever JSON is passed in
	 *         request/response manner.
	 */
	protected abstract String getIdentifier();
}
