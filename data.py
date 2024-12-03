import firebase_admin
from firebase_admin import credentials, firestore
import pandas as pd

# Initialize Firebase Admin SDK
cred = credentials.Certificate("serviceAccountKey.json")  # Replace with actual path to your Firebase key
firebase_admin.initialize_app(cred)
db = firestore.client()

# Function to fetch user data from Firebase Firestore
# def fetch_user_data():
#     users_ref = db.collection("users")
#     docs = users_ref.stream()

#     user_data = []
#     for doc in docs:
#         doc_data = doc.to_dict()
#         doc_data['id'] = doc.id  # Add the document ID as a new field
#         user_data.append(doc_data)

#     # Convert to DataFrame for easier processing
#     data = pd.DataFrame(user_data)

#     data["gender"] = data["gender"].map({"M": 0, "F": 1})

#     data["interest"] = data["interest"].str.lower().map({"snowboarding": 1, "swimming": 2, "drinking": 3, "poker": 4})


#     # Filter data to include only rows where "interest" is mapped correctly
#     data = data[data["interest"].notna()]

#     dataset = data[["id", "gender", "interest", "name"]]

#     # Adjust pandas settings to display all rows
#     pd.set_option('display.max_rows', None)
#     pd.set_option('display.max_columns', None)

#     # Print the extracted data to verify successful retrieval
#     print("Extracted Data from Firebase:")
#     print(dataset)

#     # Select relevant columns
#     return dataset

# test the function to fetch and print data
# fetch_user_data()

#
# asscess from profileinfo DB

# Define the mapping for interests
interest_mapping = {
    "Travel": 1,
    "Football": 2,
    "Snowboarding": 3,
    "Swimming": 4,
    "Drinking": 5,
    "Poker": 6,
    "Photography": 7,
    "Movies": 8,
    "Skiing": 3,
    "Reading": 1,
    "Hiking": 1
}

# Function to fetch and process profileinfo data from Firebase Firestore
def fetch_user_data():
    # Fetch data from the "profileinfo" collection
    profileinfo_ref = db.collection("profileinfo")
    docs = profileinfo_ref.stream()

    profile_data = []
    for doc in docs:
        doc_data = doc.to_dict()
        doc_data['id'] = doc.id  # Add the document ID as a new field
        profile_data.append(doc_data)

    # Convert to DataFrame for easier processing
    data = pd.DataFrame(profile_data)

    # Map gender values to numeric representations
    data["gender"] = data["gender"].map({"Male": 0, "Female": 1})

    # Map interest columns to integers using the defined mapping
    for col in ["interest1", "interest2", "interest3"]:
        data[col] = data[col].map(interest_mapping)

    # Select relevant columns
    dataset = data[["id", "gender", "interest1", "interest2", "interest3", "name"]]

    # Adjust pandas settings to display all rows
    pd.set_option('display.max_rows', None)
    pd.set_option('display.max_columns', None)

    # Print the processed data to verify successful retrieval
    print("Processed Data from profileinfo:")
    print(dataset)

    return dataset

# Test the function to fetch and print data
fetch_user_data()