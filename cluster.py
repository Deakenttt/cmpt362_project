import numpy as np
from sklearn.neighbors import NearestNeighbors
from sklearn.cluster import DBSCAN
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.metrics.pairwise import euclidean_distances
from data import fetch_user_data

def cluster_users(eps=0.5, min_samples=2):
    user_data = fetch_user_data()
    user_data.index = user_data.index.map(str)  # Ensure indices are strings for user IDs

    # Separate male and female users
    male_data = user_data[user_data["gender"] == 0].drop(columns=["id", "gender", "name"])
    female_data = user_data[user_data["gender"] == 1].drop(columns=["id", "gender", "name"])

    # Perform DBSCAN clustering for male users
    male_clusters = dbscan_clustering(male_data, eps, min_samples, prefix="male")

    # Perform DBSCAN clustering for female users
    female_clusters = dbscan_clustering(female_data, eps, min_samples, prefix="female")

    return male_clusters, female_clusters, male_data, female_data

def dbscan_clustering(data, eps, min_samples, prefix="cluster"):
    """
    Perform DBSCAN clustering and return clusters as a dictionary.
    Clusters are labeled with the prefix, and noise points are excluded.
    """
    db = DBSCAN(eps=eps, min_samples=min_samples, metric='euclidean')
    labels = db.fit_predict(data)

    clusters = {}
    for label in set(labels):
        if label == -1:
            continue  # Skip noise points
        cluster_members = data.index[labels == label].tolist()
        clusters[f"{prefix}_cluster_{label + 1}"] = cluster_members

    return clusters


# def compute_centroids(clusters, data, feature_weights=None):
#     centroids = []
#     for cluster in clusters.values():
#         cluster_vectors = data.loc[cluster]
        
#         # Apply feature weighting if provided
#         if feature_weights is not None:
#             weighted_vectors = cluster_vectors * feature_weights
#             centroid = weighted_vectors.mean(axis=0).values
#         else:
#             centroid = cluster_vectors.mean(axis=0).values

#         centroids.append(centroid)
#     return np.array(centroids)

def compute_centroids(clusters, data, feature_weights=None):
    centroids = []
    for cluster in clusters.values():
        cluster_vectors = data.loc[cluster]
        
        # Apply feature weighting if provided
        if feature_weights is not None:
            if len(feature_weights) != cluster_vectors.shape[1]:
                raise ValueError("The length of feature_weights does not match the number of features.")
            weighted_vectors = cluster_vectors * feature_weights
            centroid = weighted_vectors.mean(axis=0).values
        else:
            centroid = cluster_vectors.mean(axis=0).values

        centroids.append(centroid)
    return np.array(centroids)

# recluster the updated users or small amount of new user 
def add_or_update_user(new_user_vector, user_id, male_clusters, female_clusters, male_data, female_data):
    """
    Adds or updates a user vector in the existing clusters.
    Finds the closest cluster for the new/modified user and assigns them to it.
    """
    # Example: Determine gender (0 for male, 1 for female)
    gender = new_user_vector["gender"]
    user_features = new_user_vector.drop("gender").values.reshape(1, -1)

    if gender == 0:  # Male user
        centroids = compute_centroids(male_clusters, male_data)
        similarity = cosine_similarity(user_features, centroids)
        closest_cluster = np.argmax(similarity)
        male_clusters[closest_cluster].append(user_id)  # Add user to closest male cluster
        print(f"User {user_id} added to Male Cluster {closest_cluster + 1}")
    else:  # Female user
        centroids = compute_centroids(female_clusters, female_data)
        similarity = cosine_similarity(user_features, centroids)
        closest_cluster = np.argmax(similarity)
        female_clusters[closest_cluster].append(user_id)  # Add user to closest female cluster
        print(f"User {user_id} added to Female Cluster {closest_cluster + 1}")
    
    # Optionally, update centroids of the modified clusters if necessary
    return male_clusters, female_clusters

# recluster all the users 
def full_reclustering():
    # Fetch all user data
    user_data = fetch_user_data()
    user_data.index = user_data.index.map(str)  # Ensure indices are strings for user IDs

    # Separate male and female users
    male_data = user_data[user_data["gender"] == 0].drop(columns=["gender"])
    female_data = user_data[user_data["gender"] == 1].drop(columns=["gender"])

    # Recompute male clusters
    k = 2  # Adjust k based on your needs
    knn_male = NearestNeighbors(n_neighbors=k, algorithm='auto')
    knn_male.fit(male_data)
    _, male_neighbors = knn_male.kneighbors(male_data)
    
    male_clusters = []
    for neighbors in male_neighbors:
        cluster_members = male_data.iloc[neighbors].index.tolist()
        male_clusters.append(cluster_members)

    # Recompute female clusters
    knn_female = NearestNeighbors(n_neighbors=k, algorithm='auto')
    knn_female.fit(female_data)
    _, female_neighbors = knn_female.kneighbors(female_data)
    
    female_clusters = []
    for neighbors in female_neighbors:
        cluster_members = female_data.iloc[neighbors].index.tolist()
        female_clusters.append(cluster_members)

    return male_clusters, female_clusters


# def recommend_clusters(male_clusters, female_clusters, male_data, female_data, similarity_threshold=1):
#     # Define feature weights to give more importance to the 'interest' column
#     feature_weights = np.array([1.0])  # Example: interest gets 1.0x weight

#     # Compute centroids for male and female clusters with feature weighting
#     male_centroids = compute_centroids(male_clusters, male_data, feature_weights=feature_weights)
#     female_centroids = compute_centroids(female_clusters, female_data, feature_weights=feature_weights)

#     recommendations = {}
#     for i, male_centroid in enumerate(male_centroids):
#         print(f"\nEvaluating recommendations for male_cluster_{i + 1}: centroid: {male_centroid}")
#         similar_female_clusters = []
#         max_similarity = 0
#         closest_female_cluster = None

#         for j, female_centroid in enumerate(female_centroids):
#             print(f"\nEvaluating recommendations for female_cluster_{i + 1}: centroid: {female_centroid}")
#             # Calculate cosine similarity
#             similarity = cosine_similarity([male_centroid], [female_centroid])[0][0]
            
#             print(f"  Similarity with female_cluster_{j + 1}: {similarity}")
            
#             # Track the most similar cluster for fallback
#             if similarity > max_similarity:
#                 max_similarity = similarity
#                 closest_female_cluster = f"female_cluster_{j + 1}"
#                 print(f"  Updated closest female cluster to: {closest_female_cluster} with similarity {max_similarity}")


#             # If similarity is above the threshold, consider them as a match
#             if similarity >= similarity_threshold:
#                 similar_female_clusters.append(f"female_cluster_{j + 1}")
#                 print(f"  Added female_cluster_{j + 1} to recommendations based on threshold {similarity_threshold}")


#         # Fallback: if no similar clusters found, recommend the closest based on highest similarity
#         if not similar_female_clusters and closest_female_cluster:
#             similar_female_clusters.append(closest_female_cluster)
#             print(f"  No clusters met the threshold. Fallback to closest cluster: {closest_female_cluster}")


#         recommendations[f"male_cluster_{i + 1}"] = similar_female_clusters
#         print(f"  Final recommendations for male_cluster_{i + 1}: {similar_female_clusters}")


#     return recommendations

def recommend_clusters(male_clusters, female_clusters, male_data, female_data, distance_threshold=1.5):
    # Compute centroids for male and female clusters
    male_centroids = compute_centroids(male_clusters, male_data)
    female_centroids = compute_centroids(female_clusters, female_data)

    recommendations = {}
    for i, male_centroid in enumerate(male_centroids):
        # print(f"\nEvaluating recommendations for male_cluster_{i + 1}: centroid: {male_centroid}")
        similar_female_clusters = []
        min_distance = float('inf')
        closest_female_cluster = None

        for j, female_centroid in enumerate(female_centroids):
            # Calculate Euclidean distance
            distance = euclidean_distances([male_centroid], [female_centroid])[0][0]
            # print(f"  Distance between male_cluster_{i + 1} and female_cluster_{j + 1}: {distance}")

            # Track the closest cluster for fallback
            if distance < min_distance:
                min_distance = distance
                closest_female_cluster = f"female_cluster_{j + 1}"
                # print(f"  Updated closest female cluster to: female_cluster_{j + 1} with distance {min_distance}")

            # If distance is below the threshold, consider them as a match
            if distance <= distance_threshold:
                similar_female_clusters.append(f"female_cluster_{j + 1}")
                # print(f"  Added female_cluster_{j + 1} to recommendations based on threshold {distance_threshold}")

        # Fallback: if no similar clusters found, recommend the closest based on minimum distance
        if not similar_female_clusters and closest_female_cluster:
            similar_female_clusters.append(closest_female_cluster)
            print(f"  No clusters met the threshold. Fallback to closest cluster: {closest_female_cluster}")

        recommendations[f"male_cluster_{i + 1}"] = similar_female_clusters
        print(f"  Final recommendations for male_cluster_{i + 1}: {similar_female_clusters}")

    return recommendations

# Main function to perform clustering and recommend clusters
def main():
    male_clusters, female_clusters, male_data, female_data = cluster_users(eps=0.6, min_samples=1)
    
    # Display initial clusters
    print("Male Clusters:", male_clusters)
    print("Female Clusters:", female_clusters)

    # Find and print recommendations
    recommendations = recommend_clusters(male_clusters, female_clusters, male_data, female_data)
    # print("\nCluster Recommendations:")
    # for male_cluster, recommended_female_clusters in recommendations.items():
    #     print(f"{male_cluster} is recommended to {recommended_female_clusters}")

if __name__ == "__main__":
    main()
