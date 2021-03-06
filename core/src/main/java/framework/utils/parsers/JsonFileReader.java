package framework.utils.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import framework.utils.dto.IFrameworkDTO;

/**
 * Json file reader for framework
 * 
 * @author Taras.Lytvyn
 *
 */
public class JsonFileReader extends FileReader {

	private ObjectMapper objectMapper;
	private JsonNode rootNode;

	public JsonFileReader(String fileLocation) {
		super(fileLocation);
		try {
			try {
				parsedInput = getStringFromInputStream(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setObjectMapper();
			closeStream();
		} catch (IllegalStateException e) {
			// cannot create object mapper
		}
	}

	public JsonFileReader() {
		setObjectMapper();
	}

	/**
	 * set properties of onject mapper for json parser
	 */
	private void setObjectMapper() {
		MappingJsonFactory jsonFactory = new MappingJsonFactory();
		jsonFactory.enable(Feature.ALLOW_COMMENTS);
		jsonFactory.enable(Feature.ALLOW_SINGLE_QUOTES);
		jsonFactory.enable(Feature.AUTO_CLOSE_SOURCE);
		objectMapper = new ObjectMapper(jsonFactory);
	}

	// TODO bad casting behavior here. If we have single property we are casting
	// to String, if we have same property in several places in json file we use
	// casting to List<JsonNode>
	/**
	 * read json file from input stream and get single property from json
	 * 
	 * @param property	property to read
	 * @return	read object
	 * @throws Exception
	 */
	public Object readJsonFileSingleProperty(String property) throws Exception {
		try {
			rootNode = objectMapper.readTree(parsedInput);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readJsonSingleProperty(property);
	}

	/**
	 * get single property from json response
	 * @param property	property
	 * @param jsonResponse	json response
	 * @return	parsed property
	 * @throws Exception
	 */
	public Object readJsonResponseSingleProperty(String property,
			String jsonResponse) throws Exception {
		try {
			rootNode = objectMapper.readTree(jsonResponse);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readJsonSingleProperty(property);
	}

	/**
	 * get json single property from json 
	 * @param property json property
	 * @return	property value
	 * @throws Exception
	 */
	private Object readJsonSingleProperty(String property) throws Exception {
		List<JsonNode> jsonNodesFound = rootNode.findValues(property);
		if (jsonNodesFound.size() > 1) {
			return getNeccessaryObjectListFromJsonNodeProperty(jsonNodesFound);
		} else if (jsonNodesFound.size() == 1) {
			return getNeccesaryObjectFromJsonNodeProperty(jsonNodesFound);
		} else
			throw new IllegalArgumentException("no node with: " + property
					+ " was found");
	}

	private Object getNeccessaryObjectListFromJsonNodeProperty(
			List<JsonNode> jsonNodesFound) throws Exception {
		List<String> jsonNodeStringValues = new ArrayList<String>();
		int quantityOfNodesWithChilds = 0;
		for (JsonNode node : jsonNodesFound) {
			if (node.elements().hasNext()) {
				quantityOfNodesWithChilds++;
			} else {
				jsonNodeStringValues.add(node.asText());
			}
		}

		if (quantityOfNodesWithChilds == jsonNodesFound.size()) {
			return jsonNodesFound;
		} else if (jsonNodeStringValues.size() == jsonNodesFound.size()) {
			return jsonNodeStringValues;
		} else
			throw new Exception(
					"there are mixed properties with such property: "
							+ jsonNodesFound.get(0).fields().next()
							+ "in json, with text value and with child fields.");
	}

	private Object getNeccesaryObjectFromJsonNodeProperty(
			List<JsonNode> jsonNodesFound) {
		JsonNode node = jsonNodesFound.get(0);
		Iterator<JsonNode> nodeIterator = node.elements();
		if (!nodeIterator.hasNext()) {
			return node.asText();
		} else {
			return node;
		}
	}

	/**
	 * read json to object from input stream
	 * @param clazz	class to parse
	 * @return	parsed object
	 */
	public <T extends IFrameworkDTO> T readJsonToObjectFromFile(Class<T> clazz) {
		return readJsonToObject(clazz, parsedInput);
	}

	/**
	 * read json from response (just wrapper for readJsonToObject with better name)
	 * @param clazz
	 * @param response
	 * @return
	 */
	public <T extends IFrameworkDTO> T readJsonToObjectFromResponse(
			Class<T> clazz, String response) {
		return readJsonToObject(clazz, response);
	}

	/**
	 * read json to Object
	 * @param clazz		class to parse
	 * @param input		string json input
	 * @return	parsed object
	 */
	private <T extends IFrameworkDTO> T readJsonToObject(Class<T> clazz,
			String input) {
		T parsedDTOList = null;
		try {
			objectMapper
					.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			objectMapper
					.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
			TypeFactory typeFactory = TypeFactory.defaultInstance();
			parsedDTOList = objectMapper.readValue(input,
					typeFactory.constructType(clazz));
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parsedDTOList;
	}

}
