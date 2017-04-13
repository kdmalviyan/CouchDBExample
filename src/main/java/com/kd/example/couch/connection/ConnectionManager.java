package com.kd.example.couch.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

import rx.Observable;
import rx.functions.Func1;

public class ConnectionManager {
	/**
	 * Using default bucket for now.
	 */
	public static Bucket getCouchbaseClient() {
		// Simple Cluster, connected to localhost and default bucket
		Cluster cluster = CouchbaseCluster.create();
		Bucket defaultBucket = cluster.openBucket();

		// Cluster with multiple nodes
		String[] nodes = { "localhost", "127.0.0.1" };
		Cluster cluster1 = CouchbaseCluster.create(nodes);

		CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder().connectTimeout(TimeUnit.SECONDS.toMillis(10))
				.requestBufferSize(1024).build();

		// Cluster with environment variables
		CouchbaseCluster cluster11 = CouchbaseCluster.create(env);
		// Cluster with environment variables and multiple nodes
		CouchbaseCluster cluster12 = CouchbaseCluster.create(env, nodes);
		
		Observable.just(cluster11, cluster12).subscribe();
		
		return cluster1.openBucket();
	}

	public static void shutdownClusterConnection(CouchbaseEnvironment env, List<CouchbaseCluster> clusters) {
		// If environment is used to create cluster, we need to shutdown ourself

		Observable.from(clusters).flatMap(new Func1<CouchbaseCluster, Observable<? extends Boolean>>() {
			@Override
			public Observable<? extends Boolean> call(CouchbaseCluster c) {
				return Observable.just(c.disconnect());
			}
		}).last().flatMap(new Func1<Boolean, Observable<? extends Boolean>>() {
			@Override
			public Observable<? extends Boolean> call(Boolean isDisconnected) {
				if (isDisconnected) {
					isDisconnected = env.shutdown();
				}
				// shutdown environment when last cluster has disconnected
				return Observable.just(isDisconnected);
			}
		});
	}
}
