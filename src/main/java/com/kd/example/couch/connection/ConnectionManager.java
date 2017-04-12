package com.kd.example.couch.connection;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;

public class ConnectionManager {
	public static Bucket getCouchbaseClient() {
		Cluster cluster = CouchbaseCluster.create();
		return cluster.openBucket();
	}
}
