package com.kd.example.couch.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

import rx.Observable;
import rx.functions.Func1;

public class ConnectionManager {
	/**
	 * Using default bucket for now.
	 */
	public static Bucket getCouchbaseClient(String bucketname, boolean createnew, String... nodes) {
		// Simple Cluster, connected to localhost and default bucket
		Cluster cluster = CouchbaseCluster.create();
		Bucket defaultBucket = cluster.openBucket();

		// Cluster with multiple nodes

		Cluster cluster1 = CouchbaseCluster.create(nodes);

		CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder().connectTimeout(TimeUnit.SECONDS.toMillis(10))
				.requestBufferSize(1024).build();

		// Cluster with environment variables
		CouchbaseCluster cluster11 = CouchbaseCluster.create(env);
		// Cluster with environment variables and multiple nodes
		CouchbaseCluster cluster12 = CouchbaseCluster.create(env, nodes);

		Observable.just(cluster11, cluster12).subscribe();

		checkAndCreateBucket(bucketname, cluster1, createnew);

		Bucket specificBucket = cluster1.openBucket(bucketname, "password");

		return specificBucket;
	}

	/**
	 * Check if bucket exists, if not create new bucket with specified name
	 * 
	 * @param bucketname
	 * @param cluster1
	 */
	private static void checkAndCreateBucket(String bucketname, Cluster cluster1, boolean createnew) {
		ClusterManager clusterManager = cluster1.clusterManager("admin", "admin@123");
		if (!clusterManager.hasBucket(bucketname) && createnew) {
			BucketSettings bucketSettings = new DefaultBucketSettings.Builder().type(BucketType.COUCHBASE)
					.name(bucketname).quota(120).password("password").build();
			clusterManager.insertBucket(bucketSettings);
		}
	}

	/**
	 * Close bucket and release resources when not required.
	 * 
	 * @param bucket
	 */
	public static void closeBucket(Bucket bucket) {
		if (bucket != null && !bucket.isClosed()) {
			bucket.async().close();
		}
	}

	/**
	 * Close all open clusters and environment
	 * 
	 * @param env
	 * @param clusters
	 */
	public static void shutdownClusterConnection(CouchbaseEnvironment env, List<CouchbaseCluster> clusters) {
		// If environment is used to create cluster, we need to shutdown ourself
		if (clusters != null && !clusters.isEmpty()) {
			Observable.from(clusters).flatMap(new Func1<CouchbaseCluster, Observable<? extends Boolean>>() {
				@Override
				public Observable<? extends Boolean> call(CouchbaseCluster c) {
					return Observable.just(c.disconnect());
				}
			}).last().flatMap(new Func1<Boolean, Observable<? extends Boolean>>() {
				@Override
				public Observable<? extends Boolean> call(Boolean isDisconnected) {
					if (isDisconnected) {
						if (env != null) {
							isDisconnected = env.shutdown();
						}
					}
					// shutdown environment when last cluster has disconnected
					return Observable.just(isDisconnected);
				}
			});
		}

	}
}
