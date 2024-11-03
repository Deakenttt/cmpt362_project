import torch
import torch.nn as nn
import torch.optim as optim

# Define the NCF model
class NCF(nn.Module):
    def __init__(self, num_users, num_items, embedding_dim):
        super(NCF, self).__init__()
        self.user_embedding = nn.Embedding(num_users, embedding_dim)
        self.item_embedding = nn.Embedding(num_items, embedding_dim)

        # MLP layers
        self.fc1 = nn.Linear(embedding_dim * 2, 128)
        self.fc2 = nn.Linear(128, 64)
        self.fc3 = nn.Linear(64, 32)
        self.output = nn.Linear(32, 1)
        self.sigmoid = nn.Sigmoid()

    def forward(self, user_indices, item_indices):
        user_embed = self.user_embedding(user_indices)
        item_embed = self.item_embedding(item_indices)
        x = torch.cat([user_embed, item_embed], dim=-1)
        
        # Pass through MLP layers
        x = torch.relu(self.fc1(x))
        x = torch.relu(self.fc2(x))
        x = torch.relu(self.fc3(x))
        x = self.sigmoid(self.output(x))
        return x

# Example usage
num_users = len(user_data)  # Number of users
num_items = len(user_data)  # Each user can be an "item" for recommendations
embedding_dim = 10

model = NCF(num_users, num_items, embedding_dim)
criterion = nn.BCELoss()
optimizer = optim.Adam(model.parameters(), lr=0.001)

# Training example
user_indices = torch.LongTensor([0, 1, 2])  # Example user indices
item_indices = torch.LongTensor([3, 4, 5])  # Example item indices (potential matches)
labels = torch.FloatTensor([1, 0, 1])  # Binary labels: 1 for like, 0 for dislike

# Forward pass
outputs = model(user_indices, item_indices)
loss = criterion(outputs, labels)
loss.backward()
optimizer.step()
