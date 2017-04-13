package com.kd.example.couch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.kd.example.couch.connection.ConnectionManager;
import com.kd.example.couch.service.UserService;

public class CouchDbApp {
	static String[] nodes = { "localhost", "127.0.0.1" };
	static Logger logger = Logger.getLogger(CouchDbApp.class);
	// create new bucket if not exists
	static boolean createnew = true;
	static Bucket bucket = ConnectionManager.getCouchbaseClient("CRUD_BUCKET", createnew, nodes);

	public static void main(String[] args) throws InterruptedException {
		logger.info("Creating dummy document for inserting into couch bucket.");
		JsonObject data = JsonObject.create().put("type", "user").put("name", "kuldeep").put("password", "password");
		logger.info("Creating location, inner document...");
		JsonObject location = JsonObject.create().put("Flat", "CG1 403").put("Locality", "Supertech").put("Sector", 74)
				.put("city", "Noida");
		data.put("location", location);

		logger.info("Creating service instance for communicating with couchbase...");
		UserService service = new UserService(bucket);

		// JsonDocument document = JsonDocument.create("user::kdmalviyan",
		// data);
		// service.createUser(document);
		// String id = "user::kdmalviyan";
		// service.findLocation(id);
		// service.getUserById("user::kdmalviyan");
		// document.content().put("Extra", "Extra information");
		// service.updateUserInfo(document);
		// service.deleteUserInfo(document.id());
		// service.deleteUserInfo(document);

		// service.deleteAllUsers();
		/*
		 * int docsToCreate = 10; List<JsonDocument> documentList = new
		 * ArrayList<JsonDocument>();
		 * 
		 * for (int i = 0; i < docsToCreate; i++) {
		 * documentList.add(JsonDocument.create("doc-" + i, data)); }
		 * 
		 * service.bulkCreate(documentList);
		 */
		logger.info("gettiong all the documents from bucket.");
		List<JsonDocument> documents = service.getAllUsers();
		documents.stream().forEach(doc -> {
			logger.info(doc.content());
		});

		ConnectionManager.closeBucket(bucket);
	}
}
