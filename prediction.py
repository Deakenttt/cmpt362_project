from os import abort
from flask import Flask, jsonify
from cluster import cluster_users, recommend_clusters
from data import fetch_user_data, db  # Import db directly from data.py

app = Flask(__name__)

# Endpoint to run clustering and update clusters and recommendations in Firebase
@app.route("/update_clusters", methods=["POST"])
def update_clusters():
    # Fetch user data with document IDs
    user_data = fetch_user_data()

    # Perform clustering
    male_clusters, female_clusters, male_data, female_data = cluster_users(eps=0.6, min_samples=1)
    recommendations = recommend_clusters(male_clusters, female_clusters, male_data, female_data)

    # Save clusters and recommendations in Firebase
    save_clusters_and_recommendations(male_clusters, female_clusters, recommendations, user_data)

    return jsonify({"status": "Clusters and recommendations updated in Firebase"})


def save_clusters_and_recommendations(male_clusters, female_clusters, recommendations, user_data):
    # Generate a dictionary mapping user indices to their document IDs
    doc_id_mapping = {int(index): row['id'] for index, row in user_data.iterrows()}  # Convert index to integer

    # Log the clusters and recommendations before processing
    print("Male Clusters:", male_clusters)
    print("Female Clusters:", female_clusters)
    print("Recommendations:", recommendations)
    
    # Ensure `male_cluster_3` is present in recommendations
    print("Checking if male_cluster_3 is in recommendations:", 'male_cluster_3' in recommendations)

    # Save male clusters with recommendations
    for male_cluster_name, recommended_female_clusters in recommendations.items():
        male_cluster_num = int(male_cluster_name.split('_')[-1])  # Extract cluster number
        male_cluster_members = male_clusters[male_cluster_name]

        print(f"\nProcessing male_cluster: {male_cluster_name}")
        print(f"  Male cluster number: {male_cluster_num}")
        print(f"  Male cluster members: {male_cluster_members}")
        print(f"  Recommended female clusters: {recommended_female_clusters}")

        for user_index in male_cluster_members:
            user_index = int(user_index)  # Ensure user_index is an integer
            
            if user_index in doc_id_mapping:
                user_doc_id = doc_id_mapping[user_index]
                db.collection("users").document(user_doc_id).set({
                    "cluster": male_cluster_num,
                    "recommended": [int(female_cluster.split('_')[-1]) for female_cluster in recommended_female_clusters]
                }, merge=True)
                print(f"User {user_doc_id} updated with cluster {male_cluster_num} and recommended clusters {[int(female_cluster.split('_')[-1]) for female_cluster in recommended_female_clusters]}")
            else:
                print(f"Warning: User index {user_index} not found in doc_id_mapping")

    # Save female clusters with reverse recommendations
    for female_cluster_name, female_cluster_members in female_clusters.items():
        female_cluster_num = int(female_cluster_name.split('_')[-1])  # Extract cluster number
        recommended_male_clusters = []

        # Find which male clusters are recommending this female cluster
        for male_cluster_name, recommended_female_clusters in recommendations.items():
            if female_cluster_name in recommended_female_clusters:
                recommended_male_clusters.append(int(male_cluster_name.split('_')[-1]))

        print(f"\nProcessing female_cluster: {female_cluster_name}")
        print(f"  Female cluster number: {female_cluster_num}")
        print(f"  Female cluster members: {female_cluster_members}")
        print(f"  Recommended male clusters: {recommended_male_clusters}")

        for user_index in female_cluster_members:
            user_index = int(user_index)  # Ensure user_index is an integer
            
            if user_index in doc_id_mapping:
                user_doc_id = doc_id_mapping[user_index]
                db.collection("users").document(user_doc_id).set({
                    "cluster": female_cluster_num,
                    "recommended": recommended_male_clusters
                }, merge=True)
                print(f"User {user_doc_id} updated with cluster {female_cluster_num} and recommended clusters {recommended_male_clusters}")
            else:
                print(f"Warning: User index {user_index} not found in doc_id_mapping")


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
