import pandas as pd
import matplotlib.pyplot as plt

# Lettura dei CSV
no_retry = pd.read_csv("sequence_no_retry_cdf.csv")
with_retry = pd.read_csv("sequence_retry_cdf.csv")
xor = pd.read_csv("sequence_uniform_one_shot_cdf.csv")

fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5.5), sharex=True, sharey=True)

# --- Primo subplot: Senza retry vs Con retry ---
ax1.plot(
    no_retry["time"],
    no_retry["cdf"],
    label="Sequenza senza retry",
    color="tab:blue",
    linewidth=2
)
ax1.plot(
    with_retry["time"],
    with_retry["cdf"],
    label="Sequenza con retry",
    color="tab:orange",
    linewidth=2
)
ax1.set_title("Senza retry vs Con retry")
ax1.set_xlabel("Tempo di risposta (secondi)")
ax1.set_ylabel("CDF - Probabilità cumulativa")
ax1.set_xlim(0, 20)
ax1.set_ylim(0, 1.05)
ax1.grid(True, alpha=0.3)
ax1.legend()

# --- Secondo subplot: Senza retry vs Con retry XOR ---
ax2.plot(
    no_retry["time"],
    no_retry["cdf"],
    label="Sequenza senza retry",
    color="tab:blue",
    linewidth=2
)
ax2.plot(
    xor["time"],
    xor["cdf"],
    label="Sequenza con retry XOR",
    color="tab:green",
    linewidth=2
)
ax2.set_title("Senza retry vs Con retry XOR")
ax2.set_xlabel("Tempo di risposta (secondi)")
ax2.set_xlim(0, 20)
ax2.set_ylim(0, 1.05)
ax2.grid(True, alpha=0.3)
ax2.legend()

plt.suptitle("Confronto sequenza di uniformi", fontsize=13)
plt.tight_layout()
plt.show()