import torch
import torch.nn as nn
import torch.optim as optim

class SiameseNetwork(nn.Module):
    def __init__(self, embedding_dim):
        super(SiameseNetwork, self).__init__()
        self.fc1 = nn.Linear(embedding_dim, 128)
        self.fc2 = nn.Linear(128, 64)
        self.fc3 = nn.Linear(64, 32)

    def forward(self, x):
        x = torch.relu(self.fc1(x))
        x = torch.relu(self.fc2(x))
        x = self.fc3(x)
        return x

    def get_similarity(self, user1, user2):
        user1_embed = self.forward(user1)
        user2_embed = self.forward(user2)
        return torch.cosine_similarity(user1_embed, user2_embed)

# Example usage
embedding_dim = len(user_features[0])  # Dimensionality of the feature vector
model = SiameseNetwork(embedding_dim)

# Assume we have feature vectors for two users
user1_features = torch.FloatTensor([28, 0, 0, 1, 1])
user2_features = torch.FloatTensor([25, 0, 1, 1, 1])

# Calculate similarity
similarity_score = model.get_similarity(user1_features, user2_features)
print("Similarity Score:", similarity_score)
