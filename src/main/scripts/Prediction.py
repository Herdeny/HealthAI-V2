import os
import sys
import numpy as np
import pandas as pd
import torch
import torch.nn.functional as F
from M2STGAT import Fusion
from collections import Counter

np.set_printoptions(linewidth=1000)
data_path = sys.argv[1]
model_path = sys.argv[2]
m12_gene_path = sys.argv[3]
m12_adjMatrix_path = sys.argv[4]
m24_gene_path = sys.argv[5]
m24_adjMatrix_path = sys.argv[6]
m36_gene_path = sys.argv[7]
m36_adjMatrix_path = sys.argv[8]


def load_model(model_path, device):
    """Load the trained model"""
    # Define model architecture same as in training
    input_in_dim = [1000, 1064]
    input_hidden_dim = [64, 1064]
    model = Fusion(num_class=5, num_views=6, hidden_dim=input_hidden_dim, dropout=0.1, in_dim=input_in_dim)

    # Load the model state
    print("Loading model from:", model_path)
    state = torch.load(model_path, map_location=device)
    model.load_state_dict(state['net'])
    model.to(device)
    model.eval()

    print("Model loaded successfully.")
    return model


def preprocess_data(feature_path, adj_matrix_path, threshold=0.2):
    """Load and preprocess feature and adjacency matrix data"""
    print(f"Loading adjacency matrix from: {adj_matrix_path}")
    # Load adjacency matrix
    exp_adj = pd.read_csv(adj_matrix_path, index_col=0, header=0)
    node_index = exp_adj.index
    node_index = np.array(node_index)
    exp_adj = np.array(exp_adj).astype(float)
    exp_adj = torch.LongTensor(np.where(exp_adj > threshold, 1, 0))

    print(f"Loaded adjacency matrix with shape: {exp_adj.shape}")

    # Load feature data
    print(f"Loading feature data from: {feature_path}")
    features = pd.read_csv(feature_path, index_col=0, header=0)
    features = features.loc[:, node_index]
    features = np.array(features).astype(float)
    features = torch.FloatTensor(features)

    print(f"Loaded feature data with shape: {features.shape}")

    # Also return original features DataFrame to get row indices
    return features, exp_adj, node_index, features.shape[0]


def inference(model, features, adj_matrix, tcn_data_list=None, device="cpu", batch_size=8):
    """Run inference on the given data"""
    print("Starting inference...")
    model.eval()
    all_predictions = []
    all_probabilities = []

    # Create data loader
    dataset = torch.utils.data.TensorDataset(features)
    data_loader = torch.utils.data.DataLoader(dataset=dataset, batch_size=batch_size, shuffle=False)

    with torch.no_grad():
        for i, (batch_x,) in enumerate(data_loader):
            print(f"Processing batch {i+1}/{len(data_loader)}...")

            # Reshape batch_x as in the original code
            batch_x_reshaped = batch_x.reshape(-1, 1000, 1)
            batch_x_reshaped = batch_x_reshaped.to(torch.float32)
            batch_x_reshaped = batch_x_reshaped.to(device)

            # Move adjacency matrix to device
            adj_matrix = adj_matrix.to(device)

            # Prepare TCN data if available
            current_tcn_data = None
            if tcn_data_list is not None:
                # For a single time point
                if len(tcn_data_list) == 1:
                    current_tcn_data = tcn_data_list[0][i].to(device)
                    tcn_infer = False
                # For multiple time points
                else:
                    tcn_batch_data = []
                    for tcn_data in tcn_data_list:
                        tcn_batch_data.append(tcn_data[i])
                    current_tcn_data = torch.stack(tcn_batch_data, dim=0).to(torch.float32).to(device)
                    tcn_infer = True
            else:
                # If no TCN data, use batch_x
                current_tcn_data = batch_x.to(device)
                tcn_infer = False

            # Get predictions
            logits = model.infer(batch_x_reshaped, adj_matrix, current_tcn_data, tcn_infer=tcn_infer)
            probabilities = F.softmax(logits, dim=1)
            predictions = torch.argmax(probabilities, dim=1)

            # Store results
            all_predictions.extend(predictions.cpu().numpy())
            all_probabilities.extend(probabilities.cpu().numpy())

            print(f"Batch {i+1} processed. Predictions: {predictions.cpu().numpy()}")

    print("Inference completed.")
    return np.array(all_predictions), np.array(all_probabilities)


def main():
    # Set device
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")

    # Load the model
    print("Loading model...")
    model = load_model(model_path, device)

    # Paths for new data to make predictions on
    # You can adjust these paths as needed
    feature_path = data_path + m36_gene_path
    adj_matrix_path = data_path + m36_adjMatrix_path

    # Load and preprocess data
    print(f"Preprocessing data for current time point (m36)...")
    features, adj_matrix, node_index, num_samples = preprocess_data(feature_path, adj_matrix_path)

    # For multi-time-point inference
    print(f"Preprocessing data for previous time points (m12, m24)...")
    # Load previous time points data
    features1, adj_matrix1, _, _ = preprocess_data(data_path + m12_gene_path, data_path + m12_adjMatrix_path)
    features2, adj_matrix2, _, _ = preprocess_data(data_path + m24_gene_path, data_path + m24_adjMatrix_path)

    # Create TCN data batches for each time point
    tcn_data_list = []
    print("Creating TCN data batches for time points m12, m24, and m36...")
    dataset1 = torch.utils.data.TensorDataset(features1)
    data_loader1 = torch.utils.data.DataLoader(dataset=dataset1, batch_size=8, shuffle=False)
    tcn_data_list.append([batch_x for batch_x, in data_loader1])

    dataset2 = torch.utils.data.TensorDataset(features2)
    data_loader2 = torch.utils.data.DataLoader(dataset=dataset2, batch_size=8, shuffle=False)
    tcn_data_list.append([batch_x for batch_x, in data_loader2])

    # Current time point (features and adj_matrix from above)
    dataset3 = torch.utils.data.TensorDataset(features)
    data_loader3 = torch.utils.data.DataLoader(dataset=dataset3, batch_size=8, shuffle=False)
    tcn_data_list.append([batch_x for batch_x, in data_loader3])

    # Run multi-time-point inference
    print("Running multi-time-point inference...")
    predictions, probabilities = inference(model, features, adj_matrix, tcn_data_list, device=device)
    print(f"Result: {predictions}")

    # Save predictions to CSV - without using node_index as index to avoid shape mismatch
    # pd.DataFrame(predictions, columns=["Predicted_Class"]).to_csv("predictions.csv")

    # Save probabilities with appropriate shape
    prob_df = pd.DataFrame(probabilities, columns=[f"Class_{i}" for i in range(5)])
    probabilities_path = data_path + "prediction_probabilities.csv"
    prob_df.to_csv(probabilities_path)
    print(f"Predictions probabilities saved to: {probabilities_path}")
    print(prob_df)

    # Additionally, save a mapping file with node indices and predictions
    sample_ids = pd.read_csv(feature_path, index_col=0).index.values
    predictions_path = data_path + "sample_predictions.csv"
    mapping_df = pd.DataFrame({
        'Sample_ID': sample_ids,
        'Predicted_Class': predictions
    })
    mapping_df.to_csv(predictions_path, index=False)
    print(f"Predictions mapping saved to: {predictions_path}")
    print("Prediction distribution:", Counter(map(int, predictions)))
    print(f"PATH1: {predictions_path}")
    print(f"PATH2: {probabilities_path}")


if __name__ == "__main__":
    main()
