package com.kd.example.couch.service;

import java.util.ArrayList;
import java.util.List;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;

import rx.Observable;
import rx.functions.Func1;

public class UserService {
	public UserService() {
	}

	Bucket bucket;

	/**
	 * 
	 * @param bucket
	 */
	public UserService(Bucket bucket) {
		this.bucket = bucket;
	}

	/**
	 * 
	 * @param document
	 * @return
	 */
	public JsonDocument createUser(JsonDocument document) {
		JsonDocument updatedDocument = bucket.insert(document);
		return updatedDocument;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public JsonDocument getUserById(String id) {
		return bucket.get(id);
	}

	/**
	 * 
	 * @param document
	 * @return
	 */
	public JsonDocument updateUserInfo(JsonDocument document) {
		return bucket.replace(document);
	}

	/**
	 * 
	 * @param document
	 */
	public void deleteUserInfo(JsonDocument document) {
		bucket.remove(document);
	}

	/**
	 * 
	 * @param id
	 */
	public void deleteUserInfo(String id) {
		bucket.remove(id);
	}

	/**
	 * 
	 * @return
	 */
	public List<JsonDocument> getAllUsers() {
		int docsToCreate = 10;
		List<String> ids = new ArrayList<String>();
		for (int i = 0; i < docsToCreate; i++) {
			ids.add("doc-" + i);
		}
		ids.add("user::1492009597445");
		ids.add("user::kdmalviyan");
		ids.add("user::user@test.com");

		List<JsonDocument> listWithFrom = Observable.from(ids).flatMap(new Func1<String, Observable<JsonDocument>>() {
			@Override
			public Observable<JsonDocument> call(String id) {
				return bucket.async().get(id);
			}
		}).toList().toBlocking().single();
		return listWithFrom;
	}

	public List<JsonDocument> deleteAllUsers(List<String> ids) {
		int docsToCreate = 10;
		ids = new ArrayList<String>();
		for (int i = 0; i < docsToCreate; i++) {
			ids.add("doc-" + i);
		}

		List<JsonDocument> listWithFrom = Observable.from(ids).flatMap(new Func1<String, Observable<JsonDocument>>() {
			@Override
			public Observable<JsonDocument> call(String id) {
				return bucket.async().remove(id);
			}
		}).toList().toBlocking().single();
		return listWithFrom;
	}

	/**
	 * 
	 * @param documents
	 * @return
	 */
	public List<JsonDocument> bulkCreate(List<JsonDocument> documents) {
		return Observable.from(documents).flatMap(new Func1<JsonDocument, Observable<JsonDocument>>() {
			@Override
			public Observable<JsonDocument> call(JsonDocument document) {
				return bucket.async().insert(document);
			}
		}).toList().toBlocking().single();
	}

	public void findLocation(String id) {
		System.out.println(bucket.lookupIn(id).exists("$.location").get("$.location").execute());;
	}
}