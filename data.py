import firebase_admin
from firebase_admin import credentials, firestore
import pandas as pd

# Initialize Firebase Admin SDK
cred = credentials.Certificate("serviceAccountKey.json")  # Replace with actual path to your Firebase key
firebase_admin.initialize_app(cred)
db = firestore.client()

# Function to fetch user data from Firebase Firestore
def fetch_user_data():
    users_ref = db.collection("users")
    docs = users_ref.stream()

    user_data = []
    for doc in docs:
        doc_data = doc.to_dict()
        doc_data['id'] = doc.id  # Add the document ID as a new field
        user_data.append(doc_data)

    # Convert to DataFrame for easier processing
    data = pd.DataFrame(user_data)

    # Ensure categorical data is mapped to numerical values for KNN compatibility
    # data["gender"] = data["gender"].map({"M": 0, "F": 1})
    # data["relationship"] = data["relationship"].map({"serious": 1, "casual": 0})
    # data["hair_color"] = data["hair_color"].map({"black": 0, "blonde": 1, "grey": 2, "red": 3})

    # # Select relevant columns
    # return data[["age", "gender", "hair_color", "outdoorsy", "relationship"]]

    data["gender"] = data["gender"].map({"M": 0, "F": 1})

    data["interest"] = data["interest"].str.lower()
    data["interest"] = data["interest"].map({"Snowboarding": 1, "swimming": 2, "drinking": 3, "poker": 4 })

    # Filter data to include only rows where "interest" is mapped correctly
    data = data[data["interest"].notna()]

    dataset = data[["id", "gender", "interest", "name"]]

    # Print the extracted data to verify successful retrieval
    print("Extracted Data from Firebase:")
    print(dataset)

    # Select relevant columns
    return dataset

# test the function to fetch and print data
# fetch_user_data()
